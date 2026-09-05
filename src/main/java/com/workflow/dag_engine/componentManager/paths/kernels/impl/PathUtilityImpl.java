package com.workflow.dag_engine.componentManager.paths.kernels.impl;

import java.util.Stack;

import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.paths.interfaces.kernels.PathUtilityInterface;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;

@Component("pathUtility")
public class PathUtilityImpl implements PathUtilityInterface {

    private static class TraversalState {
        int graphNodeId;
        int parentTreeId;
        float[] cumulativeCosts;

        TraversalState(int graphNodeId, int parentTreeId, float[] cumulativeCosts) {
            this.graphNodeId = graphNodeId;
            this.parentTreeId = parentTreeId;
            this.cumulativeCosts = cumulativeCosts;
        }
    }

    @Override
    public void traverseGraph(GraphComponentManagerInterface graphCm, Object mutableStorage) {
        if (!(mutableStorage instanceof TreePathComponentStorage)) {
            throw new IllegalArgumentException("Unsupported storage type. Expected TreePathComponentStorage.");
        }

        TreePathComponentStorage storage = (TreePathComponentStorage) mutableStorage;
        
        int startNodeId = graphCm.getStartNodeId();
        int costDimension = graphCm.getCostDimension();
        
        if (graphCm.getNumberOfNodes() == 0 || !graphCm.containsNode(startNodeId)) {
            return;
        }

        Stack<TraversalState> stack = new Stack<>();
        
        float[] startCosts = new float[costDimension];
        for (int d = 0; d < costDimension; d++) {
            startCosts[d] = graphCm.getNodeCost(startNodeId, d);
        }
        
        stack.push(new TraversalState(startNodeId, -1, startCosts));

        while (!stack.isEmpty()) {
            TraversalState current = stack.pop();
            
            // Add to storage tree
            int treeId = storage.addNode(current.parentTreeId, current.graphNodeId, current.cumulativeCosts);
            
            int[] children = graphCm.getChildren(current.graphNodeId);
            if (children == null || children.length == 0) {
                // Leaf node -> path is complete
                storage.addLeaf(treeId);
            } else {
                for (int childId : children) {
                    float[] nextCosts = new float[costDimension];
                    for (int d = 0; d < costDimension; d++) {
                        // Cumulative Cost = Parent Cumulative + Edge Cost + Child Node Cost
                        float edgeCost = graphCm.getEdgeCost(current.graphNodeId, childId, d);
                        float nodeCost = graphCm.getNodeCost(childId, d);
                        nextCosts[d] = current.cumulativeCosts[d] + edgeCost + nodeCost;
                    }
                    stack.push(new TraversalState(childId, treeId, nextCosts));
                }
            }
        }
    }
}
