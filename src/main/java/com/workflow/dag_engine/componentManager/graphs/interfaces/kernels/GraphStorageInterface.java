package com.workflow.dag_engine.componentManager.graphs.interfaces.kernels;

public interface GraphStorageInterface {

    /**
     * 
     * A dummy Statefull facade to hide the implementation of the storage container
     * 
     * This is done to have dynamic implementation of the optimized graph storage
     * for traversal
     * 
     * Ex: CSR, Adjency Matrix Etc. It should just provide the query method used by
     * Component Manager
     * 
     * to provide query methods. It stores the optimzied translated version of the
     * GraphUloadRequest
     * 
     **/

    // 1. Basic queries
    public int getNodeCount();

    public int getEdgeCount();

    // 2. Node metadata
    public int getInDegree(int nodeId);

    public int getOutDegree(int nodeId);

    // 3. Navigation
    public int[] getChildren(int nodeId); // successor nodes

    public int[] getParents(int nodeId); // predecessor nodes

    public int[] getOutgoingEdges(int nodeId);

    public int[] getIncomingEdges(int nodeId);

    // 4. Edge details
    public int getSourceNode(int edgeId);

    public int getTargetNode(int edgeId);

    public int getEdgeId(int source, int target); // fast lookup

    // 5. Weights / attributes (pre-extracted)
    public float[] getNodeWeights(int nodeId);

    public float[] getEdgeWeights(int edgeId);

    public float[][] getNodeWeights(int[] nodeIds); // batch

    public float[][] getEdgeWeights(int[] edgeIds); // batch

    // 6. Topology
    public int[] getStartNodes(); // root nodes

    public int[] getEndNodes(); // sink nodes

    // 7. Utilities
    public boolean containsNode(int nodeId);

    public int getGraphSizeDim(); // size of vectors (K)

}
