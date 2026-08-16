package com.workflow.dag_engine.models.graph;

import com.workflow.dag_engine.models.validation.ErrorDetails;

public class GraphLoadResponse {

    ErrorDetails objErrorDetails;

    public ErrorDetails getObjErrorDetails() {
        return objErrorDetails;
    }

    public void setObjErrorDetails(ErrorDetails objErrorDetails) {
        this.objErrorDetails = objErrorDetails;
    }

    @Override
    public String toString() {
        return "GraphLoadResponse [objErrorDetails=" + objErrorDetails + "]";
    }

}
