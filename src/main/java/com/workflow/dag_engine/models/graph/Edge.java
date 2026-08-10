package com.workflow.dag_engine.models.graph;

public class Edge {

    Long id;
    Long sourceNodeId;
    Long targetNodeId;
    Cost edgeCost;
    String edgeName;
    String edgeDescription;

    public Edge(Long id, Long sourceNodeId, Long targetNodeId, Cost edgeCost, String edgeName, String edgeDescription) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.edgeCost = edgeCost;
        this.edgeName = edgeName;
        this.edgeDescription = edgeDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public Cost getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(Cost edgeCost) {
        this.edgeCost = edgeCost;
    }

    public String getEdgeName() {
        return edgeName;
    }

    public void setEdgeName(String edgeName) {
        this.edgeName = edgeName;
    }

    public String getEdgeDescription() {
        return edgeDescription;
    }

    public void setEdgeDescription(String edgeDescription) {
        this.edgeDescription = edgeDescription;
    }

    public double getEdgeCostAt(String costName) {

        return this.edgeCost.getCostAt(costName);

    }

    @Override
    public String toString() {
        return "Edge[id=" + id + ", sourceNodeId=" + sourceNodeId + ", targetNodeId=" + targetNodeId
                + ", edgeCost=" + edgeCost + ", edgeName=" + edgeName + ", edgeDescription=" + edgeDescription + "]";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        return id.equals(other.id);
    }

}
