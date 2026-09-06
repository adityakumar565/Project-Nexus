package com.workflow.dag_engine.config;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.dag_engine.helpers.CorrelationIdGenerator;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.persistence.entities.ApiLogEntity;
import com.workflow.dag_engine.services.ApiLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
@Order(1)
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);

    private final ApiLogService apiLogService;
    private final ObjectMapper objectMapper;

    public ApiLoggingAspect(ApiLogService apiLogService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        this.apiLogService = apiLogService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper().findAndRegisterModules();
    }

    /**
     * Intercepts all methods in classes annotated with @RestController under controller package.
     */
    @Pointcut("within(com.workflow.dag_engine.controller..*)")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        String correlationId = null;
        if (request != null) {
            correlationId = request.getHeader("X-Correlation-ID");
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = request.getParameter("correlationId");
            }
        }
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = CorrelationIdGenerator.generateCorrelationId();
        } else {
            correlationId = correlationId.trim();
            if (correlationId.length() > 27) {
                correlationId = correlationId.substring(0, 27);
            }
        }

        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();

        if (response != null) {
            response.setHeader("X-Correlation-ID", correlationId);
            response.setHeader("Access-Control-Expose-Headers", "X-Correlation-ID");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String apiName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String apiUrl = request != null ? request.getRequestURI() : "UNKNOWN";
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";

        // Extract user name and serialize request body
        String userName = extractUserName(joinPoint.getArgs(), request);
        String apiRequestJson = serializeAndMaskRequest(joinPoint.getArgs());

        ApiLogEntity logEntity = new ApiLogEntity();
        logEntity.setCorrelationId(correlationId);
        logEntity.setApiName(apiName);
        logEntity.setApiUrl(apiUrl);
        logEntity.setHttpMethod(httpMethod);
        logEntity.setUserName(userName);
        logEntity.setStartTime(startTime);
        logEntity.setApiRequest(apiRequestJson);

        Object result = null;
        try {
            result = joinPoint.proceed();
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = System.currentTimeMillis() - startMs;

            logEntity.setEndTime(endTime);
            logEntity.setDurationMs(durationMs);
            logEntity.setApiResponse(serializeResponse(result));
            logEntity.setStatusCode(extractStatusCode(result, response));

            return result;
        } catch (Throwable t) {
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = System.currentTimeMillis() - startMs;

            logEntity.setEndTime(endTime);
            logEntity.setDurationMs(durationMs);
            logEntity.setApiResponse("{\"error\": \"" + escapeJson(t.getMessage()) + "\"}");
            logEntity.setStatusCode("500");

            throw t;
        } finally {
            try {
                apiLogService.saveLog(logEntity);
                log.info("Logged API call [{}]: {} {} ({}) - {}ms", correlationId, httpMethod, apiUrl, apiName,
                        logEntity.getDurationMs());
            } catch (Exception e) {
                log.error("Could not record API log: {}", e.getMessage());
            }
        }
    }

    private String extractUserName(Object[] args, HttpServletRequest request) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof UserRequest) {
                    UserRequest ur = (UserRequest) arg;
                    if (ur.getUserName() != null && !ur.getUserName().trim().isEmpty()) {
                        return ur.getUserName();
                    }
                }
            }
        }

        if (request != null) {
            String userHeader = request.getHeader("X-User-Name");
            if (userHeader != null && !userHeader.trim().isEmpty()) {
                return userHeader;
            }
            String userParam = request.getParameter("userName");
            if (userParam != null && !userParam.trim().isEmpty()) {
                return userParam;
            }
        }

        return "ANONYMOUS";
    }

    private String serializeAndMaskRequest(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        try {
            Object toSerialize = args.length == 1 ? args[0] : args;
            String json = objectMapper.writeValueAsString(toSerialize);
            // Mask password fields for security
            return json.replaceAll("(?i)\"(userPassword|password)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"••••••••\"");
        } catch (Exception e) {
            return "{\"serialization_error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private String serializeResponse(Object result) {
        if (result == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"serialization_error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private String extractStatusCode(Object result, HttpServletResponse response) {
        // Try extracting error code from returned object if present
        if (result != null) {
            try {
                java.lang.reflect.Method getErrorDetails = result.getClass().getMethod("getObjErrorDetails");
                Object errorDetails = getErrorDetails.invoke(result);
                if (errorDetails != null) {
                    java.lang.reflect.Method getErrorCode = errorDetails.getClass().getMethod("getErrorCode");
                    Object code = getErrorCode.invoke(errorDetails);
                    if (code != null) {
                        return code.toString();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (response != null) {
            return String.valueOf(response.getStatus());
        }

        return "200";
    }

    private String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

}
