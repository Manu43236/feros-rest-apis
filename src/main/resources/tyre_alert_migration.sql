-- ============================================================
-- FEROS Tyre Alert & Lifecycle Enhancement Migration
-- Run this manually before starting the application
-- ============================================================

USE feros_db;

-- Add tyre lifecycle columns to tires table
ALTER TABLE tires
  ADD COLUMN IF NOT EXISTS tyre_life_years INT NULL,
  ADD COLUMN IF NOT EXISTS expiry_date DATE NULL,
  ADD COLUMN IF NOT EXISTS max_lifetime_km DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS last_km_alert_km DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS retreader_name VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS expected_return_date DATE NULL;

-- Add tyre rotation interval to vehicles table
ALTER TABLE vehicles
  ADD COLUMN IF NOT EXISTS tyre_rotation_interval_km INT NULL;
