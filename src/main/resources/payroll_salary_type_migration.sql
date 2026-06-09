-- Add salary type and monthly salary support to staff profiles
ALTER TABLE staff_profiles
    ADD COLUMN salary_type VARCHAR(10) NOT NULL DEFAULT 'DAILY',
    ADD COLUMN monthly_salary DECIMAL(12, 2) NULL;

-- Add salary type snapshot columns to payroll records
ALTER TABLE payroll
    ADD COLUMN salary_type  VARCHAR(10)    NULL,
    ADD COLUMN monthly_salary DECIMAL(12, 2) NULL;

-- Backfill existing payroll records as DAILY
UPDATE payroll SET salary_type = 'DAILY' WHERE id > 0;
