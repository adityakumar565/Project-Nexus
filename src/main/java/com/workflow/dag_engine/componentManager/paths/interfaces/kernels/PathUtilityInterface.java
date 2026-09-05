package com.workflow.dag_engine.componentManager.paths.interfaces.kernels;

import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;

/**
 * Utility Kernel Interface for Path Component Manager.
 * Encapsulates purely algorithmic traversal logic.
 */
public interface PathUtilityInterface {

    /**
     * Traverses the graph via the GraphComponentManagerInterface (e.g., using BFS or DFS)
     * and constructs the paths. The results are fed into the provided mutable storage implementation.
     * 
     * @param graphCm The Graph Component Manager providing graph access.
     * @param mutableStorage The specific storage implementation (castable internally to the implementation type).
     */
    void traverseGraph(GraphComponentManagerInterface graphCm, Object mutableStorage);

}
