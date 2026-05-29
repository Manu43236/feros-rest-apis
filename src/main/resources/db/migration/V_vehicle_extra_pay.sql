-- Add extra pay fields to vehicles
ALTER TABLE vehicles
    ADD COLUMN  extra_pay_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN  extra_pay_per_day  DECIMAL(10,2) NULL;

-- Add vehicle_extra_pay to payroll
ALTER TABLE payroll
    ADD COLUMN  vehicle_extra_pay  DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Vehicle-staff assignment history
CREATE TABLE  vehicle_staff_assignments (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants(id),
    vehicle_id      BIGINT       NOT NULL REFERENCES vehicles(id),
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    assigned_from   DATE         NOT NULL,
    assigned_to     DATE         NULL,
    assigned_by     BIGINT       NOT NULL REFERENCES users(id),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX  idx_vsa_user_tenant ON vehicle_staff_assignments(user_id, tenant_id);
CREATE INDEX  idx_vsa_vehicle     ON vehicle_staff_assignments(vehicle_id);
