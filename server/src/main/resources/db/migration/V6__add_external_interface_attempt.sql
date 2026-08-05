CREATE TABLE external_interface_attempt (
    attempt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interface_id BIGINT NOT NULL,
    attempt_no INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    response_status INT NULL,
    response_body LONGTEXT NULL,
    error_code VARCHAR(100) NULL,
    error_message VARCHAR(1000) NULL,
    trace_id VARCHAR(64) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_interface_attempt_no UNIQUE (interface_id, attempt_no),
    CONSTRAINT fk_interface_attempt_log FOREIGN KEY (interface_id) REFERENCES external_interface_log(interface_id),
    CONSTRAINT chk_interface_attempt_status CHECK (status IN ('SUCCESS','TEMPORARY_FAILED','PERMANENT_FAILED'))
);
CREATE INDEX idx_interface_attempt_trace ON external_interface_attempt (trace_id);
CREATE INDEX idx_interface_attempt_completed ON external_interface_attempt (completed_at, status);
