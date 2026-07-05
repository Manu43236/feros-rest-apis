-- Equipment Service Task Types Master
CREATE TABLE IF NOT EXISTS equipment_service_task_types (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    is_active  TINYINT(1)   DEFAULT 1,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO equipment_service_task_types (name) VALUES
('Engine Oil Change'),
('Hydraulic Oil Change'),
('Air Filter Service'),
('Fuel Filter Service'),
('Greasing / Lubrication'),
('Track / Undercarriage Inspection'),
('Bucket / Boom Inspection'),
('Hydraulic Hose Replacement'),
('Coolant / Radiator Service'),
('Battery Check'),
('Electrical Fault Repair'),
('Engine Repair'),
('Tyre Change'),
('General Service'),
('Breakdown Repair'),
('Safety Inspection'),
('Swing Gear / Slew Ring Service'),
('Final Drive Service');
