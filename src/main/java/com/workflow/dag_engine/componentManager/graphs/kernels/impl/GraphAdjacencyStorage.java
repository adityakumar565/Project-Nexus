package com.workflow.dag_engine.componentManager.graphs.kernels.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphStorageInterface;

public class GraphAdjacencyStorage implements GraphStorageInterface {

    private GraphDataContract graphDataContract;
    private Long userId;
    private String graphName;
    private String binaryFilePath;

    // Node mappings: index (0..N-1) <-> nodeId
    private int[] indexToNodeId;
    private Map<Integer, Integer> nodeIdToIndex;

    // Adjacency Matrix: adjMatrix[u][v] = edgeId (or -1 if no edge)
    private int[][] adjMatrix;

    // In/Out degrees
    private int[] inDegree;
    private int[] outDegree;

    // Edge tables: index (0..E-1) <-> edgeId
    private int[] indexToEdgeId;
    private int[] edgeSource;
    private int[] edgeTarget;
    private Map<Integer, Integer> edgeIdToIndex;

    // Cost vectors
    private float[][] nodeWeights; // N x K
    private float[][] edgeWeights; // E x K

    // Sinks
    private int[] sinkNodes;

    public GraphAdjacencyStorage(GraphDataContract graphDataContract) {
        this.graphDataContract = graphDataContract;
        if (graphDataContract != null) {
            int n = graphDataContract.getNumNodes();
            int e = graphDataContract.getNumEdges();
            int k = graphDataContract.getCostDimension();

            this.indexToNodeId = new int[n];
            this.nodeIdToIndex = new HashMap<>(n);

            this.adjMatrix = new int[n][n];
            for (int i = 0; i < n; i++) {
                Arrays.fill(this.adjMatrix[i], -1);
            }

            this.inDegree = new int[n];
            this.outDegree = new int[n];

            this.indexToEdgeId = new int[e];
            this.edgeSource = new int[e];
            this.edgeTarget = new int[e];
            this.edgeIdToIndex = new HashMap<>(e);

            this.nodeWeights = new float[n][k];
            this.edgeWeights = new float[e][k];

            this.sinkNodes = new int[0];
        }
    }

    public GraphDataContract getGraphDataContract() {
        return graphDataContract;
    }

    public void setGraphDataContract(GraphDataContract graphDataContract) {
        this.graphDataContract = graphDataContract;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getBinaryFilePath() {
        return binaryFilePath;
    }

    public void setBinaryFilePath(String binaryFilePath) {
        this.binaryFilePath = binaryFilePath;
    }

    // --- Accessors for Bridge ---

    public int[] getIndexToNodeId() {
        return indexToNodeId;
    }

    public void setIndexToNodeId(int[] indexToNodeId) {
        this.indexToNodeId = indexToNodeId;
    }

    public Map<Integer, Integer> getNodeIdToIndex() {
        return nodeIdToIndex;
    }

    public void setNodeIdToIndex(Map<Integer, Integer> nodeIdToIndex) {
        this.nodeIdToIndex = nodeIdToIndex;
    }

    public int[][] getAdjMatrix() {
        return adjMatrix;
    }

    public void setAdjMatrix(int[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
    }

    public int[] getInDegreeArray() {
        return inDegree;
    }

    public void setInDegreeArray(int[] inDegree) {
        this.inDegree = inDegree;
    }

    public int[] getOutDegreeArray() {
        return outDegree;
    }

    public void setOutDegreeArray(int[] outDegree) {
        this.outDegree = outDegree;
    }

    public int[] getIndexToEdgeId() {
        return indexToEdgeId;
    }

    public void setIndexToEdgeId(int[] indexToEdgeId) {
        this.indexToEdgeId = indexToEdgeId;
    }

    public int[] getEdgeSource() {
        return edgeSource;
    }

    public void setEdgeSource(int[] edgeSource) {
        this.edgeSource = edgeSource;
    }

    public int[] getEdgeTarget() {
        return edgeTarget;
    }

    public void setEdgeTarget(int[] edgeTarget) {
        this.edgeTarget = edgeTarget;
    }

    public Map<Integer, Integer> getEdgeIdToIndex() {
        return edgeIdToIndex;
    }

    public void setEdgeIdToIndex(Map<Integer, Integer> edgeIdToIndex) {
        this.edgeIdToIndex = edgeIdToIndex;
    }

    public float[][] getNodeWeightsArray() {
        return nodeWeights;
    }

    public void setNodeWeightsArray(float[][] nodeWeights) {
        this.nodeWeights = nodeWeights;
    }

    public float[][] getEdgeWeightsArray() {
        return edgeWeights;
    }

    public void setEdgeWeightsArray(float[][] edgeWeights) {
        this.edgeWeights = edgeWeights;
    }

    public void setSinkNodes(int[] sinkNodes) {
        this.sinkNodes = sinkNodes;
    }

    // --- GraphStorageInterface Implementations ---

    @Override
    public int getNodeCount() {
        return graphDataContract != null ? graphDataContract.getNumNodes() : 0;
    }

    @Override
    public int getEdgeCount() {
        return graphDataContract != null ? graphDataContract.getNumEdges() : 0;
    }

    @Override
    public int getInDegree(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId)) {
            return 0;
        }
        int u = nodeIdToIndex.get(nodeId);
        return inDegree != null && u < inDegree.length ? inDegree[u] : 0;
    }

    @Override
    public int getOutDegree(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId)) {
            return 0;
        }
        int u = nodeIdToIndex.get(nodeId);
        return outDegree != null && u < outDegree.length ? outDegree[u] : 0;
    }

    @Override
    public int[] getChildren(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId) || adjMatrix == null) {
            return new int[0];
        }
        int u = nodeIdToIndex.get(nodeId);
        int outDeg = outDegree != null && u < outDegree.length ? outDegree[u] : 0;
        int[] children = new int[outDeg];
        int count = 0;
        for (int v = 0; v < adjMatrix[u].length; v++) {
            if (adjMatrix[u][v] != -1 && count < outDeg) {
                children[count++] = indexToNodeId[v];
            }
        }
        return children;
    }

    @Override
    public int[] getParents(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId) || adjMatrix == null) {
            return new int[0];
        }
        int v = nodeIdToIndex.get(nodeId);
        int inDeg = inDegree != null && v < inDegree.length ? inDegree[v] : 0;
        int[] parents = new int[inDeg];
        int count = 0;
        for (int u = 0; u < adjMatrix.length; u++) {
            if (adjMatrix[u][v] != -1 && count < inDeg) {
                parents[count++] = indexToNodeId[u];
            }
        }
        return parents;
    }

    @Override
    public int[] getOutgoingEdges(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId) || adjMatrix == null) {
            return new int[0];
        }
        int u = nodeIdToIndex.get(nodeId);
        int outDeg = outDegree != null && u < outDegree.length ? outDegree[u] : 0;
        int[] edges = new int[outDeg];
        int count = 0;
        for (int v = 0; v < adjMatrix[u].length; v++) {
            if (adjMatrix[u][v] != -1 && count < outDeg) {
                edges[count++] = adjMatrix[u][v];
            }
        }
        return edges;
    }

    @Override
    public int[] getIncomingEdges(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId) || adjMatrix == null) {
            return new int[0];
        }
        int v = nodeIdToIndex.get(nodeId);
        int inDeg = inDegree != null && v < inDegree.length ? inDegree[v] : 0;
        int[] edges = new int[inDeg];
        int count = 0;
        for (int u = 0; u < adjMatrix.length; u++) {
            if (adjMatrix[u][v] != -1 && count < inDeg) {
                edges[count++] = adjMatrix[u][v];
            }
        }
        return edges;
    }

    @Override
    public int getSourceNode(int edgeId) {
        if (edgeIdToIndex == null || !edgeIdToIndex.containsKey(edgeId)) {
            return -1;
        }
        int eIdx = edgeIdToIndex.get(edgeId);
        return edgeSource != null && eIdx < edgeSource.length ? edgeSource[eIdx] : -1;
    }

    @Override
    public int getTargetNode(int edgeId) {
        if (edgeIdToIndex == null || !edgeIdToIndex.containsKey(edgeId)) {
            return -1;
        }
        int eIdx = edgeIdToIndex.get(edgeId);
        return edgeTarget != null && eIdx < edgeTarget.length ? edgeTarget[eIdx] : -1;
    }

    @Override
    public int getEdgeId(int source, int target) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(source) || !nodeIdToIndex.containsKey(target)
                || adjMatrix == null) {
            return -1;
        }
        int u = nodeIdToIndex.get(source);
        int v = nodeIdToIndex.get(target);
        return adjMatrix[u][v];
    }

    @Override
    public float[] getNodeWeights(int nodeId) {
        if (nodeIdToIndex == null || !nodeIdToIndex.containsKey(nodeId) || nodeWeights == null) {
            return new float[getGraphSizeDim()];
        }
        int u = nodeIdToIndex.get(nodeId);
        return nodeWeights[u];
    }

    @Override
    public float[] getEdgeWeights(int edgeId) {
        if (edgeIdToIndex == null || !edgeIdToIndex.containsKey(edgeId) || edgeWeights == null) {
            return new float[getGraphSizeDim()];
        }
        int eIdx = edgeIdToIndex.get(edgeId);
        return edgeWeights[eIdx];
    }

    @Override
    public float[][] getNodeWeights(int[] nodeIds) {
        if (nodeIds == null) {
            return new float[0][0];
        }
        float[][] batch = new float[nodeIds.length][];
        for (int i = 0; i < nodeIds.length; i++) {
            batch[i] = getNodeWeights(nodeIds[i]);
        }
        return batch;
    }

    @Override
    public float[][] getEdgeWeights(int[] edgeIds) {
        if (edgeIds == null) {
            return new float[0][0];
        }
        float[][] batch = new float[edgeIds.length][];
        for (int i = 0; i < edgeIds.length; i++) {
            batch[i] = getEdgeWeights(edgeIds[i]);
        }
        return batch;
    }

    @Override
    public int[] getStartNodes() {
        return graphDataContract != null ? new int[] { graphDataContract.getStartNodeId() } : new int[0];
    }

    @Override
    public int[] getEndNodes() {
        return sinkNodes != null ? sinkNodes : new int[0];
    }

    @Override
    public boolean containsNode(int nodeId) {
        return nodeIdToIndex != null && nodeIdToIndex.containsKey(nodeId);
    }

    @Override
    public int getGraphSizeDim() {
        return graphDataContract != null ? graphDataContract.getCostDimension() : 0;
    }

}
