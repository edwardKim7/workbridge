CREATE INDEX idx_system_error_status_occurred ON system_error_log (http_status, occurred_at);
CREATE INDEX idx_audit_created_trace ON audit_log (created_at, trace_id);
CREATE INDEX idx_migration_completed_status ON migration_batch (completed_at, status);
