package com.workflow.dag_engine.models.graph;

import com.workflow.dag_engine.models.validation.BaseModel;

public class UserGraphRequest extends BaseModel {

    private Long userId;

    public UserGraphRequest() {
    }

    public UserGraphRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserGraphRequest [userId=" + userId + ", getUserId()=" + getUserId() + "]";
    }

}
