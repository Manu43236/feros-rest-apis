-- Attendance mark-out migration
-- Adds marked_out_at column to attendance table for duty time tracking

ALTER TABLE attendance ADD COLUMN IF NOT EXISTS marked_out_at TIMESTAMP NULL;
