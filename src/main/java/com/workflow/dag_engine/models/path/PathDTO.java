package com.workflow.dag_engine.models.path;

import java.util.List;
import java.util.Map;

public class PathDTO {
    
    private List<Integer> nodeSequence;
    private Map<String, Float> pathCosts;

    public PathDTO() {}

    public PathDTO(List<Integer> nodeSequence, Map<String, Float> pathCosts) {
        this.nodeSequence = nodeSequence;
        this.pathCosts = pathCosts;
    }

    public List<Integer> getNodeSequence() {
        return nodeSequence;
    }

    public void setNodeSequence(List<Integer> nodeSequence) {
        this.nodeSequence = nodeSequence;
    }

    public Map<String, Float> getPathCosts() {
        return pathCosts;
    }

    public void setPathCosts(Map<String, Float> pathCosts) {
        this.pathCosts = pathCosts;
    }
}
