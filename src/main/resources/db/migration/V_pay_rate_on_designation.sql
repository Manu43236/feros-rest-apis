-- Add pay_per_day directly on designations (replaces separate pay_rates table)
ALTER TABLE designations
    ADD COLUMN IF NOT EXISTS pay_per_day DECIMAL(10, 2) NULL;

-- Drop the now-obsolete pay_rates table
DROP TABLE IF EXISTS pay_rates;
