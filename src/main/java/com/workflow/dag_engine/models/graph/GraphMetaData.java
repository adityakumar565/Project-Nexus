package com.workflow.dag_engine.models.graph;

import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;

public class GraphMetaData {

    private String graphName;
    private String graphDescription;
    private String userId;
    private Integer numberOfEdges;
    private Integer numberOfNodes;
    private Integer costDimension;
    private CycleStatus cycleStatus;
    private GraphStatus graphStatus;
    private String version;

    public GraphMetaData() {
    }

    public GraphMetaData(String graphName, String graphDescription, String userId, Integer numberOfEdges,
            Integer numberOfNodes, Integer costDimension, CycleStatus cycleStatus, GraphStatus graphStatus,
            String version) {
        this.graphName = graphName;
        this.graphDescription = graphDescription;
        this.userId = userId;
        this.numberOfEdges = numberOfEdges;
        this.numberOfNodes = numberOfNodes;
        this.costDimension = costDimension;
        this.cycleStatus = cycleStatus;
        this.graphStatus = graphStatus;
        this.version = version;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getGraphDescription() {
        return graphDescription;
    }

    public void setGraphDescription(String graphDescription) {
        this.graphDescription = graphDescription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getNumberOfEdges() {
        return numberOfEdges;
    }

    public void setNumberOfEdges(Integer numberOfEdges) {
        this.numberOfEdges = numberOfEdges;
    }

    public Integer getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(Integer numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public Integer getCostDimension() {
        return costDimension;
    }

    public void setCostDimension(Integer costDimension) {
        this.costDimension = costDimension;
    }

    public CycleStatus getCycleStatus() {
        return cycleStatus;
    }

    public void setCycleStatus(CycleStatus cycleStatus) {
        this.cycleStatus = cycleStatus;
    }

    public GraphStatus getGraphStatus() {
        return graphStatus;
    }

    public void setGraphStatus(GraphStatus graphStatus) {
        this.graphStatus = graphStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "GraphMetaData [graphName=" + graphName + ", graphDescription=" + graphDescription + ", userId=" + userId
                + ", numberOfEdges=" + numberOfEdges + ", numberOfNodes=" + numberOfNodes + ", costDimension="
                + costDimension + ", cycleStatus=" + cycleStatus + ", graphStatus=" + graphStatus + ", version="
                + version + "]";
    }

}
