-- Per-machine billing rate on machine_assignments
-- Overrides the work order rate for invoicing when set.
-- Run on staging then prod.

ALTER TABLE machine_assignments
    ADD COLUMN IF NOT EXISTS rate_type   VARCHAR(20)     NULL AFTER division_name,
    ADD COLUMN IF NOT EXISTS rate_amount DECIMAL(12, 2)  NULL AFTER rate_type;
