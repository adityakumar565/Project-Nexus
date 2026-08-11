package com.workflow.dag_engine.models.graph;

import java.util.List;

public class GraphUploadRequest {

    String graphName;
    String graphDescription;
    Long graphId;

    Long userId;

    List<Node> node;
    List<Edge> edge;

    List<String> costNames;

    public GraphUploadRequest(String graphName, String graphDescription, Long graphId, Long userId, List<Node> node,
            List<Edge> edge, List<String> costNames) {
        this.graphName = graphName;
        this.graphDescription = graphDescription;
        this.graphId = graphId;
        this.userId = userId;
        this.node = node;
        this.edge = edge;
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

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Node> getNode() {
        return node;
    }

    public void setNode(List<Node> node) {
        this.node = node;
    }

    public List<Edge> getEdge() {
        return edge;
    }

    public void setEdge(List<Edge> edge) {
        this.edge = edge;
    }

    public List<String> getCostNames() {
        return costNames;
    }

    public void setCostNames(List<String> costNames) {
        this.costNames = costNames;
    }

    @Override
    public String toString() {
        return "GraphUploadRequest[graphName=" + graphName + ", graphDescription=" + graphDescription + ", graphId="
                + graphId
                + ", userId=" + userId + ", node=" + node + ", edge=" + edge + ", costNames=" + costNames + "]";
    }

}
