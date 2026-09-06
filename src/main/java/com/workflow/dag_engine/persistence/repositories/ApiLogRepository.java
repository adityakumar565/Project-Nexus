package com.workflow.dag_engine.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workflow.dag_engine.persistence.entities.ApiLogEntity;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLogEntity, Long> {

    List<ApiLogEntity> findByCorrelationId(String correlationId);

    List<ApiLogEntity> findByUserName(String userName);

    List<ApiLogEntity> findByApiName(String apiName);

}
