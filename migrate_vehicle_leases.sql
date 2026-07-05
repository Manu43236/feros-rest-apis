-- Vehicle Lease Module Migration
-- Run on staging first, then prod

-- 1. Add ON_LEASE to vehicle_statuses master table
INSERT INTO vehicle_statuses (name, status_type, is_active, created_at, updated_at)
VALUES ('On Lease', 'ON_LEASE', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 2. Create vehicle_leases table
CREATE TABLE IF NOT EXISTS vehicle_leases (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    lease_number    VARCHAR(50) UNIQUE,
    client_id       BIGINT NOT NULL,
    site            VARCHAR(255),
    start_date      DATE NOT NULL,
    end_date        DATE,
    rate_type       VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      DATETIME,
    updated_at      DATETIME,
    CONSTRAINT fk_vl_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_vl_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- 3. Create lease_vehicle_assignments table
CREATE TABLE IF NOT EXISTS lease_vehicle_assignments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    lease_id            BIGINT NOT NULL,
    vehicle_id          BIGINT NOT NULL,
    driver_staff_id     BIGINT,
    rate_per_vehicle    DECIMAL(12,2) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    odometer_at_start   DECIMAL(10,2),
    odometer_at_end     DECIMAL(10,2),
    is_active           TINYINT(1) DEFAULT 1,
    notes               VARCHAR(500),
    created_at          DATETIME,
    updated_at          DATETIME,
    CONSTRAINT fk_lva_lease   FOREIGN KEY (lease_id)        REFERENCES vehicle_leases(id),
    CONSTRAINT fk_lva_vehicle FOREIGN KEY (vehicle_id)      REFERENCES vehicles(id),
    CONSTRAINT fk_lva_driver  FOREIGN KEY (driver_staff_id) REFERENCES staff_profiles(id)
);
