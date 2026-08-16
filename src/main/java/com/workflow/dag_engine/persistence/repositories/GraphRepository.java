package com.workflow.dag_engine.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workflow.dag_engine.persistence.entities.GraphEntity;

public interface GraphRepository extends JpaRepository<GraphEntity, Long> {

    boolean existsByGraphName(String graphName);

    List<GraphEntity> findByUserId(Long userId);

    GraphEntity findByGraphId(Long graphId);

}
