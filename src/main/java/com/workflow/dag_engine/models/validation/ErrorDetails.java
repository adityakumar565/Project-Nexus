package com.workflow.dag_engine.models.validation;

public class ErrorDetails {

    String errorCode;
    String errorMessage;

    public ErrorDetails(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorDetails[errorCode=" + errorCode + ", errorMessage=" + errorMessage + "]";
    }

    public ErrorDetails() {

    }

    public ErrorDetails(Integer code) {

        switch (code) {
            case 1:
                this.errorCode = "0";
                this.errorMessage = "SUCCESS";
                break;
            case 2:
                this.errorCode = "200";
                this.errorMessage = "BAD REQUEST";
                break;
            case 3:
                this.errorCode = "400";
                this.errorMessage = "DATA NOT FOUND";
                break;
            case 4:
                this.errorCode = "401";
                this.errorMessage = "UNAUTHORIZED";
                break;
            case 5:
                this.errorCode = "404";
                this.errorMessage = "NOT FOUND";
                break;
            case 6:
                this.errorCode = "409";
                this.errorMessage = "CONFLICT";
                break;
            case 7:
                this.errorCode = "500";
                this.errorMessage = "INTERNAL SERVER ERROR";
                break;
            case 8:
                this.errorCode = "503";
                this.errorMessage = "SERVICE UNAVAILABLE";
                break;
            case 9:
                this.errorCode = "504";
                this.errorMessage = "GATEWAY TIMEOUT";
                break;
            default:
                this.errorCode = "-1";
                this.errorMessage = "UNKNOWN ERROR";
                break;
        }
    }

}
