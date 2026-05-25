-- Add model and gross_vehicle_weight columns to vehicles
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS model VARCHAR(100);
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS gross_vehicle_weight DECIMAL(10,2);

-- Extend vehicle_types to 40 Wheeler
INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '38 Wheeler', 85.00, 38, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '38 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '40 Wheeler', 90.00, 40, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '40 Wheeler');
