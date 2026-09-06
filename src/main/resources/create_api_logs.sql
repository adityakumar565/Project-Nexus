-- 1. Create schema
CREATE SCHEMA IF NOT EXISTS logs;

-- 2. Create api_logs table
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

-- 3. Create performance indexes
CREATE INDEX IF NOT EXISTS idx_api_logs_correlation_id ON logs.api_logs(correlation_id);
CREATE INDEX IF NOT EXISTS idx_api_logs_user_name ON logs.api_logs(user_name);
CREATE INDEX IF NOT EXISTS idx_api_logs_start_time ON logs.api_logs(start_time);

-- 4. Grant permissions to application user
GRANT USAGE, CREATE ON SCHEMA logs TO dag_engine_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA logs TO dag_engine_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA logs TO dag_engine_user;
ALTER TABLE logs.api_logs OWNER TO dag_engine_user;
