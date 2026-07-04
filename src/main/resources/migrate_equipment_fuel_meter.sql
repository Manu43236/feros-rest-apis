-- Equipment Fuel Logs
CREATE TABLE IF NOT EXISTS equipment_fuel_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    equipment_id    BIGINT NOT NULL,
    fill_date       DATE NOT NULL,
    litres_filled   DECIMAL(10,2) NOT NULL,
    hmr_at_fill     DECIMAL(10,2),
    cost_per_litre  DECIMAL(10,2),
    total_cost      DECIMAL(12,2),
    is_full_tank    BOOLEAN DEFAULT FALSE,
    payment_mode    VARCHAR(30),
    fuel_station    VARCHAR(100),
    notes           TEXT,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    CONSTRAINT fk_efl_tenant    FOREIGN KEY (tenant_id)    REFERENCES tenants(id),
    CONSTRAINT fk_efl_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);

-- Equipment Meter Readings
CREATE TABLE IF NOT EXISTS equipment_meter_readings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    equipment_id    BIGINT NOT NULL,
    reading_date    DATE NOT NULL,
    reading_value   DECIMAL(10,2) NOT NULL,
    notes           TEXT,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    CONSTRAINT fk_emr_tenant    FOREIGN KEY (tenant_id)    REFERENCES tenants(id),
    CONSTRAINT fk_emr_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);
