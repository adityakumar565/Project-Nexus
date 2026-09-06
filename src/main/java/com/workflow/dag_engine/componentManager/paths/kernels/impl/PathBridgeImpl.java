package com.workflow.dag_engine.componentManager.paths.kernels.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathBridgeInterface;
import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathStorageInterface;
import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathUtilityInterface;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.models.graph.Node;
import com.workflow.dag_engine.models.path.PathDTO;

@Component("pathBridge")
public class PathBridgeImpl implements PathBridgeInterface {

    private final PathUtilityInterface pathUtility;
    private PathStorageInterface activeStorage;

    public PathBridgeImpl(@Qualifier("pathUtility") PathUtilityInterface pathUtility) {
        this.pathUtility = pathUtility;
    }

    public PathStorageInterface getActiveStorage() {
        return activeStorage;
    }

    @Override
    public void bake(GraphComponentManagerInterface graphCm) {
        if (graphCm == null) return;
        
        // Use a reasonable initial capacity based on graph size, or fallback to default
        int initialCapacity = Math.max(1024, graphCm.getNumberOfNodes() * 2);
        TreePathComponentStorage storage = new TreePathComponentStorage(initialCapacity, graphCm.getCostDimension());
        
        pathUtility.traverseGraph(graphCm, storage);
        
        this.activeStorage = storage;
    }

    @Override
    public List<PathDTO> sync(List<String> costNames) {
        return sync(costNames, null);
    }

    @Override
    public List<PathDTO> sync(List<String> costNames, Map<Integer, String> nodeNames) {
        if (activeStorage == null) {
            return new ArrayList<>();
        }
        
        int[][] paths = activeStorage.getAllPaths();
        float[][] costs = activeStorage.getAllCosts();
        
        List<PathDTO> result = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            result.add(syncPath(paths[i], costs[i], costNames, nodeNames));
        }
        return result;
    }

    @Override
    public PathDTO syncPath(int[] rawPathSequence, float[] rawCosts, List<String> costNames) {
        return syncPath(rawPathSequence, rawCosts, costNames, null);
    }

    @Override
    public PathDTO syncPath(int[] rawPathSequence, float[] rawCosts, List<String> costNames, Map<Integer, String> nodeNames) {
        if (rawPathSequence == null) return null;
        
        List<Node> seq = new ArrayList<>(rawPathSequence.length);
        for (int id : rawPathSequence) {
            String name = (nodeNames != null && nodeNames.containsKey(id)) ? nodeNames.get(id) : ("Node_" + id);
            seq.add(new Node((long) id, name));
        }
        
        Map<String, Float> costMap = new HashMap<>();
        if (costNames != null && rawCosts != null) {
            int limit = Math.min(costNames.size(), rawCosts.length);
            for (int i = 0; i < limit; i++) {
                costMap.put(costNames.get(i), rawCosts[i]);
            }
        }
        
        return new PathDTO(seq, costMap);
    }

    @Override
    public String save(PathStorageInterface storage, String graphName) {
        if (storage == null) {
            throw new IllegalArgumentException("Cannot save null storage.");
        }
        
        File dir = new File("path_representation");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String safeName = graphName != null ? graphName.replaceAll("[^a-zA-Z0-9.-]", "_") : "unknown";
        String filePath = dir.getAbsolutePath() + File.separator + safeName + "_paths_" + System.currentTimeMillis() + ".dat";
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(storage);
            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save path storage to file: " + filePath, e);
        }
    }

    @Override
    public PathStorageInterface load(String binaryFilePath) {
        if (binaryFilePath == null || binaryFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid binary file path provided.");
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(binaryFilePath))) {
            Object obj = ois.readObject();
            if (obj instanceof PathStorageInterface) {
                PathStorageInterface loaded = (PathStorageInterface) obj;
                this.activeStorage = loaded;
                return loaded;
            } else {
                throw new RuntimeException("Loaded object is not a PathStorageInterface");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load path storage from file: " + binaryFilePath, e);
        }
    }
}
