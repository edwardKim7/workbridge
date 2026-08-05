ALTER TABLE migration_batch ADD COLUMN dedup_key VARCHAR(64) NULL;
ALTER TABLE migration_batch ADD CONSTRAINT uk_migration_dedup_key UNIQUE (dedup_key);

ALTER TABLE migration_staging ADD COLUMN assigned_sales_id BIGINT NULL;
ALTER TABLE migration_staging ADD COLUMN business_registration_no VARCHAR(10) NULL;
ALTER TABLE migration_staging ADD COLUMN customer_name VARCHAR(200) NULL;
ALTER TABLE migration_staging ADD COLUMN representative_name VARCHAR(100) NULL;
ALTER TABLE migration_staging ADD COLUMN contact_name VARCHAR(100) NULL;
ALTER TABLE migration_staging ADD COLUMN contact_email VARCHAR(200) NULL;
ALTER TABLE migration_staging ADD COLUMN contact_phone VARCHAR(30) NULL;
ALTER TABLE migration_staging ADD COLUMN established_date DATE NULL;
ALTER TABLE migration_staging ADD CONSTRAINT fk_migration_staging_sales FOREIGN KEY (assigned_sales_id) REFERENCES employee(employee_id);
CREATE INDEX idx_staging_batch_business_no ON migration_staging (batch_id, business_registration_no);
CREATE INDEX idx_staging_batch_assignee ON migration_staging (batch_id, assigned_sales_id);
