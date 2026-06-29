-- Add can_access_vehicles flag to staff_profiles.
-- Defaults TRUE so all existing staff retain vehicle access.
-- Run on staging: pending
-- Run on prod: pending

ALTER TABLE staff_profiles
    ADD COLUMN can_access_vehicles BOOLEAN NOT NULL DEFAULT TRUE;
