-- ============================================================
-- FEROS Service Timeline Migration
-- Run this manually before starting the application
-- ============================================================

-- Add startedAt timestamp to vehicle_services
ALTER TABLE vehicle_services
    ADD COLUMN started_at DATETIME NULL AFTER odometer;
