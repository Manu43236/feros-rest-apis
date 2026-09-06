CREATE TABLE IF NOT EXISTS lease_driver_assignment_logs (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    lease_vehicle_assignment_id BIGINT       NOT NULL,
    driver_staff_id             BIGINT       NULL,
    assigned_at                 DATETIME     NOT NULL,
    unassigned_at               DATETIME     NULL,
    assigned_by_user_id         BIGINT       NULL,
    tenant_id                   BIGINT       NOT NULL,
    created_at                  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ldal_assignment FOREIGN KEY (lease_vehicle_assignment_id)
        REFERENCES lease_vehicle_assignments(id),
    CONSTRAINT fk_ldal_driver    FOREIGN KEY (driver_staff_id)
        REFERENCES staff_profiles(id),
    CONSTRAINT fk_ldal_assigned_by FOREIGN KEY (assigned_by_user_id)
        REFERENCES users(id),
    CONSTRAINT fk_ldal_tenant   FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    INDEX idx_ldal_assignment (lease_vehicle_assignment_id),
    INDEX idx_ldal_driver     (driver_staff_id),
    INDEX idx_ldal_tenant     (tenant_id),
    INDEX idx_ldal_active     (tenant_id, unassigned_at)
);
