-- Change fill_date column from DATE to TIMESTAMP in vehicle_fuel_logs
ALTER TABLE vehicle_fuel_logs
    ALTER COLUMN fill_date TYPE TIMESTAMP USING fill_date::TIMESTAMP;
