-- Add IoT flag to vehicles
ALTER TABLE vehicles ADD COLUMN is_iot BOOLEAN NOT NULL DEFAULT FALSE;
