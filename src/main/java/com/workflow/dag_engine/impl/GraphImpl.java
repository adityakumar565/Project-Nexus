package com.workflow.dag_engine.impl;

import java.util.ArrayList;
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
import com.workflow.dag_engine.models.graph.UserGraphRequest;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
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
            if (graphMetaData == null || graphMetaData.getGraphId() == null) {
                throw new ApplicationException("2001", "Invalid GraphMetaData or graphId");
            }

            Long graphId = graphMetaData.getGraphId();

            GraphEntity objGraphEntity = objGraphRepository.findById(graphId).orElse(null);
            if (objGraphEntity == null) {
                throw new ApplicationException("2002", "Error in loading graph Or Graph does not Exists for the user");
            }

            String storagePath = objGraphEntity.getBinaryFilePath();
            GraphUploadRequest graphUpload = objGraphEntity.getGraphData();
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

            // 5. Check if graph is currently loaded in memory
            if (inMemoryGraphs.containsKey(graphId)) {
                throw new ApplicationException("2005",
                        "Cannot delete graph: Graph is currently active in memory. Please close the graph first.");
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
                        graphList.add(entity.getGraphData());
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
    public GraphResponse updateGraph(GraphUploadRequest graphUpdateRequest) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGraph'");
    }

}
