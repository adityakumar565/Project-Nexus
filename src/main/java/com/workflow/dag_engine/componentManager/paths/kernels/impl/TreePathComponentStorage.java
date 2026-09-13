package com.workflow.dag_engine.componentManager.paths.kernels.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathStorageInterface;

/**
 * Implementation of PathStorageInterface using a primitive-array-backed Prefix Tree.
 * 
 * Memory Layout:
 * Each node in the traversal tree is assigned a unique incremental treeNodeId.
 * - graphNodeIds[treeNodeId] = the actual graph node ID
 * - parentIds[treeNodeId] = the parent treeNodeId in the path
 * - cumulativeCosts[treeNodeId][dim] = cumulative cost from start to this node for cost dimension 'dim'
 */
public class TreePathComponentStorage implements PathStorageInterface, Serializable {
    private static final long serialVersionUID = 1L;

    private int[] graphNodeIds;
    private int[] parentIds;
    private float[][] cumulativeCosts;
    
    private int size;
    private int costDimension;
    
    private List<Integer> leafIds;

    public TreePathComponentStorage(int initialCapacity, int costDimension) {
        this.graphNodeIds = new int[initialCapacity];
        this.parentIds = new int[initialCapacity];
        this.cumulativeCosts = new float[initialCapacity][costDimension];
        this.size = 0;
        this.costDimension = costDimension;
        this.leafIds = new ArrayList<>();
    }

    /**
     * Mutable method for the Utility kernel to build the tree.
     * 
     * @param parentTreeId The tree ID of the parent node. -1 if this is the root.
     * @param graphNodeId The actual graph node ID.
     * @param costs The cumulative costs up to this node.
     * @return The newly assigned tree ID.
     */
    public int addNode(int parentTreeId, int graphNodeId, float[] costs) {
        if (size >= graphNodeIds.length) {
            expandCapacity();
        }
        
        int treeId = size++;
        graphNodeIds[treeId] = graphNodeId;
        parentIds[treeId] = parentTreeId;
        
        if (costs != null && costs.length == costDimension) {
            System.arraycopy(costs, 0, cumulativeCosts[treeId], 0, costDimension);
        }

        return treeId;
    }
    
    /**
     * Mutable method to register a leaf node (i.e., a complete path).
     */
    public void addLeaf(int treeId) {
        this.leafIds.add(treeId);
    }

    /**
     * Checks whether a graphNodeId is already an ancestor along the path branch ending at treeId.
     */
    public boolean containsAncestor(int treeId, int graphNodeId) {
        int curr = treeId;
        while (curr != -1 && curr < size) {
            if (graphNodeIds[curr] == graphNodeId) {
                return true;
            }
            curr = parentIds[curr];
        }
        return false;
    }

    private void expandCapacity() {
        int newCap = graphNodeIds.length * 2;
        graphNodeIds = Arrays.copyOf(graphNodeIds, newCap);
        parentIds = Arrays.copyOf(parentIds, newCap);
        cumulativeCosts = Arrays.copyOf(cumulativeCosts, newCap);
        for (int i = size; i < newCap; i++) {
            cumulativeCosts[i] = new float[costDimension];
        }
    }

    // --- Internal Helpers to resolve paths ---

    private int[] buildPathSequence(int leafTreeId) {
        // Count length
        int length = 0;
        int curr = leafTreeId;
        while (curr != -1) {
            length++;
            curr = parentIds[curr];
        }
        
        int[] seq = new int[length];
        curr = leafTreeId;
        for (int i = length - 1; i >= 0; i--) {
            seq[i] = graphNodeIds[curr];
            curr = parentIds[curr];
        }
        return seq;
    }

    // --- PathStorageInterface Implementation ---

    @Override
    public int[][] getAllPaths() {
        int numPaths = leafIds.size();
        int[][] paths = new int[numPaths][];
        for (int i = 0; i < numPaths; i++) {
            paths[i] = buildPathSequence(leafIds.get(i));
        }
        return paths;
    }

    @Override
    public float[][] getAllCosts() {
        int numPaths = leafIds.size();
        float[][] costs = new float[numPaths][costDimension];
        for (int i = 0; i < numPaths; i++) {
            int leafTreeId = leafIds.get(i);
            System.arraycopy(cumulativeCosts[leafTreeId], 0, costs[i], 0, costDimension);
        }
        return costs;
    }

    @Override
    public int[] getMinCostPathByCostID(int costIndex) {
        if (leafIds.isEmpty()) return new int[0];
        
        int bestLeaf = leafIds.get(0);
        float minCost = cumulativeCosts[bestLeaf][costIndex];
        
        for (int i = 1; i < leafIds.size(); i++) {
            int leaf = leafIds.get(i);
            float cost = cumulativeCosts[leaf][costIndex];
            if (cost < minCost) {
                minCost = cost;
                bestLeaf = leaf;
            }
        }
        
        return buildPathSequence(bestLeaf);
    }

    @Override
    public int[] getMaxCostPathByCostID(int costIndex) {
        if (leafIds.isEmpty()) return new int[0];
        
        int bestLeaf = leafIds.get(0);
        float maxCost = cumulativeCosts[bestLeaf][costIndex];
        
        for (int i = 1; i < leafIds.size(); i++) {
            int leaf = leafIds.get(i);
            float cost = cumulativeCosts[leaf][costIndex];
            if (cost > maxCost) {
                maxCost = cost;
                bestLeaf = leaf;
            }
        }
        
        return buildPathSequence(bestLeaf);
    }
}
