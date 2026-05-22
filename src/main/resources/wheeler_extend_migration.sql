-- Extend vehicle_types with 24–36 Wheeler entries
-- Skip if already exists (safe to re-run)

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '24 Wheeler', 50.00, 24, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '24 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '26 Wheeler', 55.00, 26, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '26 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '28 Wheeler', 60.00, 28, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '28 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '30 Wheeler', 65.00, 30, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '30 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '32 Wheeler', 70.00, 32, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '32 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '34 Wheeler', 75.00, 34, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '34 Wheeler');

INSERT INTO vehicle_types (name, capacity_in_tons, tyre_count, is_active)
SELECT '36 Wheeler', 80.00, 36, true WHERE NOT EXISTS (SELECT 1 FROM vehicle_types WHERE name = '36 Wheeler');
