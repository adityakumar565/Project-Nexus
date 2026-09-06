package com.workflow.dag_engine.journey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.workflow.dag_engine.controller.UserController;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.persistence.entities.ApiLogEntity;
import com.workflow.dag_engine.persistence.repositories.ApiLogRepository;

@SpringBootTest
public class ApiLoggingIntegrationTest {

    @Autowired
    private UserController userController;

    @Autowired
    private ApiLogRepository apiLogRepository;

    @Test
    public void testControllerCallCreatesApiLogEntry() throws Exception {
        long initialCount = apiLogRepository.count();

        UserRequest request = new UserRequest("Aditya", "abcd");
        userController.validateUser(request);

        List<ApiLogEntity> logs = apiLogRepository.findAll();
        assertTrue(logs.size() > initialCount, "A new API log record must be created in logs.api_logs");

        ApiLogEntity latestLog = logs.get(logs.size() - 1);
        assertNotNull(latestLog.getCorrelationId());
        assertEquals(27, latestLog.getCorrelationId().length(), "Correlation ID must be 27 digits");
        assertTrue(latestLog.getCorrelationId().matches("^\\d{27}$"), "Correlation ID must be numeric");

        assertEquals("UserController.validateUser", latestLog.getApiName());
        assertEquals("Aditya", latestLog.getUserName());
        assertNotNull(latestLog.getStartTime());
        assertNotNull(latestLog.getEndTime());
        assertNotNull(latestLog.getDurationMs());

        // Password should be masked
        assertFalse(latestLog.getApiRequest().contains("abcd"), "Plaintext password must not be stored in request log");
        assertTrue(latestLog.getApiRequest().contains("••••••••"), "Password should be masked with bullets");
    }

}
