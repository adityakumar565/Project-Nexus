package com.workflow.dag_engine.models.path;

import java.util.List;
import java.util.Map;

import com.workflow.dag_engine.models.graph.Node;

public class PathDTO {
    
    private List<Node> nodeSequence;
    private Map<String, Float> pathCosts;

    public PathDTO() {}

    public PathDTO(List<Node> nodeSequence, Map<String, Float> pathCosts) {
        this.nodeSequence = nodeSequence;
        this.pathCosts = pathCosts;
    }

    public List<Node> getNodeSequence() {
        return nodeSequence;
    }

    public void setNodeSequence(List<Node> nodeSequence) {
        this.nodeSequence = nodeSequence;
    }

    public Map<String, Float> getPathCosts() {
        return pathCosts;
    }

    public void setPathCosts(Map<String, Float> pathCosts) {
        this.pathCosts = pathCosts;
    }
}
