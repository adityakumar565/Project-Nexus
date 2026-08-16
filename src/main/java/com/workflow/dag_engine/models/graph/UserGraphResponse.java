package com.workflow.dag_engine.models.graph;

import java.util.List;

import com.workflow.dag_engine.models.validation.ErrorDetails;

public class UserGraphResponse {

    private ErrorDetails objErrorDetails;
    private List<GraphUploadRequest> userGraphList;

    public UserGraphResponse() {
    }

    public UserGraphResponse(ErrorDetails objErrorDetails, List<GraphUploadRequest> userGraphList) {
        this.objErrorDetails = objErrorDetails;
        this.userGraphList = userGraphList;
    }

    public UserGraphResponse(List<GraphUploadRequest> userGraphList) {
        this.userGraphList = userGraphList;
    }

    public ErrorDetails getObjErrorDetails() {
        return objErrorDetails;
    }

    public void setObjErrorDetails(ErrorDetails objErrorDetails) {
        this.objErrorDetails = objErrorDetails;
    }

    public List<GraphUploadRequest> getUserGraphList() {
        return userGraphList;
    }

    public void setUserGraphList(List<GraphUploadRequest> userGraphList) {
        this.userGraphList = userGraphList;
    }

    @Override
    public String toString() {
        return "UserGraphResponse [objErrorDetails=" + objErrorDetails + ", userGraphList=" + userGraphList + "]";
    }

}
