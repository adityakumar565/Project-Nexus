package com.workflow.dag_engine.models.graph;

import com.workflow.dag_engine.models.userModel.User;
import com.workflow.dag_engine.models.validation.ErrorDetails;

public class GraphResponse extends User {

    String graphName;
    String graphDescription;
    ErrorDetails objErrorDetails;
    GraphUploadRequest objGraphUploadRequest;

    public GraphResponse(String graphName, String graphDescription, ErrorDetails objErrorDetails) {
        this.graphName = graphName;
        this.graphDescription = graphDescription;
        this.objErrorDetails = objErrorDetails;
    }

    public GraphResponse() {
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

    public ErrorDetails getObjErrorDetails() {
        return objErrorDetails;
    }

    public void setObjErrorDetails(ErrorDetails objErrorDetails) {
        this.objErrorDetails = objErrorDetails;
    }

    @Override
    public String toString() {
        return "GraphResponse[graphName=" + graphName + ", graphDescription=" + graphDescription
                + ", objErrorDetails=" + objErrorDetails + "]";
    }

    public GraphUploadRequest getObjGraphUploadRequest() {
        return objGraphUploadRequest;
    }

    public void setObjGraphUploadRequest(GraphUploadRequest objGraphUploadRequest) {
        this.objGraphUploadRequest = objGraphUploadRequest;
    }

}
