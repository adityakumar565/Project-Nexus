package com.workflow.dag_engine.persistence.entities;

import java.time.LocalDateTime;

import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "t_graph", schema = "workflow_graphs",uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_graph_name", // Optional: Gives the constraint a readable name in PostgreSQL
            columnNames = {"user_id", "graph_name"}
        
    })
public class GraphEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long graphId; // UUID or UI-provided ID

    @Column(name = "user_id")
    private String userId; // The owner's ID

    // --- Memory Allocation Metadata ---
    private int numNodes;
    private int numEdges;
    private int costDimension;

    // --- Execution Metadata ---
    @Enumerated(EnumType.STRING)
    private CycleStatus isCyclic; // Enum: YES, NO, UNCHECKED

    private String implementationType; // e.g., "V1_CSR"

    // --- File Storage References ---
    private String jsonFilePath; // Claim-check path to the GraphRequest
    private String binaryFilePath; // Claim-check path to the Optimized structure

    // --- Concurrency & Audit ---
    @Version
    private Integer version; // Auto-managed by JPA for lock prevention

    @Column(name = "graph_name")
    private String graphName;

    @Column(name = "graph_description")
    private String graphDescription;

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

    @Enumerated(EnumType.STRING)
    private GraphStatus status; // Enum: DRAFT, VALIDATING, READY

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GraphEntity() {
    }

    public GraphEntity(Long graphId, String userId, int numNodes, int numEdges, int costDimension,
            CycleStatus isCyclic,
            String implementationType, String jsonFilePath, String binaryFilePath, Integer version, GraphStatus status,
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

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
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

    public CycleStatus getIsCyclic() {
        return isCyclic;
    }

    public void setIsCyclic(CycleStatus isCyclic) {
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

    public GraphStatus getStatus() {
        return status;
    }

    public void setStatus(GraphStatus status) {
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
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", graphName=" + graphName
                + ", graphDescription=" + graphDescription + "]";
    }

}
