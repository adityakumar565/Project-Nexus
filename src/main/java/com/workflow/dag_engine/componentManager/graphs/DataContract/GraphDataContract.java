package com.workflow.dag_engine.componentManager.graphs.DataContract;

/**
 * Data Contract containing dimensional metadata extracted from GraphUploadRequest.
 * Used by ComponentManager and GraphStorage to pre-allocate primitive arrays/buffers
 * with exact sizing before baking data.
 */
public class GraphDataContract {

    private int numNodes;
    private int numEdges;
    private int costDimension;
    private int startNodeId;

    public GraphDataContract() {
    }

    public GraphDataContract(int numNodes, int numEdges, int costDimension, int startNodeId) {
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.costDimension = costDimension;
        this.startNodeId = startNodeId;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getNumEdges() {
        return numEdges;
    }

    public void setNumEdges(int numEdges) {
        this.numEdges = numEdges;
    }

    public int getCostDimension() {
        return costDimension;
    }

    public void setCostDimension(int costDimension) {
        this.costDimension = costDimension;
    }

    public int getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(int startNodeId) {
        this.startNodeId = startNodeId;
    }

    @Override
    public String toString() {
        return "GraphDataContract [numNodes=" + numNodes + ", numEdges=" + numEdges + ", costDimension="
                + costDimension + ", startNodeId=" + startNodeId + "]";
    }

}
