-- Add operator fields to machine_assignments table
ALTER TABLE machine_assignments
    ADD COLUMN operator_type VARCHAR(20),
    ADD COLUMN operator_staff_id BIGINT,
    ADD COLUMN hired_operator_name VARCHAR(100),
    ADD COLUMN hired_operator_phone VARCHAR(20),
    ADD CONSTRAINT fk_ma_operator_staff FOREIGN KEY (operator_staff_id) REFERENCES staff_profiles(id);
