-- ============================================================
-- FEROS Phase 3 Migration — E-way Bill fields on orders
-- Run manually on feros_db
-- ============================================================

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS eway_bill_number VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS eway_bill_date DATE NULL,
    ADD COLUMN IF NOT EXISTS eway_bill_valid_upto DATE NULL;
