package com.workflow.dag_engine.models.graph;

import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;

public class GraphMetaData {

    private Long graphId;
    private String graphName;
    private String graphDescription;
    private Long userId;
    private Integer numberOfEdges;
    private Integer numberOfNodes;
    private Integer costDimension;
    private CycleStatus cycleStatus;
    private GraphStatus graphStatus;
    private Integer version;

    public GraphMetaData() {
    }

    public GraphMetaData(Long graphId, String graphName, String graphDescription, Long userId, Integer numberOfEdges,
            Integer numberOfNodes, Integer costDimension, CycleStatus cycleStatus, GraphStatus graphStatus,
            Integer version) {
        this.graphId = graphId;
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

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "GraphMetaData [graphId=" + graphId + ", graphName=" + graphName + ", graphDescription="
                + graphDescription + ", userId=" + userId + ", numberOfEdges=" + numberOfEdges + ", numberOfNodes="
                + numberOfNodes + ", costDimension=" + costDimension + ", cycleStatus=" + cycleStatus
                + ", graphStatus=" + graphStatus + ", version=" + version + "]";
    }

}
