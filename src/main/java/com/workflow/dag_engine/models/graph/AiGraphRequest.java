package com.workflow.dag_engine.models.graph;

public class AiGraphRequest {

    private String prompt;
    private Long userId;
    private Long graphId;
    private String currentGraphJson;

    public AiGraphRequest() {
    }

    public AiGraphRequest(String prompt, Long userId, Long graphId, String currentGraphJson) {
        this.prompt = prompt;
        this.userId = userId;
        this.graphId = graphId;
        this.currentGraphJson = currentGraphJson;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public String getCurrentGraphJson() {
        return currentGraphJson;
    }

    public void setCurrentGraphJson(String currentGraphJson) {
        this.currentGraphJson = currentGraphJson;
    }

    @Override
    public String toString() {
        return "AiGraphRequest [prompt=" + prompt + ", userId=" + userId + ", graphId=" + graphId + "]";
    }
}
