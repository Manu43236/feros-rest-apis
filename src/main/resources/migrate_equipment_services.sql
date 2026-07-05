-- Equipment Service Records
CREATE TABLE IF NOT EXISTS equipment_services (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id             BIGINT       NOT NULL,
    equipment_id          BIGINT       NOT NULL,
    service_number        VARCHAR(100),
    triggered_by          VARCHAR(20)  NOT NULL,
    service_type          VARCHAR(20)  NOT NULL,
    payer_type            VARCHAR(20)  DEFAULT 'OWN_EXPENSE',
    status                VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    hmr_at_service        DECIMAL(10,2),
    due_at_hmr            DECIMAL(10,2),
    vendor_name           VARCHAR(255),
    location              VARCHAR(255),
    service_date          DATE,
    completed_date        DATE,
    started_at            DATETIME,
    total_cost            DECIMAL(10,2),
    insurance_claim_no    VARCHAR(100),
    insurance_claim_amt   DECIMAL(10,2),
    certificate_number    VARCHAR(100),
    certificate_valid_until DATE,
    is_escalated          TINYINT(1)   DEFAULT 0,
    notes                 TEXT,
    invoice_id            BIGINT,
    is_active             TINYINT(1)   DEFAULT 1,
    created_at            DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_esvc_tenant    FOREIGN KEY (tenant_id)    REFERENCES tenants(id),
    CONSTRAINT fk_esvc_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);

-- Equipment Service Tasks
CREATE TABLE IF NOT EXISTS equipment_service_tasks (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id     BIGINT       NOT NULL,
    task_type_id   BIGINT,
    custom_name    VARCHAR(255),
    is_recurring   TINYINT(1)   DEFAULT 0,
    frequency_hmr  DECIMAL(10,2),
    cost           DECIMAL(10,2),
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    started_at     DATETIME,
    completed_at   DATETIME,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_esvc_task_service FOREIGN KEY (service_id) REFERENCES equipment_services(id) ON DELETE CASCADE
);
