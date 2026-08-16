package com.workflow.dag_engine.persistence.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.persistence.listeners.GraphAuditListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@EntityListeners(GraphAuditListener.class)
@Table(name = "t_graph", schema = "workflow_graphs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_graph_name", columnNames = { "user_id", "graph_name" })
})
public class GraphEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "graph_id")
    private Long graphId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "graph_name", nullable = false, length = 255)
    private String graphName;

    @Column(name = "graph_description", length = 1000)
    private String graphDescription;

    // --- Memory Allocation Metadata ---
    @Column(name = "num_nodes", nullable = false)
    private int numNodes;

    @Column(name = "num_edges", nullable = false)
    private int numEdges;

    @Column(name = "cost_dimension", nullable = false)
    private int costDimension;

    // --- Execution Metadata ---
    @Enumerated(EnumType.STRING)
    @Column(name = "is_cyclic", nullable = false, length = 20)
    private CycleStatus isCyclic;

    @Column(name = "implementation_type", length = 50)
    private String implementationType;

    // --- JSON Storage ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "graph_data", columnDefinition = "jsonb")
    private GraphUploadRequest graphData;

    // --- File Storage References ---
    @Column(name = "binary_file_path", length = 500)
    private String binaryFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GraphStatus status;

    // --- Concurrency & Audit ---
    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GraphEntity() {
    }

    public GraphEntity(Long graphId, Long userId, String graphName, String graphDescription, int numNodes,
            int numEdges, int costDimension, CycleStatus isCyclic, String implementationType,
            GraphUploadRequest graphData, String binaryFilePath, Integer version, GraphStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.graphId = graphId;
        this.userId = userId;
        this.graphName = graphName;
        this.graphDescription = graphDescription;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.costDimension = costDimension;
        this.isCyclic = isCyclic;
        this.implementationType = implementationType;
        this.graphData = graphData;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public GraphUploadRequest getGraphData() {
        return graphData;
    }

    public void setGraphData(GraphUploadRequest graphData) {
        this.graphData = graphData;
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
        return "GraphEntity [graphId=" + graphId + ", userId=" + userId + ", graphName=" + graphName
                + ", graphDescription=" + graphDescription + ", numNodes=" + numNodes + ", numEdges=" + numEdges
                + ", costDimension=" + costDimension + ", isCyclic=" + isCyclic + ", implementationType="
                + implementationType + ", graphData=" + graphData + ", binaryFilePath=" + binaryFilePath
                + ", version=" + version + ", status=" + status + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + "]";
    }
}
