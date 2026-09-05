package com.workflow.dag_engine.interfaces.componentManager;

import com.workflow.dag_engine.models.path.PathDTO;
import java.util.List;

public interface PathComponentManagerInterface {

    /**
     * Retrieves all valid paths in the graph.
     * @return A list of PathDTO objects containing node sequences and their associated costs.
     */
    List<PathDTO> getAllPaths();

    /**
     * Retrieves the path with the least cost for a specific cost component.
     * @param costName The name of the cost dimension.
     * @return The PathDTO with the lowest cost for the given dimension.
     */
    PathDTO getLeastCostPathByCost(String costName);

    /**
     * Retrieves the path with the most cost for a specific cost component.
     * @param costName The name of the cost dimension.
     * @return The PathDTO with the highest cost for the given dimension.
     */
    PathDTO getMostCostPathByCost(String costName);

    /**
     * Deletes all paths that match the specified condition.
     * @param comparator The comparison operator (e.g., LESS_THAN, GREATER_THAN).
     * @param costName The name of the cost dimension to check.
     * @param costValue The value to compare against.
     */
    void prunePaths(PathCostComparator comparator, String costName, float costValue);

    /**
     * Stores the currently baked paths into a binary file.
     * @param graphName The name of the graph to be used in the file name.
     * @return The binary file path where the paths are stored.
     */
    String storePaths(String graphName);
}
