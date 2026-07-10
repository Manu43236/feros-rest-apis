-- Equipment Service Task Types Master
CREATE TABLE IF NOT EXISTS equipment_service_task_types (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    is_active  TINYINT(1)   DEFAULT 1,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO equipment_service_task_types (name, is_active) VALUES
('Engine Oil Change', 1),
('Hydraulic Oil Change', 1),
('Air Filter Service', 1),
('Fuel Filter Service', 1),
('Greasing / Lubrication', 1),
('Track / Undercarriage Inspection', 1),
('Bucket / Boom Inspection', 1),
('Hydraulic Hose Replacement', 1),
('Coolant / Radiator Service', 1),
('Battery Check', 1),
('Electrical Fault Repair', 1),
('Engine Repair', 1),
('Tyre Change', 1),
('General Service', 1),
('Breakdown Repair', 1),
('Safety Inspection', 1),
('Swing Gear / Slew Ring Service', 1),
('Final Drive Service', 1);

-- Backfill any rows left with NULL is_active from an earlier seed run.
UPDATE equipment_service_task_types SET is_active = 1 WHERE is_active IS NULL;
