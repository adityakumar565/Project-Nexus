package com.workflow.dag_engine.componentManager.paths.interfaces.kernels;

import java.util.List;
import java.util.Map;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.models.path.PathDTO;

/**
 * Bridge Kernel Interface for Path Component Manager.
 * Orchestrates the traversal (using Utility) and manages the transformation
 * between raw generic storage (PathStorageInterface) and the UI-friendly PathDTO structures.
 */
public interface PathBridgeInterface {

    /**
     * Retrieves the active path storage currently held by the bridge.
     * @return The active PathStorageInterface.
     */
    PathStorageInterface getActiveStorage();

    /**
     * Orchestrates the building phase. Calls the Utility implementation to parse and traverse
     * the graph, and populates the internal storage with the generated paths.
     * 
     * @param graphCm The Graph Component Manager providing the source graph data.
     */
    void bake(GraphComponentManagerInterface graphCm);

    /**
     * Reads all paths from the internal populated storage and parses them into PathDTO structures.
     * 
     * @param costNames The ordered list of cost dimension names from the Graph.
     * @return A list of PathDTOs representing all valid traversal paths.
     */
    List<PathDTO> sync(List<String> costNames);

    /**
     * Reads all paths from the internal populated storage and parses them into PathDTO structures with node names.
     * 
     * @param costNames The ordered list of cost dimension names from the Graph.
     * @param nodeNames Map of node ID to node name.
     * @return A list of PathDTOs representing all valid traversal paths.
     */
    List<PathDTO> sync(List<String> costNames, Map<Integer, String> nodeNames);

    /**
     * Converts a specific raw path sequence and its corresponding costs into a PathDTO.
     * 
     * @param rawPathSequence The array of graph node IDs for the path.
     * @param rawCosts The cost array mapped by dimension.
     * @param costNames The ordered list of cost dimension names.
     * @return The constructed PathDTO.
     */
    PathDTO syncPath(int[] rawPathSequence, float[] rawCosts, List<String> costNames);

    /**
     * Converts a specific raw path sequence and its corresponding costs into a PathDTO with node names.
     * 
     * @param rawPathSequence The array of graph node IDs for the path.
     * @param rawCosts The cost array mapped by dimension.
     * @param costNames The ordered list of cost dimension names.
     * @param nodeNames Map of node ID to node name.
     * @return The constructed PathDTO.
     */
    PathDTO syncPath(int[] rawPathSequence, float[] rawCosts, List<String> costNames, Map<Integer, String> nodeNames);

    /**
     * Saves the provided storage to a binary file and returns the file path.
     * 
     * @param storage The storage to save.
     * @param graphName The graph name used for generating the file name.
     * @return The saved binary file path.
     */
    String save(PathStorageInterface storage, String graphName);

    /**
     * Loads the storage from a binary file.
     * 
     * @param binaryFilePath The path to load from.
     * @return The loaded storage interface.
     */
    PathStorageInterface load(String binaryFilePath);

}
