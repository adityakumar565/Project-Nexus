package com.workflow.dag_engine.models.graph;

public class GraphUpdateRequest extends GraphUploadRequest {
    
    private String updateType; // "S" for Simple, "C" for Complex

    public GraphUpdateRequest() {
        super();
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    @Override
    public String toString() {
        return "GraphUpdateRequest [updateType=" + updateType + ", " + super.toString() + "]";
    }
}
