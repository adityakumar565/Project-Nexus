package com.workflow.dag_engine.componentManager.graphs.kernels.impl.AdjencyMatrixImplementation;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphStorageInterface;

public class GraphAdjacencyStorage implements GraphStorageInterface {

    private GraphDataContract graphDataContract;

    public GraphAdjacencyStorage(GraphDataContract graphDataContract) {
        this.graphDataContract = graphDataContract;
        // Pre-allocate memory based on contract (e.g. Adjacency Matrix arrays, weights)
    }

    public GraphDataContract getGraphDataContract() {
        return graphDataContract;
    }

    @Override
    public int getNodeCount() {
        return graphDataContract != null ? graphDataContract.getNumNodes() : 0;
    }

    @Override
    public int getEdgeCount() {
        return graphDataContract != null ? graphDataContract.getNumEdges() : 0;
    }

    @Override
    public int getInDegree(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getInDegree'");
    }

    @Override
    public int getOutDegree(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getOutDegree'");
    }

    @Override
    public int[] getChildren(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getChildren'");
    }

    @Override
    public int[] getParents(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getParents'");
    }

    @Override
    public int[] getOutgoingEdges(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getOutgoingEdges'");
    }

    @Override
    public int[] getIncomingEdges(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getIncomingEdges'");
    }

    @Override
    public int getSourceNode(int edgeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getSourceNode'");
    }

    @Override
    public int getTargetNode(int edgeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getTargetNode'");
    }

    @Override
    public int getEdgeId(int source, int target) {
        throw new UnsupportedOperationException("Unimplemented method 'getEdgeId'");
    }

    @Override
    public float[] getNodeWeights(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getNodeWeights'");
    }

    @Override
    public float[] getEdgeWeights(int edgeId) {
        throw new UnsupportedOperationException("Unimplemented method 'getEdgeWeights'");
    }

    @Override
    public float[][] getNodeWeights(int[] nodeIds) {
        throw new UnsupportedOperationException("Unimplemented method 'getNodeWeights'");
    }

    @Override
    public float[][] getEdgeWeights(int[] edgeIds) {
        throw new UnsupportedOperationException("Unimplemented method 'getEdgeWeights'");
    }

    @Override
    public int[] getStartNodes() {
        return graphDataContract != null ? new int[] { graphDataContract.getStartNodeId() } : new int[0];
    }

    @Override
    public int[] getEndNodes() {
        throw new UnsupportedOperationException("Unimplemented method 'getEndNodes'");
    }

    @Override
    public boolean containsNode(int nodeId) {
        throw new UnsupportedOperationException("Unimplemented method 'containsNode'");
    }

    @Override
    public int getGraphSizeDim() {
        return graphDataContract != null ? graphDataContract.getCostDimension() : 0;
    }

}
