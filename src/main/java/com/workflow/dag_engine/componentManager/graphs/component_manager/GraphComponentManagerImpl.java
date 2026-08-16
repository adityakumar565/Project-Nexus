package com.workflow.dag_engine.componentManager.graphs.component_manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphBridgeInterface;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphStorageInterface;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphUtilityInterface;
import com.workflow.dag_engine.componentManager.graphs.kernels.impl.GraphAdjacencyStorage;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.models.enums.ImplementationType;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

public class GraphComponentManagerImpl implements GraphComponentManagerInterface {

    private static final Logger log = LoggerFactory.getLogger(GraphComponentManagerImpl.class);

    private final GraphBridgeInterface graphBridge;
    private final GraphUtilityInterface graphUtility;

    private GraphStorageInterface activeStorage;

    public GraphComponentManagerImpl(
            GraphBridgeInterface graphBridge,
            GraphUtilityInterface graphUtility) {
        this.graphBridge = graphBridge;
        this.graphUtility = graphUtility;
    }

    @Override
    public ImplementationType getImplementationType() {
        return graphBridge != null ? graphBridge.getImplementationType() : ImplementationType.ADJACENCY_V1;
    }

    public GraphStorageInterface getActiveStorage() {
        return activeStorage;
    }

    public void setActiveStorage(GraphStorageInterface activeStorage) {
        this.activeStorage = activeStorage;
    }

    @Override
    public String transformAndStoreGraph(GraphUploadRequest graphUploadRequest) {
        if (graphUploadRequest == null) {
            throw new IllegalArgumentException("GraphUploadRequest cannot be null");
        }

        log.info("Transforming and storing graph: {}", graphUploadRequest.getGraphName());

        // 1. Extract Data Contract
        GraphDataContract contract = graphUtility.extractDataContract(graphUploadRequest);

        // 2. Pre-allocate memory in Adjacency Storage
        GraphStorageInterface storage = new GraphAdjacencyStorage(contract);

        // 3. Bake data into storage
        graphBridge.bake(graphUploadRequest, storage);

        // 4. Save to binary file
        String savedPath = graphBridge.save(storage);

        // 5. Set active storage
        this.activeStorage = storage;

        log.info("Graph '{}' saved to: {}", graphUploadRequest.getGraphName(), savedPath);
        return savedPath;
    }

    @Override
    public void loadGraph(String binaryFilePath) {
        log.info("Loading binary graph from: {}", binaryFilePath);
        this.activeStorage = graphBridge.load(binaryFilePath);
    }

    @Override
    public void unload() {
        log.info("Unloading active graph storage");
        this.activeStorage = null;
    }

    @Override
    public int getStartNodeId() {
        if (activeStorage != null && activeStorage.getStartNodes() != null && activeStorage.getStartNodes().length > 0) {
            return activeStorage.getStartNodes()[0];
        }
        return 0;
    }

    @Override
    public int getNumberOfNodes() {
        return activeStorage != null ? activeStorage.getNodeCount() : 0;
    }

    @Override
    public int getNumberOfEdges() {
        return activeStorage != null ? activeStorage.getEdgeCount() : 0;
    }

    @Override
    public int getCostDimension() {
        return activeStorage != null ? activeStorage.getGraphSizeDim() : 0;
    }

    @Override
    public boolean containsNode(int nodeId) {
        return activeStorage != null && activeStorage.containsNode(nodeId);
    }

    @Override
    public int[] getSinkNodes() {
        return activeStorage != null ? activeStorage.getEndNodes() : new int[0];
    }

    @Override
    public int getInDegree(int nodeId) {
        return activeStorage != null ? activeStorage.getInDegree(nodeId) : 0;
    }

    @Override
    public int getOutDegree(int nodeId) {
        return activeStorage != null ? activeStorage.getOutDegree(nodeId) : 0;
    }

    @Override
    public int[] getChildren(int nodeId) {
        return activeStorage != null ? activeStorage.getChildren(nodeId) : new int[0];
    }

    @Override
    public int[] getParents(int nodeId) {
        return activeStorage != null ? activeStorage.getParents(nodeId) : new int[0];
    }

    @Override
    public int[] getOutgoingEdges(int nodeId) {
        return activeStorage != null ? activeStorage.getOutgoingEdges(nodeId) : new int[0];
    }

    @Override
    public int[] getIncomingEdges(int nodeId) {
        return activeStorage != null ? activeStorage.getIncomingEdges(nodeId) : new int[0];
    }

    @Override
    public int getSourceNode(int edgeId) {
        return activeStorage != null ? activeStorage.getSourceNode(edgeId) : -1;
    }

    @Override
    public int getTargetNode(int edgeId) {
        return activeStorage != null ? activeStorage.getTargetNode(edgeId) : -1;
    }

    @Override
    public int getEdgeId(int sourceNodeId, int targetNodeId) {
        return activeStorage != null ? activeStorage.getEdgeId(sourceNodeId, targetNodeId) : -1;
    }

    @Override
    public float[] getNodeWeight(int nodeId) {
        return activeStorage != null ? activeStorage.getNodeWeights(nodeId) : new float[0];
    }

    @Override
    public float[] getEdgeWeight(int edgeId) {
        return activeStorage != null ? activeStorage.getEdgeWeights(edgeId) : new float[0];
    }

    @Override
    public float[] getEdgeWeight(int sourceNodeId, int targetNodeId) {
        int edgeId = getEdgeId(sourceNodeId, targetNodeId);
        if (edgeId != -1) {
            return getEdgeWeight(edgeId);
        }
        return new float[getCostDimension()];
    }

    @Override
    public float[][] getNodeWeights(int[] nodeIds) {
        return activeStorage != null ? activeStorage.getNodeWeights(nodeIds) : new float[0][0];
    }

    @Override
    public float[][] getEdgeWeights(int[] edgeIds) {
        return activeStorage != null ? activeStorage.getEdgeWeights(edgeIds) : new float[0][0];
    }

}
