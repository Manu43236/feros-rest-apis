-- Add rejection fields to lr_trip_expenses
ALTER TABLE lr_trip_expenses
    ADD COLUMN rejected_by_id BIGINT NULL,
    ADD COLUMN rejected_at DATETIME NULL,
    ADD COLUMN rejection_reason TEXT NULL,
    ADD CONSTRAINT fk_trip_expense_rejected_by FOREIGN KEY (rejected_by_id) REFERENCES users(id);
