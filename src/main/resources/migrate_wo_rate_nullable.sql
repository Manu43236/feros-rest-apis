-- Drop rate and operator columns from work_orders
-- These fields now live on machine_assignments (per-machine level)
ALTER TABLE work_orders
    DROP FOREIGN KEY fk_wo_operator_staff,
    DROP COLUMN rate_type,
    DROP COLUMN rate_amount,
    DROP COLUMN shift_hours,
    DROP COLUMN overtime_rate_per_hour,
    DROP COLUMN operator_type,
    DROP COLUMN operator_staff_id,
    DROP COLUMN hired_operator_name,
    DROP COLUMN hired_operator_phone,
    DROP COLUMN operator_billing,
    DROP COLUMN operator_rate_per_day;
