package com.workflow.dag_engine.models.path;

import java.util.List;
import com.workflow.dag_engine.models.validation.ErrorDetails;

public class GraphPathResponse {

    private Long graphId;
    private String graphName;
    private Integer totalPaths;
    private List<PathDTO> paths;
    private ErrorDetails objErrorDetails;

    public GraphPathResponse() {
    }

    public GraphPathResponse(Long graphId, String graphName, Integer totalPaths, List<PathDTO> paths,
            ErrorDetails objErrorDetails) {
        this.graphId = graphId;
        this.graphName = graphName;
        this.totalPaths = totalPaths;
        this.paths = paths;
        this.objErrorDetails = objErrorDetails;
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

    public Integer getTotalPaths() {
        return totalPaths;
    }

    public void setTotalPaths(Integer totalPaths) {
        this.totalPaths = totalPaths;
    }

    public List<PathDTO> getPaths() {
        return paths;
    }

    public void setPaths(List<PathDTO> paths) {
        this.paths = paths;
    }

    public ErrorDetails getObjErrorDetails() {
        return objErrorDetails;
    }

    public void setObjErrorDetails(ErrorDetails objErrorDetails) {
        this.objErrorDetails = objErrorDetails;
    }

    @Override
    public String toString() {
        return "GraphPathResponse [graphId=" + graphId + ", graphName=" + graphName + ", totalPaths=" + totalPaths
                + ", paths=" + paths + ", objErrorDetails=" + objErrorDetails + "]";
    }
}
