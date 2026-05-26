-- Invoice: split tax into CGST + SGST columns
ALTER TABLE invoices
    ADD COLUMN cgst_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN sgst_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN cgst_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN sgst_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00;

-- Invoice: add IGST columns for inter-state transactions
ALTER TABLE invoices
    ADD COLUMN igst_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN igst_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00;

-- Tenant: optional transport HSN/SAC code (default 996791 for goods transport)
ALTER TABLE tenants
    ADD COLUMN transport_hsn_sac VARCHAR(20) NULL;

