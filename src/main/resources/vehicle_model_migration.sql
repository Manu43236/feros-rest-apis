-- Vehicle Models master table
CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT NOT NULL,
    vehicle_type_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    tyre_count INT NULL,
    capacity_in_tons DECIMAL(8,2) NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vm_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brands(id),
    CONSTRAINT fk_vm_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id)
);

-- Add vehicle_model_id to vehicles table
ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS vehicle_model_id BIGINT NULL,
    ADD CONSTRAINT fk_vehicle_model FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_models(id);

-- Seed common Indian truck models
-- These can be added via Super Admin UI, just examples:
-- INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons) VALUES ...
