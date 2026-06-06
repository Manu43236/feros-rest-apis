-- Add invoice_no and invoice_date to spare_parts_transactions for supplier invoice tracking
ALTER TABLE spare_parts_transactions
    ADD COLUMN invoice_no VARCHAR(100) NULL,
    ADD COLUMN invoice_date DATE NULL;
