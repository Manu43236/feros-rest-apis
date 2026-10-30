-- Optional name fields for client's own operator/cleaner on shift assignments.
-- Null = no name provided (still valid — name is optional).
ALTER TABLE machine_shift_assignments
    ADD COLUMN IF NOT EXISTS client_operator_name VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS client_cleaner_name  VARCHAR(100) NULL;
