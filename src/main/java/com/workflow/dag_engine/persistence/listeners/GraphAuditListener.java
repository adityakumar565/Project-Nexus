package com.workflow.dag_engine.persistence.listeners;

import java.time.LocalDateTime;

import com.workflow.dag_engine.persistence.entities.GraphEntity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class GraphAuditListener {

    @PrePersist
    public void onPrePersist(GraphEntity graphEntity) {
        LocalDateTime now = LocalDateTime.now();
        if (graphEntity.getCreatedAt() == null) {
            graphEntity.setCreatedAt(now);
        }
        if (graphEntity.getUpdatedAt() == null) {
            graphEntity.setUpdatedAt(now);
        }
    }

    @PreUpdate
    public void onPreUpdate(GraphEntity graphEntity) {
        graphEntity.setUpdatedAt(LocalDateTime.now());
    }
}
