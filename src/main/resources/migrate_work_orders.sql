-- Migration: create work_orders, machine_assignments, equipment_daily_logs
-- Run on staging first, then prod
-- Date: 2026-06-28

CREATE TABLE work_orders (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id               BIGINT          NOT NULL,
    wo_number               VARCHAR(60)     UNIQUE,
    client_id               BIGINT          NOT NULL,
    site                    VARCHAR(255),
    rate_type               VARCHAR(20)     NOT NULL,
    rate_amount             DECIMAL(12,2)   NOT NULL,
    shift_hours             INT             DEFAULT 8,
    overtime_rate_per_hour  DECIMAL(10,2),
    operator_type           VARCHAR(20),
    operator_staff_id       BIGINT,
    hired_operator_name     VARCHAR(100),
    hired_operator_phone    VARCHAR(20),
    operator_billing        VARCHAR(30)     NOT NULL DEFAULT 'NOT_BILLED',
    operator_rate_per_day   DECIMAL(10,2),
    mobilization_charge     DECIMAL(10,2),
    demobilization_charge   DECIMAL(10,2),
    start_date              DATE            NOT NULL,
    end_date                DATE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    parent_wo_id            BIGINT,
    notes                   TEXT,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_wo_tenant         FOREIGN KEY (tenant_id)         REFERENCES tenants (id),
    CONSTRAINT fk_wo_client         FOREIGN KEY (client_id)         REFERENCES clients (id),
    CONSTRAINT fk_wo_operator_staff FOREIGN KEY (operator_staff_id) REFERENCES staff_profiles (id),
    CONSTRAINT fk_wo_parent         FOREIGN KEY (parent_wo_id)      REFERENCES work_orders (id)
);

CREATE TABLE machine_assignments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id   BIGINT      NOT NULL,
    equipment_id    BIGINT      NOT NULL,
    start_date      DATE        NOT NULL,
    end_date        DATE,
    end_reason      VARCHAR(30),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ma_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders (id),
    CONSTRAINT fk_ma_equipment  FOREIGN KEY (equipment_id)  REFERENCES equipment (id)
);

CREATE TABLE equipment_daily_logs (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    machine_assignment_id BIGINT          NOT NULL,
    work_order_id         BIGINT          NOT NULL,
    log_date              DATE            NOT NULL,
    status                VARCHAR(20)     NOT NULL,
    start_hour_meter      DECIMAL(10,2),
    end_hour_meter        DECIMAL(10,2),
    hours_worked          DECIMAL(10,2),
    fuel_consumed         DECIMAL(10,2),
    notes                 TEXT,
    created_at            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_edl_assignment FOREIGN KEY (machine_assignment_id) REFERENCES machine_assignments (id),
    UNIQUE KEY uk_assignment_date (machine_assignment_id, log_date)
);
