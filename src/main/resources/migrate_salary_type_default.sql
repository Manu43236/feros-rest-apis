-- Add DEFAULT 'MONTHLY' to salary_type column so staff profile inserts
-- don't fail when salary type is not provided at user creation time.
-- Run on staging: done (2026-06-28)
-- Run on prod: pending

ALTER TABLE staff_profiles
    MODIFY COLUMN salary_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY';
