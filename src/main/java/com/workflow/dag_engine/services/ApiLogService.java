package com.workflow.dag_engine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.workflow.dag_engine.persistence.entities.ApiLogEntity;
import com.workflow.dag_engine.persistence.repositories.ApiLogRepository;

@Service
public class ApiLogService {

    private static final Logger log = LoggerFactory.getLogger(ApiLogService.class);

    private final ApiLogRepository apiLogRepository;

    public ApiLogService(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    /**
     * Saves the API log entity in an isolated transaction so that it commits
     * independently and does not fail the main request on logging errors.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(ApiLogEntity logEntity) {
        try {
            apiLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to persist API log in logs.api_logs: {}", e.getMessage(), e);
        }
    }

}
