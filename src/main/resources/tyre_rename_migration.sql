-- ============================================================
-- FEROS Tyre Rename Migration
-- Renames existing DB tables and columns from tire → tyre
-- Run this ONCE on the existing database
-- ============================================================

USE feros_db;

-- Step 1: Rename columns before renaming tables (drop FKs first if needed)

-- Rename tyre_type column in tires table
ALTER TABLE tires CHANGE tire_type tyre_type ENUM('RADIAL','BIAS','TUBELESS','TUBE_TYPE') NOT NULL;

-- Rename tyre_id column in vehicle_tire_fittings
ALTER TABLE vehicle_tire_fittings CHANGE tire_id tyre_id BIGINT NOT NULL;

-- Rename tyre_id column in tire_rotation_items
ALTER TABLE tire_rotation_items CHANGE tire_id tyre_id BIGINT NOT NULL;

-- Step 2: Rename tables
RENAME TABLE
  tires                  TO tyres,
  vehicle_tire_positions TO vehicle_tyre_positions,
  tire_rotation_logs     TO tyre_rotation_logs,
  vehicle_tire_fittings  TO vehicle_tyre_fittings,
  tire_rotation_items    TO tyre_rotation_items;
