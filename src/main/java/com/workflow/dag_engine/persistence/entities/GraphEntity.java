package com.workflow.dag_engine.persistence.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "t_graph", schema = "workflow_graphs")
public class GraphEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String graphId; // UUID or UI-provided ID

    private String userId; // The owner's ID

    // --- Memory Allocation Metadata ---
    private int numNodes;
    private int numEdges;
    private int costDimension;

    // --- Execution Metadata ---
    @Enumerated(EnumType.STRING)
    private String isCyclic; // Enum: YES, NO, UNCHECKED

    private String implementationType; // e.g., "V1_CSR"

    // --- File Storage References ---
    private String jsonFilePath; // Claim-check path to the GraphRequest
    private String binaryFilePath; // Claim-check path to the Optimized structure

    // --- Concurrency & Audit ---
    @Version
    private Integer version; // Auto-managed by JPA for lock prevention

    @Enumerated(EnumType.STRING)
    private String status; // Enum: DRAFT, VALIDATING, READY

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GraphEntity() {
    }

    public GraphEntity(String graphId, String userId, int numNodes, int numEdges, int costDimension, String isCyclic,
            String implementationType, String jsonFilePath, String binaryFilePath, Integer version, String status,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.graphId = graphId;
        this.userId = userId;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.costDimension = costDimension;
        this.isCyclic = isCyclic;
        this.implementationType = implementationType;
        this.jsonFilePath = jsonFilePath;
        this.binaryFilePath = binaryFilePath;
        this.version = version;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getNumEdges() {
        return numEdges;
    }

    public void setNumEdges(int numEdges) {
        this.numEdges = numEdges;
    }

    public int getCostDimension() {
        return costDimension;
    }

    public void setCostDimension(int costDimension) {
        this.costDimension = costDimension;
    }

    public String getIsCyclic() {
        return isCyclic;
    }

    public void setIsCyclic(String isCyclic) {
        this.isCyclic = isCyclic;
    }

    public String getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }

    public String getJsonFilePath() {
        return jsonFilePath;
    }

    public void setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    public String getBinaryFilePath() {
        return binaryFilePath;
    }

    public void setBinaryFilePath(String binaryFilePath) {
        this.binaryFilePath = binaryFilePath;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "GraphEntity [graphId=" + graphId + ", userId=" + userId + ", numNodes=" + numNodes + ", numEdges="
                + numEdges + ", costDimension=" + costDimension + ", isCyclic=" + isCyclic
                + ", implementationType=" + implementationType + ", jsonFilePath=" + jsonFilePath
                + ", binaryFilePath=" + binaryFilePath + ", version=" + version + ", status=" + status
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

}
