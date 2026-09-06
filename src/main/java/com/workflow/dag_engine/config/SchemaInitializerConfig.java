package com.workflow.dag_engine.config;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializerConfig.class);

    public SchemaInitializerConfig(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 1. Create Schemas
            stmt.execute("CREATE SCHEMA IF NOT EXISTS logs;");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS workflow_user;");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS workflow_graphs;");

            // 2. Create Table logs.api_logs
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS logs.api_logs (
                    id BIGSERIAL PRIMARY KEY,
                    correlation_id VARCHAR(27) NOT NULL,
                    api_name VARCHAR(100),
                    api_url VARCHAR(255),
                    http_method VARCHAR(10),
                    user_name VARCHAR(100),
                    start_time TIMESTAMP,
                    end_time TIMESTAMP,
                    duration_ms BIGINT,
                    api_request TEXT,
                    api_response TEXT,
                    status_code VARCHAR(20)
                );
                CREATE INDEX IF NOT EXISTS idx_api_logs_correlation_id ON logs.api_logs(correlation_id);
                CREATE INDEX IF NOT EXISTS idx_api_logs_user_name ON logs.api_logs(user_name);
                CREATE INDEX IF NOT EXISTS idx_api_logs_start_time ON logs.api_logs(start_time);
            """;
            stmt.execute(createTableSql);

            log.info("SchemaInitializerConfig: Verified schemas and table logs.api_logs exist.");
        } catch (Exception e) {
            log.warn("SchemaInitializerConfig: Exception during schema initialization: {}", e.getMessage());
        }
    }

}
