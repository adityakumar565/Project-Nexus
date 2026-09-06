package com.workflow.dag_engine.componentManager.paths.component_manager;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathBridgeInterface;
import com.workflow.dag_engine.componentManager.paths.kernels.impl.PathBridgeImpl;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.interfaces.componentManager.PathComponentManagerInterface;
import com.workflow.dag_engine.interfaces.componentManager.PathCostComparator;
import com.workflow.dag_engine.models.path.PathDTO;

public class PathComponentManagerImpl implements PathComponentManagerInterface {

    private final GraphComponentManagerInterface graphCm;
    private final PathBridgeInterface pathBridge;

    public PathComponentManagerImpl(GraphComponentManagerInterface graphCm, PathBridgeInterface pathBridge) {
        this(graphCm, pathBridge, true);
    }

    public PathComponentManagerImpl(GraphComponentManagerInterface graphCm, PathBridgeInterface pathBridge, boolean autoBake) {
        this.graphCm = graphCm;
        this.pathBridge = pathBridge;
        
        if (autoBake) {
            this.pathBridge.bake(graphCm);
        }
    }

    @Override
    public List<PathDTO> getAllPaths() {
        return getAllPaths(null);
    }

    @Override
    public List<PathDTO> getAllPaths(Map<Integer, String> nodeNames) {
        return pathBridge.sync(graphCm.getCostNames(), nodeNames);
    }

    @Override
    public PathDTO getLeastCostPathByCost(String costName) {
        if (graphCm == null || graphCm.getCostNames() == null) return null;
        
        int costIndex = graphCm.getCostNames().indexOf(costName);
        if (costIndex == -1) {
            throw new IllegalArgumentException("Unknown cost dimension name: " + costName);
        }

        if (!(pathBridge instanceof PathBridgeImpl)) {
            throw new UnsupportedOperationException("Bridge does not support direct minimum extraction.");
        }
        
        PathBridgeImpl bridgeImpl = (PathBridgeImpl) pathBridge;
        if (bridgeImpl.getActiveStorage() == null) return null;
        
        int[] rawPath = bridgeImpl.getActiveStorage().getMinCostPathByCostID(costIndex);
        if (rawPath == null || rawPath.length == 0) return null;
        
        // Recalculate raw costs for the final leaf or map it.
        // Wait, activeStorage doesn't return the raw costs directly for a specific path ID
        // But since we just want to parse it, we can fetch all costs or we should just get the DTO by syncing everything and sorting.
        // Actually, syncing everything and sorting is easier, but activeStorage provides getMinCostPathByCostID
        // We need the rawCosts to syncPath... Let's just find it in the sync list or modify bridge to accept leaf ID.
        // For simplicity and to use the exact path:
        List<PathDTO> allDTOs = getAllPaths();
        PathDTO minPath = null;
        float minCost = Float.MAX_VALUE;
        for (PathDTO dto : allDTOs) {
            Float cost = dto.getPathCosts().get(costName);
            if (cost != null && cost < minCost) {
                minCost = cost;
                minPath = dto;
            }
        }
        return minPath;
    }

    @Override
    public PathDTO getMostCostPathByCost(String costName) {
        if (graphCm == null || graphCm.getCostNames() == null) return null;
        
        int costIndex = graphCm.getCostNames().indexOf(costName);
        if (costIndex == -1) {
            throw new IllegalArgumentException("Unknown cost dimension name: " + costName);
        }

        List<PathDTO> allDTOs = getAllPaths();
        PathDTO maxPath = null;
        float maxCost = Float.NEGATIVE_INFINITY;
        for (PathDTO dto : allDTOs) {
            Float cost = dto.getPathCosts().get(costName);
            if (cost != null && cost > maxCost) {
                maxCost = cost;
                maxPath = dto;
            }
        }
        return maxPath;
    }

    @Override
    public void prunePaths(PathCostComparator comparator, String costName, float costValue) {
        // Pruning is not required for now as per user feedback
    }

    @Override
    public String storePaths(String graphName) {
        return pathBridge.save(pathBridge.getActiveStorage(), graphName);
    }

    @Override
    public void loadPaths(String binaryFilePath) {
        this.pathBridge.load(binaryFilePath);
    }
}

