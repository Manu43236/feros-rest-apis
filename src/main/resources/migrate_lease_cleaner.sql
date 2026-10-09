-- Lease cleaner parity: cleaner on lease_vehicle_assignments + a cleaner assignment log table.
-- Hibernate (ddl-auto=update) creates these on deploy; this is the explicit staging/prod path.

ALTER TABLE lease_vehicle_assignments
    ADD COLUMN IF NOT EXISTS cleaner_staff_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS client_cleaner_name VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS lease_cleaner_assignment_logs (
    id                         BIGINT       NOT NULL AUTO_INCREMENT,
    lease_vehicle_assignment_id BIGINT      NOT NULL,
    cleaner_staff_id           BIGINT       NULL,
    assigned_at                DATETIME(6)  NOT NULL,
    unassigned_at              DATETIME(6)  NULL,
    assigned_by_user_id        BIGINT       NULL,
    tenant_id                  BIGINT       NOT NULL,
    created_at                 DATETIME(6)  NULL,
    updated_at                 DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_lca_assignment (lease_vehicle_assignment_id),
    KEY idx_lca_cleaner (cleaner_staff_id),
    KEY idx_lca_tenant_open (tenant_id, unassigned_at)
) ENGINE=InnoDB;
