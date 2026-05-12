-- ============================================================
-- FEROS Sprint 2 Migration
-- Adds GPS + selfie columns to attendance table
-- Run manually before starting the application
-- ============================================================

ALTER TABLE attendance
  ADD COLUMN IF NOT EXISTS selfie_url  VARCHAR(500)    NULL,
  ADD COLUMN IF NOT EXISTS latitude    DECIMAL(10, 7)  NULL,
  ADD COLUMN IF NOT EXISTS longitude   DECIMAL(10, 7)  NULL;
