-- ============================================================
-- FEROS Tyre Alert & Lifecycle Enhancement Migration
-- Run this manually before starting the application
-- ============================================================

USE feros_db;

-- Add tyre lifecycle columns to tyres table
ALTER TABLE tyres
  ADD COLUMN tyre_life_years INT NULL,
  ADD COLUMN expiry_date DATE NULL,
  ADD COLUMN max_lifetime_km DECIMAL(12,2) NULL,
  ADD COLUMN last_km_alert_km DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN retreader_name VARCHAR(255) NULL,
  ADD COLUMN expected_return_date DATE NULL;

-- Add tyre rotation interval to vehicles table
ALTER TABLE vehicles
  ADD COLUMN tyre_rotation_interval_km INT NULL;
