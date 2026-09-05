package com.workflow.dag_engine.interfaces.componentManager;

import com.workflow.dag_engine.models.enums.ImplementationType;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

/**
 * Facade interface for DAG Component Manager.
 * Operates on Single-Source Directed Acyclic Graphs (DAGs).
 * Hides low-level binary layout (CSR, Matrix, Memory-Mapped buffers)
 * and provides primitive-based traversal and cost operations.
 */
public interface GraphComponentManagerInterface {

    /**
     * Returns the underlying implementation type of this Component Manager.
     */
    public ImplementationType getImplementationType();

    // ==========================================
    // Lifecycle & Storage
    // ==========================================

    /**
     * Transforms GraphUploadRequest into optimized binary layout (e.g. CSR)
     * and saves to server storage.
     * 
     * @param graphUploadRequest incoming graph payload
     * @return path to the persisted binary file
     */
    public String transformAndStoreGraph(GraphUploadRequest graphUploadRequest);

    /**
     * Loads a binary graph into memory / memory-mapped buffer for execution.
     * 
     * @param binaryFilePath path to the binary file
     */
    public void loadGraph(String binaryFilePath);

    /**
     * Releases mapped memory buffers and file handles.
     */
    public void unload();

    // ==========================================
    // Graph Metadata & Bounds
    // ==========================================

    /**
     * Returns the single entry-point / root node ID.
     */
    public int getStartNodeId();

    public int getNumberOfNodes();

    public int getNumberOfEdges();

    public int getCostDimension();

    public boolean containsNode(int nodeId);

    /**
     * Returns all terminal / sink nodes (nodes with out-degree = 0).
     */
    public int[] getSinkNodes();

    // ==========================================
    // Graph Traversal & Topology
    // ==========================================

    /**
     * In-degree query (number of incoming dependencies).
     * Essential for Kahn's topological sort and dependency checking without memory allocation.
     */
    public int getInDegree(int nodeId);

    /**
     * Out-degree query (number of downstream branches).
     */
    public int getOutDegree(int nodeId);

    // Child (successor) nodes
    public int[] getChildren(int nodeId);

    // Parent (predecessor) nodes
    public int[] getParents(int nodeId);

    // Outgoing edge IDs
    public int[] getOutgoingEdges(int nodeId);

    // Incoming edge IDs
    public int[] getIncomingEdges(int nodeId);

    // Edge Endpoints
    public int getSourceNode(int edgeId);

    public int getTargetNode(int edgeId);

    public int getEdgeId(int sourceNodeId, int targetNodeId);

    // ==========================================
    // Node & Edge Weights (Cost Vectors)
    // ==========================================

    // Node cost vector
    public float[] getNodeWeight(int nodeId);

    // Edge cost vector by edgeId
    public float[] getEdgeWeight(int edgeId);

    // Edge cost vector by source/target pair
    public float[] getEdgeWeight(int sourceNodeId, int targetNodeId);

    // Batch queries for SIMD / vector path calculations
    public float[][] getNodeWeights(int[] nodeIds);

    public float[][] getEdgeWeights(int[] edgeIds);

    // Contiguous Flat 1D Arrays (for high performance / zero-copy traversal)
    public float[] getFlatNodeCosts();

    public float[] getFlatEdgeCosts();

    // Specific dimension cost lookup
    public float getNodeCost(int nodeId, int dimension);

    public float getEdgeCost(int sourceNodeId, int targetNodeId, int dimension);

}
