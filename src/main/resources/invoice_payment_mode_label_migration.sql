-- Add payment_mode_label column to invoice_payments
-- Used when payment_mode = 'OTHER' to store a custom description (e.g. "Fuel", "Kind")
ALTER TABLE invoice_payments
    ADD COLUMN payment_mode_label VARCHAR(100) NULL;
