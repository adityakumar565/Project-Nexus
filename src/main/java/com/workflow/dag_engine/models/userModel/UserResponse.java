package com.workflow.dag_engine.models.userModel;

import com.workflow.dag_engine.models.validation.ErrorDetails;

public class UserResponse {

    ErrorDetails objErrorDetails;
    UserRequest objUserRequest;

    public UserResponse(ErrorDetails objErrorDetails, UserRequest objUserRequest) {
        this.objErrorDetails = objErrorDetails;
        this.objUserRequest = objUserRequest;
    }

    public ErrorDetails getObjErrorDetails() {
        return objErrorDetails;
    }

    public void setObjErrorDetails(ErrorDetails objErrorDetails) {
        this.objErrorDetails = objErrorDetails;
    }

    public UserRequest getObjUserRequest() {
        return objUserRequest;
    }

    public void setObjUserRequest(UserRequest objUserRequest) {
        this.objUserRequest = objUserRequest;
    }

    @Override
    public String toString() {
        return "[UserResponse(objErrorDetails=" + objErrorDetails + ", objUserRequest=" + objUserRequest
                + ")]";
    }

    public UserResponse() {

    }
}
