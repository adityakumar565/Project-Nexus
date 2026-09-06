package com.workflow.dag_engine.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.graphs.factory.GraphComponentManagerFactory;
import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathBridgeInterface;
import com.workflow.dag_engine.componentManager.paths.component_manager.PathComponentManagerImpl;
import com.workflow.dag_engine.helpers.GraphUtility;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.interfaces.componentManager.PathComponentManagerInterface;
import com.workflow.dag_engine.interfaces.wrapper.GraphInterface;
import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.enums.ImplementationType;
import com.workflow.dag_engine.models.graph.GraphMetaData;
import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.graph.UserGraphRequest;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
import com.workflow.dag_engine.models.path.GraphPathResponse;
import com.workflow.dag_engine.models.path.PathDTO;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;
import com.workflow.dag_engine.models.validation.ApplicationException;
import com.workflow.dag_engine.models.validation.ErrorDetails;
import com.workflow.dag_engine.persistence.entities.GraphEntity;
import com.workflow.dag_engine.persistence.repositories.GraphRepository;

@Component("graphServicesV1")
public class GraphImpl implements GraphInterface {

    private static final Logger log = LoggerFactory.getLogger(GraphImpl.class);

    private final GraphRepository objGraphRepository;
    private final UserInterface objUserInterface;
    private final GraphComponentManagerInterface objGraphComponentManager;
    private final GraphComponentManagerFactory componentManagerFactory;
    private final PathBridgeInterface pathBridge;

    // In-memory active graph registry keyed by graphId
    private final Map<Long, GraphComponentManagerInterface> inMemoryGraphs = new ConcurrentHashMap<>();

    @Autowired
    public GraphImpl(
            GraphRepository objGraphRepository,
            @Qualifier("userServicesV1") UserInterface objUserInterface,
            @Qualifier("graphComponentManagerAdjacency") GraphComponentManagerInterface objGraphComponentManager,
            GraphComponentManagerFactory componentManagerFactory,
            @Qualifier("pathBridge") PathBridgeInterface pathBridge) {
        this.objGraphRepository = objGraphRepository;
        this.objUserInterface = objUserInterface;
        this.objGraphComponentManager = objGraphComponentManager;
        this.componentManagerFactory = componentManagerFactory;
        this.pathBridge = pathBridge;
    }

    @Override
    public GraphResponse uploadUserGraph(GraphUploadRequest graphUploadRequest) throws Exception {

        String methodName = "Inside GraphImpl.uploadUserGraph --> ";

        log.info(methodName + " graphUploadRequest:" + graphUploadRequest.toString());

        GraphResponse graphResponse = new GraphResponse();

        ErrorDetails objErrorDetails = new ErrorDetails(1);

        // 1. Check user existence through UserInterface (maintaining loose coupling)
        UserRequest userRequest = new UserRequest();
        userRequest.setUserId(graphUploadRequest.getUserId());

        UserResponse userResponse = objUserInterface.validateUser(userRequest);

        if (userResponse != null && userResponse.getObjErrorDetails() != null
                && !"0".equals(userResponse.getObjErrorDetails().getErrorCode())) {
            log.error(methodName + " User validation failed: " + userResponse.getObjErrorDetails().getErrorMessage());
            graphResponse.setObjErrorDetails(userResponse.getObjErrorDetails());
            graphResponse.setGraphName("User validation failed for graph: " + graphUploadRequest.getGraphName());
            graphResponse.setGraphDescription(userResponse.getObjErrorDetails().getErrorMessage());
            return graphResponse;
        }

        // 2. Proceed with graph entity conversion and saving
        graphResponse.setObjErrorDetails(objErrorDetails);

        GraphEntity objGraphEntity = GraphUtility.convertGraphUploadRequestToGraphEntity(graphUploadRequest);

        log.info(methodName + " Returning objGraphEntity:" + objGraphEntity.toString());

        String storageGraphPath = objGraphComponentManager.transformAndStoreGraph(graphUploadRequest);

        log.info(methodName + " Storage Graph Path:" + storageGraphPath);

        objGraphEntity.setBinaryFilePath(storageGraphPath);

        // Generate and store path representation
        PathComponentManagerInterface pathCm = new PathComponentManagerImpl(objGraphComponentManager, pathBridge);
        String pathStoragePath = pathCm.storePaths(objGraphEntity.getGraphName());
        
        log.info(methodName + " Storage Path Path:" + pathStoragePath);
        
        objGraphEntity.setPathBinaryFilePath(pathStoragePath);
        objGraphEntity.setPathVersion(1);

        ImplementationType implType = objGraphComponentManager.getImplementationType();
        objGraphEntity
                .setImplementationType(implType != null ? implType.name() : ImplementationType.ADJACENCY_V1.name());

        objGraphRepository.save(objGraphEntity);

        if (objGraphEntity.getGraphId() != null) {
            inMemoryGraphs.put(objGraphEntity.getGraphId(), objGraphComponentManager);
        }

        graphResponse.setGraphName(objGraphEntity.getGraphName());
        graphResponse.setGraphDescription(objGraphEntity.getGraphDescription());

        log.info(methodName + " Returning graphResponse:" + graphResponse.toString());

        return graphResponse;
    }

    @Override
    public GraphResponse getGraphById(GraphMetaData graphMetaData) throws Exception {
        String methodName = "Inside GraphImpl.getGraphById --> ";
        log.info(methodName + " graphMetaData: " + graphMetaData);

        GraphResponse objGraphResponse = new GraphResponse();
        objGraphResponse.setGraphName(graphMetaData != null ? graphMetaData.getGraphName() : null);
        objGraphResponse.setGraphDescription(graphMetaData != null ? graphMetaData.getGraphDescription() : null);
        ErrorDetails objErrorDetails = new ErrorDetails(1);
        objGraphResponse.setObjErrorDetails(objErrorDetails);

        try {
            if (graphMetaData == null) {
                throw new ApplicationException("2001", "Invalid GraphMetaData");
            }

            Long graphId = graphMetaData.getGraphId();
            GraphEntity objGraphEntity = null;

            if (graphId != null) {
                objGraphEntity = objGraphRepository.findById(graphId).orElse(null);
            } else if (graphMetaData.getUserId() != null) {
                List<GraphEntity> userGraphs = objGraphRepository.findByUserId(graphMetaData.getUserId());
                if (userGraphs != null && !userGraphs.isEmpty()) {
                    objGraphEntity = userGraphs.get(0);
                    graphId = objGraphEntity.getGraphId();
                }
            } else {
                throw new ApplicationException("2001", "Invalid GraphMetaData: graphId or userId required");
            }

            if (objGraphEntity == null) {
                throw new ApplicationException("2002", "Error in loading graph Or Graph does not Exists for the user");
            }

            String storagePath = objGraphEntity.getBinaryFilePath();
            GraphUploadRequest graphUpload = objGraphEntity.getGraphData();
            if (graphUpload != null && graphUpload.getGraphId() == null) {
                graphUpload.setGraphId(objGraphEntity.getGraphId());
            }
            objGraphResponse.setObjGraphUploadRequest(graphUpload);
            objGraphResponse.setGraphName(objGraphEntity.getGraphName());
            objGraphResponse.setGraphDescription(objGraphEntity.getGraphDescription());

            // Load graph into in-memory registry if not already active
            GraphComponentManagerInterface manager = inMemoryGraphs.get(graphId);
            if (manager == null) {
                manager = componentManagerFactory.getComponentManager(objGraphEntity.getImplementationType());
                if (storagePath != null && !storagePath.isEmpty()) {
                    manager.loadGraph(storagePath);
                }
                inMemoryGraphs.put(graphId, manager);
                log.info(methodName + " Graph " + graphId + " loaded into in-memory registry using "
                        + objGraphEntity.getImplementationType());
            } else {
                log.info(methodName + " Graph " + graphId + " was already active in memory");
            }

        } catch (ApplicationException a) {
            log.error(methodName + " ApplicationException: " + a.message);
            objErrorDetails.setErrorCode(a.code);
            objErrorDetails.setErrorMessage(a.message);
        } catch (Exception e) {
            log.error(methodName + " Exception: ", e);
            objErrorDetails.setErrorCode("500");
            objErrorDetails.setErrorMessage("Error in loading graph: " + e.getMessage());
        }

        return objGraphResponse;
    }

    @Override
    public GraphResponse closeGraphById(GraphMetaData graphMetaData) throws Exception {
        String methodName = "Inside GraphImpl.closeGraphById --> ";
        log.info(methodName + " graphMetaData: " + graphMetaData);

        GraphResponse objGraphResponse = new GraphResponse();
        ErrorDetails objErrorDetails = new ErrorDetails(1);
        objGraphResponse.setObjErrorDetails(objErrorDetails);

        try {
            if (graphMetaData == null || graphMetaData.getGraphId() == null) {
                throw new ApplicationException("2001", "Invalid GraphMetaData or graphId");
            }

            Long graphId = graphMetaData.getGraphId();
            GraphComponentManagerInterface manager = inMemoryGraphs.remove(graphId);
            if (manager != null) {
                manager.unload();
                log.info(methodName + " Graph " + graphId + " successfully unloaded from memory");
            } else {
                log.info(methodName + " Graph " + graphId + " was not in active memory");
            }

            objGraphResponse.setGraphName(graphMetaData.getGraphName());
            objGraphResponse.setGraphDescription("Graph closed and unloaded from memory successfully");

        } catch (ApplicationException a) {
            log.error(methodName + " ApplicationException: " + a.message);
            objErrorDetails.setErrorCode(a.code);
            objErrorDetails.setErrorMessage(a.message);
        } catch (Exception e) {
            log.error(methodName + " Exception: ", e);
            objErrorDetails.setErrorCode("500");
            objErrorDetails.setErrorMessage("Error in closing graph: " + e.getMessage());
        }

        return objGraphResponse;
    }

    /**
     * Helper to retrieve an active in-memory graph for execution and pathfinding.
     */
    public GraphComponentManagerInterface getActiveGraph(Long graphId) throws ApplicationException {
        GraphComponentManagerInterface manager = inMemoryGraphs.get(graphId);
        if (manager == null) {
            throw new ApplicationException("2003",
                    "Graph " + graphId + " is not loaded in memory. Please call getGraphById first.");
        }
        return manager;
    }

    @Override
    public GraphResponse deleteGraph(GraphMetaData graphMetaData) throws Exception {
        String methodName = "Inside GraphImpl.deleteGraph --> ";
        log.info(methodName + " graphMetaData: " + graphMetaData);

        GraphResponse graphResponse = new GraphResponse();
        ErrorDetails objErrorDetails = new ErrorDetails(1);
        graphResponse.setObjErrorDetails(objErrorDetails);

        try {
            // 1. Validate input
            if (graphMetaData == null || graphMetaData.getGraphId() == null) {
                throw new ApplicationException("2001", "Invalid GraphMetaData or graphId");
            }

            Long graphId = graphMetaData.getGraphId();
            Long userId = graphMetaData.getUserId();

            // 2. Validate user existence (maintaining loose coupling via UserInterface)
            if (userId != null) {
                UserRequest userRequest = new UserRequest();
                userRequest.setUserId(userId);
                UserResponse userResponse = objUserInterface.validateUser(userRequest);
                if (userResponse != null && userResponse.getObjErrorDetails() != null
                        && !"0".equals(userResponse.getObjErrorDetails().getErrorCode())) {
                    log.error(methodName + " User validation failed for user " + userId);
                    graphResponse.setObjErrorDetails(userResponse.getObjErrorDetails());
                    graphResponse.setGraphName(graphMetaData.getGraphName());
                    graphResponse.setGraphDescription("User Not Available");
                    return graphResponse;
                }
            }

            // 3. Validate graph existence in DB
            GraphEntity objGraphEntity = objGraphRepository.findById(graphId).orElse(null);
            if (objGraphEntity == null) {
                throw new ApplicationException("2002", "Error in deleting graph: Graph does not exist");
            }

            // 4. Verify graph ownership if userId was provided
            if (userId != null && !userId.equals(objGraphEntity.getUserId())) {
                throw new ApplicationException("2004", "Graph does not belong to the specified user");
            }

            // 5. Check if graph is currently loaded in memory - must NOT allow deletion while active
            if (inMemoryGraphs.containsKey(graphId)) {
                throw new ApplicationException("2005",
                        "Cannot delete graph: Graph is currently active in memory. Close or unload it first.");
            }

            // 6. Delete binary file from disk
            String binaryFilePath = objGraphEntity.getBinaryFilePath();
            if (binaryFilePath != null && !binaryFilePath.trim().isEmpty()) {
                java.io.File file = new java.io.File(binaryFilePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info(methodName + " Successfully deleted binary file: " + binaryFilePath);
                    } else {
                        log.warn(methodName + " Could not delete binary file: " + binaryFilePath);
                    }
                } else {
                    log.info(methodName + " Binary file did not exist on disk: " + binaryFilePath);
                }
            }

            // 6.5. Delete path binary file from disk
            String pathBinaryFilePath = objGraphEntity.getPathBinaryFilePath();
            if (pathBinaryFilePath != null && !pathBinaryFilePath.trim().isEmpty()) {
                java.io.File file = new java.io.File(pathBinaryFilePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info(methodName + " Successfully deleted path binary file: " + pathBinaryFilePath);
                    } else {
                        log.warn(methodName + " Could not delete path binary file: " + pathBinaryFilePath);
                    }
                } else {
                    log.info(methodName + " Path binary file did not exist on disk: " + pathBinaryFilePath);
                }
            }

            // 7. Delete graph from DB
            objGraphRepository.delete(objGraphEntity);
            log.info(methodName + " Successfully deleted graph entity with id " + graphId);

            graphResponse.setGraphName(objGraphEntity.getGraphName());
            graphResponse.setGraphDescription("Graph deleted successfully");

        } catch (ApplicationException a) {
            log.error(methodName + " ApplicationException: " + a.message);
            objErrorDetails.setErrorCode(a.code);
            objErrorDetails.setErrorMessage(a.message);
        } catch (Exception e) {
            log.error(methodName + " Exception: ", e);
            objErrorDetails.setErrorCode("500");
            objErrorDetails.setErrorMessage("Error in deleting graph: " + e.getMessage());
        }

        return graphResponse;
    }

    @Override
    public UserGraphResponse getUserGraphs(UserGraphRequest userGraphRequest) throws Exception {
        String methodName = "Inside GraphImpl.getUserGraphs --> ";
        log.info(methodName + " userGraphRequest:" + userGraphRequest);

        UserGraphResponse userGraphResponse = new UserGraphResponse();

        try {
            if (userGraphRequest == null || userGraphRequest.getUserId() == null) {
                userGraphResponse.setObjErrorDetails(new ErrorDetails("200", "User ID cannot be null"));
                return userGraphResponse;
            }

            List<GraphEntity> graphEntities = objGraphRepository.findByUserId(userGraphRequest.getUserId());
            List<GraphUploadRequest> graphList = new ArrayList<>();

            if (graphEntities != null) {
                for (GraphEntity entity : graphEntities) {
                    if (entity.getGraphData() != null) {
                        GraphUploadRequest req = entity.getGraphData();
                        if (req.getGraphId() == null) {
                            req.setGraphId(entity.getGraphId());
                        }
                        graphList.add(req);
                    }
                }
            }

            userGraphResponse.setUserGraphList(graphList);
            userGraphResponse.setObjErrorDetails(new ErrorDetails(1)); // SUCCESS

        } catch (Exception e) {
            log.error(methodName + " Exception occurred: ", e);
            userGraphResponse.setObjErrorDetails(new ErrorDetails("500", e.getMessage()));
        }

        return userGraphResponse;
    }

    @Override
    public GraphResponse updateGraph(GraphUpdateRequest request) throws Exception {
        String methodName = "Inside GraphImpl.updateGraph --> ";
        log.info(methodName + " request: " + request);

        if (request == null || request.getGraphId() == null) {
            GraphResponse err = new GraphResponse();
            err.setObjErrorDetails(new ErrorDetails("400", "Graph ID cannot be null"));
            return err;
        }

        if (request.getUpdateType() == null) {
            GraphResponse err = new GraphResponse();
            err.setObjErrorDetails(new ErrorDetails("400", "Update Type cannot be null"));
            return err;
        }

        if (request.getUpdateType().equalsIgnoreCase("C")) {
            // Complex Update: Re-generate binaries and overwrite
            GraphEntity entity = objGraphRepository.findById(request.getGraphId()).orElseThrow(() -> new ApplicationException("404", "Graph Not Found"));
            
            // Release memory if loaded
            if (inMemoryGraphs.containsKey(request.getGraphId())) {
                GraphComponentManagerInterface manager = inMemoryGraphs.remove(request.getGraphId());
                manager.unload();
            }

            // Generate new binary and path files
            String storageGraphPath = objGraphComponentManager.transformAndStoreGraph(request);
            PathComponentManagerInterface pathCm = new PathComponentManagerImpl(objGraphComponentManager, pathBridge);
            String pathStoragePath = pathCm.storePaths(request.getGraphName());
            
            // Update entity with new structure and files
            entity.setGraphName(request.getGraphName());
            entity.setGraphDescription(request.getGraphDescription());
            entity.setGraphData(request);
            entity.setNumNodes(objGraphComponentManager.getNumberOfNodes());
            entity.setNumEdges(objGraphComponentManager.getNumberOfEdges());
            entity.setCostDimension(objGraphComponentManager.getCostDimension());
            
            GraphEntity tempEntity = GraphUtility.convertGraphUploadRequestToGraphEntity(request);
            entity.setIsCyclic(tempEntity.getIsCyclic());
            
            entity.setBinaryFilePath(storageGraphPath);
            entity.setPathBinaryFilePath(pathStoragePath);
            
            objGraphRepository.save(entity);
            
            // Reload into memory
            inMemoryGraphs.put(entity.getGraphId(), objGraphComponentManager);
            
            GraphResponse graphResponse = new GraphResponse();
            graphResponse.setObjErrorDetails(new ErrorDetails(1));
            graphResponse.setGraphName(entity.getGraphName());
            graphResponse.setGraphDescription(entity.getGraphDescription());
            graphResponse.setObjGraphUploadRequest(request);
            return graphResponse;
            
        } else if (request.getUpdateType().equalsIgnoreCase("S")) {
            // Simple Update: Update metadata only
            GraphEntity entity = objGraphRepository.findById(request.getGraphId()).orElseThrow(() -> new ApplicationException("404", "Graph Not Found"));
            
            entity.setGraphName(request.getGraphName());
            entity.setGraphDescription(request.getGraphDescription());
            entity.setGraphData(request); 
            
            objGraphRepository.save(entity);
            
            if (inMemoryGraphs.containsKey(request.getGraphId())) {
                GraphComponentManagerInterface manager = inMemoryGraphs.get(request.getGraphId());
                manager.updateMetaData(request.getCostNames());
            }
            
            GraphResponse graphResponse = new GraphResponse();
            graphResponse.setObjErrorDetails(new ErrorDetails(1));
            graphResponse.setGraphName(entity.getGraphName());
            graphResponse.setGraphDescription(entity.getGraphDescription());
            graphResponse.setObjGraphUploadRequest(request);
            return graphResponse;
        } else {
            GraphResponse err = new GraphResponse();
            err.setObjErrorDetails(new ErrorDetails("400", "Invalid update type: " + request.getUpdateType()));
            return err;
        }
    }

    @Override
    public GraphPathResponse getGraphPaths(GraphMetaData graphMetaData) throws Exception {
        String methodName = "Inside GraphImpl.getGraphPaths --> ";
        log.info(methodName + " graphMetaData: " + graphMetaData);

        GraphPathResponse response = new GraphPathResponse();
        ErrorDetails objErrorDetails = new ErrorDetails(1);
        response.setObjErrorDetails(objErrorDetails);

        try {
            if (graphMetaData == null || graphMetaData.getGraphId() == null) {
                throw new ApplicationException("2001", "Invalid GraphMetaData or graphId");
            }

            Long graphId = graphMetaData.getGraphId();
            response.setGraphId(graphId);

            GraphEntity objGraphEntity = objGraphRepository.findById(graphId).orElse(null);
            if (objGraphEntity == null) {
                throw new ApplicationException("2002", "Error in loading graph Or Graph does not Exist for the user");
            }

            response.setGraphName(objGraphEntity.getGraphName());

            // 1. Ensure GraphComponentManager is loaded in memory
            GraphComponentManagerInterface manager = inMemoryGraphs.get(graphId);
            if (manager == null) {
                manager = componentManagerFactory.getComponentManager(objGraphEntity.getImplementationType());
                String storagePath = objGraphEntity.getBinaryFilePath();
                if (storagePath != null && !storagePath.isEmpty() && new java.io.File(storagePath).exists()) {
                    manager.loadGraph(storagePath);
                }
                inMemoryGraphs.put(graphId, manager);
                log.info(methodName + " Graph " + graphId + " loaded into in-memory registry");
            }

            // 2. Load or compute paths
            String pathStoragePath = objGraphEntity.getPathBinaryFilePath();
            PathComponentManagerInterface pathCm;

            if (pathStoragePath != null && !pathStoragePath.isEmpty() && new java.io.File(pathStoragePath).exists()) {
                log.info(methodName + " Loading pre-computed paths from file: " + pathStoragePath);
                pathCm = new PathComponentManagerImpl(manager, pathBridge, false);
                pathCm.loadPaths(pathStoragePath);
            } else {
                log.info(methodName + " Pre-computed path file not found. Baking paths from graph component manager.");
                pathCm = new PathComponentManagerImpl(manager, pathBridge, true);
                String newPathStoragePath = pathCm.storePaths(objGraphEntity.getGraphName());
                objGraphEntity.setPathBinaryFilePath(newPathStoragePath);
                objGraphEntity.setPathVersion(1);
                objGraphRepository.save(objGraphEntity);
                log.info(methodName + " Newly baked paths saved to: " + newPathStoragePath);
            }

            // 3. Extract node names from graphData
            Map<Integer, String> nodeNames = new HashMap<>();
            if (objGraphEntity.getGraphData() != null && objGraphEntity.getGraphData().getNode() != null) {
                for (com.workflow.dag_engine.models.graph.Node n : objGraphEntity.getGraphData().getNode()) {
                    if (n.getId() != null) {
                        nodeNames.put(n.getId().intValue(), n.getName());
                    }
                }
            }

            List<PathDTO> paths = pathCm.getAllPaths(nodeNames);
            response.setPaths(paths != null ? paths : new ArrayList<>());
            response.setTotalPaths(paths != null ? paths.size() : 0);

        } catch (ApplicationException a) {
            log.error(methodName + " ApplicationException: " + a.message);
            objErrorDetails.setErrorCode(a.code);
            objErrorDetails.setErrorMessage(a.message);
        } catch (Exception e) {
            log.error(methodName + " Exception: ", e);
            objErrorDetails.setErrorCode("500");
            objErrorDetails.setErrorMessage("Error in calculating paths: " + e.getMessage());
        }

        return response;
    }

}

