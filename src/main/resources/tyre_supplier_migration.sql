-- Add supplier and invoice reference fields to tyres table
ALTER TABLE tyres
    ADD COLUMN supplier_name  VARCHAR(255) NULL,
    ADD COLUMN invoice_number VARCHAR(100) NULL;
