package com.workflow.dag_engine.models.validation;

public class ApplicationException extends Exception {

    public String code;
    public String message;

    public ApplicationException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApplicationException() {
    }

}
