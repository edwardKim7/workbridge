CREATE TABLE department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(30) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    parent_department_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_department_code UNIQUE (department_code),
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_department_id) REFERENCES department (department_id)
);

CREATE TABLE employee (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_no VARCHAR(30) NOT NULL,
    login_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL,
    department_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    login_fail_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,
    last_login_at DATETIME(6) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_employee_no UNIQUE (employee_no),
    CONSTRAINT uk_employee_login_id UNIQUE (login_id),
    CONSTRAINT uk_employee_email UNIQUE (email),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (department_id)
);

CREATE TABLE `role` (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_role_code UNIQUE (role_code)
);

CREATE TABLE employee_role (
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_by BIGINT NULL,
    assigned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (employee_id, role_id),
    CONSTRAINT fk_employee_role_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_employee_role_role FOREIGN KEY (role_id) REFERENCES `role` (role_id)
);

CREATE TABLE menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_menu_id BIGINT NULL,
    menu_code VARCHAR(50) NOT NULL,
    menu_name VARCHAR(100) NOT NULL,
    menu_path VARCHAR(200) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_menu_code UNIQUE (menu_code),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_menu_id) REFERENCES menu (menu_id)
);

CREATE TABLE role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    can_read BOOLEAN NOT NULL DEFAULT TRUE,
    can_write BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES `role` (role_id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menu (menu_id)
);

CREATE TABLE customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_registration_no VARCHAR(10) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    representative_name VARCHAR(100) NULL,
    contact_name VARCHAR(100) NULL,
    contact_phone VARCHAR(30) NULL,
    contact_email VARCHAR(200) NULL,
    established_date DATE NULL,
    assigned_sales_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PROSPECT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_customer_business_no UNIQUE (business_registration_no),
    CONSTRAINT fk_customer_assigned_sales FOREIGN KEY (assigned_sales_id) REFERENCES employee (employee_id),
    CONSTRAINT chk_customer_status CHECK (status IN ('PROSPECT', 'ACTIVE', 'INACTIVE'))
);

CREATE TABLE sales_opportunity (
    opportunity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opportunity_no VARCHAR(40) NOT NULL,
    customer_id BIGINT NOT NULL,
    opportunity_name VARCHAR(200) NOT NULL,
    stage VARCHAR(30) NOT NULL,
    expected_revenue DECIMAL(18, 2) NOT NULL DEFAULT 0,
    expected_close_date DATE NULL,
    probability INT NOT NULL DEFAULT 0,
    owner_employee_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description VARCHAR(2000) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sales_opportunity_no UNIQUE (opportunity_no),
    CONSTRAINT fk_opportunity_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_opportunity_owner FOREIGN KEY (owner_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT chk_opportunity_stage CHECK (stage IN ('LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST')),
    CONSTRAINT chk_opportunity_status CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_opportunity_probability CHECK (probability BETWEEN 0 AND 100)
);

CREATE TABLE sales_activity (
    activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    opportunity_id BIGINT NULL,
    employee_id BIGINT NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    activity_at DATETIME(6) NOT NULL,
    location VARCHAR(300) NULL,
    content VARCHAR(4000) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_activity_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_activity_opportunity FOREIGN KEY (opportunity_id) REFERENCES sales_opportunity (opportunity_id),
    CONSTRAINT fk_activity_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT chk_activity_type CHECK (activity_type IN ('VISIT', 'CALL', 'MEETING', 'EMAIL', 'OTHER'))
);

CREATE TABLE approval_request (
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    approval_no VARCHAR(40) NOT NULL,
    approval_type VARCHAR(40) NOT NULL,
    business_type VARCHAR(40) NOT NULL,
    business_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    request_payload LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    requested_at DATETIME(6) NULL,
    processed_at DATETIME(6) NULL,
    canceled_at DATETIME(6) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_approval_no UNIQUE (approval_no),
    CONSTRAINT fk_approval_requester FOREIGN KEY (requester_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_approval_approver FOREIGN KEY (approver_id) REFERENCES employee (employee_id),
    CONSTRAINT chk_approval_type CHECK (approval_type IN ('SALES_EXPENSE', 'VISIT_PLAN', 'DISCOUNT_EXCEPTION')),
    CONSTRAINT chk_approval_status CHECK (status IN ('DRAFT', 'REQUESTED', 'APPROVED', 'REJECTED', 'CANCELED'))
);

CREATE TABLE approval_history (
    approval_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    approval_id BIGINT NOT NULL,
    from_status VARCHAR(20) NULL,
    to_status VARCHAR(20) NOT NULL,
    action VARCHAR(30) NOT NULL,
    actor_id BIGINT NOT NULL,
    opinion VARCHAR(1000) NULL,
    trace_id VARCHAR(64) NOT NULL,
    acted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_approval_history_request FOREIGN KEY (approval_id) REFERENCES approval_request (approval_id),
    CONSTRAINT fk_approval_history_actor FOREIGN KEY (actor_id) REFERENCES employee (employee_id)
);

CREATE TABLE external_interface_log (
    interface_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interface_type VARCHAR(40) NOT NULL,
    business_type VARCHAR(40) NOT NULL,
    business_id BIGINT NOT NULL,
    business_version BIGINT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(160) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    request_method VARCHAR(10) NOT NULL,
    request_url VARCHAR(500) NOT NULL,
    request_headers LONGTEXT NULL,
    request_body LONGTEXT NULL,
    request_truncated BOOLEAN NOT NULL DEFAULT FALSE,
    response_status INT NULL,
    response_headers LONGTEXT NULL,
    response_body LONGTEXT NULL,
    response_truncated BOOLEAN NOT NULL DEFAULT FALSE,
    error_code VARCHAR(100) NULL,
    error_message VARCHAR(1000) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6) NULL,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_interface_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_interface_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'TEMPORARY_FAILED', 'PERMANENT_FAILED', 'RETRYING'))
);

CREATE TABLE migration_batch (
    batch_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no VARCHAR(40) NOT NULL,
    migration_type VARCHAR(40) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    total_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    failure_count INT NOT NULL DEFAULT 0,
    executed_by BIGINT NOT NULL,
    started_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at DATETIME(6) NULL,
    error_file_path VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_migration_batch_no UNIQUE (batch_no),
    CONSTRAINT fk_migration_batch_executor FOREIGN KEY (executed_by) REFERENCES employee (employee_id),
    CONSTRAINT chk_migration_batch_status CHECK (status IN ('RECEIVED', 'STAGING', 'VALIDATING', 'APPLYING', 'COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED'))
);

CREATE TABLE migration_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    row_no INT NOT NULL,
    raw_data LONGTEXT NOT NULL,
    normalized_data LONGTEXT NULL,
    validation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    operation_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    existing_customer_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_migration_staging_row UNIQUE (batch_id, row_no),
    CONSTRAINT fk_migration_staging_batch FOREIGN KEY (batch_id) REFERENCES migration_batch (batch_id),
    CONSTRAINT fk_migration_staging_customer FOREIGN KEY (existing_customer_id) REFERENCES customer (customer_id),
    CONSTRAINT chk_staging_validation_status CHECK (validation_status IN ('PENDING', 'VALID', 'INVALID')),
    CONSTRAINT chk_staging_operation_type CHECK (operation_type IN ('INSERT', 'UPDATE', 'NONE'))
);

CREATE TABLE migration_error (
    migration_error_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    staging_id BIGINT NOT NULL,
    row_no INT NOT NULL,
    field_name VARCHAR(100) NULL,
    error_code VARCHAR(100) NOT NULL,
    error_message VARCHAR(1000) NOT NULL,
    raw_value VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_migration_error_batch FOREIGN KEY (batch_id) REFERENCES migration_batch (batch_id),
    CONSTRAINT fk_migration_error_staging FOREIGN KEY (staging_id) REFERENCES migration_staging (staging_id)
);

CREATE TABLE audit_log (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NULL,
    action VARCHAR(60) NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id VARCHAR(100) NULL,
    before_data LONGTEXT NULL,
    after_data LONGTEXT NULL,
    client_ip VARCHAR(64) NULL,
    user_agent VARCHAR(500) NULL,
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES employee (employee_id)
);

CREATE TABLE refresh_token (
    refresh_token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    replaced_by_token_id BIGINT NULL,
    device_info VARCHAR(300) NULL,
    client_ip VARCHAR(64) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_refresh_token_replacement FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_token (refresh_token_id)
);

CREATE TABLE system_error_log (
    system_error_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    employee_id BIGINT NULL,
    request_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    http_status INT NOT NULL,
    error_code VARCHAR(100) NOT NULL,
    exception_type VARCHAR(300) NULL,
    safe_message VARCHAR(1000) NOT NULL,
    log_reference VARCHAR(500) NULL,
    occurred_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_system_error_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
);

CREATE INDEX idx_department_parent_active ON department (parent_department_id, active);
CREATE INDEX idx_department_name ON department (department_name);
CREATE INDEX idx_employee_department_active ON employee (department_id, active);
CREATE INDEX idx_employee_name_active ON employee (employee_name, active);
CREATE INDEX idx_employee_locked_until ON employee (locked_until);
CREATE INDEX idx_employee_role_role_employee ON employee_role (role_id, employee_id);
CREATE INDEX idx_menu_parent_sort_active ON menu (parent_menu_id, sort_order, active);
CREATE INDEX idx_role_menu_menu_role ON role_menu (menu_id, role_id);
CREATE INDEX idx_customer_name_active ON customer (customer_name, active);
CREATE INDEX idx_customer_sales_status ON customer (assigned_sales_id, status, active);
CREATE INDEX idx_customer_contact_name ON customer (contact_name);
CREATE INDEX idx_opportunity_owner_status_stage ON sales_opportunity (owner_employee_id, status, stage);
CREATE INDEX idx_opportunity_customer_created ON sales_opportunity (customer_id, created_at);
CREATE INDEX idx_opportunity_close_status ON sales_opportunity (expected_close_date, status);
CREATE INDEX idx_opportunity_stage_status ON sales_opportunity (stage, status);
CREATE INDEX idx_activity_customer_at ON sales_activity (customer_id, activity_at);
CREATE INDEX idx_activity_opportunity_at ON sales_activity (opportunity_id, activity_at);
CREATE INDEX idx_activity_employee_at ON sales_activity (employee_id, activity_at);
CREATE INDEX idx_activity_type_at ON sales_activity (activity_type, activity_at);
CREATE INDEX idx_approval_approver_status ON approval_request (approver_id, status, requested_at);
CREATE INDEX idx_approval_requester_status ON approval_request (requester_id, status, created_at);
CREATE INDEX idx_approval_business ON approval_request (business_type, business_id);
CREATE INDEX idx_approval_type_status ON approval_request (approval_type, status, created_at);
CREATE INDEX idx_approval_history_request_at ON approval_history (approval_id, acted_at);
CREATE INDEX idx_approval_history_actor_at ON approval_history (actor_id, acted_at);
CREATE INDEX idx_approval_history_action_at ON approval_history (action, acted_at);
CREATE INDEX idx_interface_status_retry ON external_interface_log (status, next_retry_at);
CREATE INDEX idx_interface_business_created ON external_interface_log (business_type, business_id, created_at);
CREATE INDEX idx_interface_trace ON external_interface_log (trace_id);
CREATE INDEX idx_interface_created_status ON external_interface_log (created_at, status);
CREATE INDEX idx_migration_type_hash_status ON migration_batch (migration_type, file_hash, status);
CREATE INDEX idx_migration_executor_started ON migration_batch (executed_by, started_at);
CREATE INDEX idx_migration_status_started ON migration_batch (status, started_at);
CREATE INDEX idx_staging_batch_validation ON migration_staging (batch_id, validation_status);
CREATE INDEX idx_staging_batch_operation ON migration_staging (batch_id, operation_type);
CREATE INDEX idx_staging_existing_customer ON migration_staging (existing_customer_id);
CREATE INDEX idx_migration_error_batch_row ON migration_error (batch_id, row_no);
CREATE INDEX idx_migration_error_batch_code ON migration_error (batch_id, error_code);
CREATE INDEX idx_migration_error_staging ON migration_error (staging_id);
CREATE INDEX idx_audit_actor_created ON audit_log (actor_id, created_at);
CREATE INDEX idx_audit_target_created ON audit_log (target_type, target_id, created_at);
CREATE INDEX idx_audit_trace ON audit_log (trace_id);
CREATE INDEX idx_audit_action_created ON audit_log (action, created_at);
CREATE INDEX idx_refresh_employee_state ON refresh_token (employee_id, revoked_at, expires_at);
CREATE INDEX idx_refresh_expires ON refresh_token (expires_at);
CREATE INDEX idx_system_error_trace ON system_error_log (trace_id);
CREATE INDEX idx_system_error_occurred_code ON system_error_log (occurred_at, error_code);
CREATE INDEX idx_system_error_path_occurred ON system_error_log (request_path, occurred_at);
