package com.workflow.dag_engine.componentManager.paths.interfaces.kernels;

/**
 * Storage Kernel Interface for Path Component Manager.
 * Operates purely as a query-focused generic interface hiding the internal storage structure 
 * (such as Tree or Flat array implementations). It is populated once by the Utility/Bridge.
 */
public interface PathStorageInterface {

    /**
     * Retrieves all constructed paths in the graph.
     * @return A 2D array where each row represents a sequence of graph node IDs for a single path.
     */
    int[][] getAllPaths();

    /**
     * Retrieves all cumulative costs for each path.
     * @return A 2D array where each row represents the cumulative costs array for the corresponding path in getAllPaths().
     */
    float[][] getAllCosts();

    /**
     * Retrieves the path sequence with the minimum cost for a specific cost dimension index.
     * @param costIndex The integer index of the cost dimension.
     * @return An array of graph node IDs representing the least cost path.
     */
    int[] getMinCostPathByCostID(int costIndex);

    /**
     * Retrieves the path sequence with the maximum cost for a specific cost dimension index.
     * @param costIndex The integer index of the cost dimension.
     * @return An array of graph node IDs representing the most cost path.
     */
    int[] getMaxCostPathByCostID(int costIndex);

}
