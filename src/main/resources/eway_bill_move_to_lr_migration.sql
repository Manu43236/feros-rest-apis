-- Migration: Move eWay Bill from orders table to lrs table
-- Date: 2026-05-25

-- Step 1: Add eWay bill columns to lrs table
ALTER TABLE lrs
    ADD COLUMN IF NOT EXISTS eway_bill_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS eway_bill_date DATE,
    ADD COLUMN IF NOT EXISTS eway_bill_valid_upto DATE;

-- Step 2: Drop eWay bill columns from orders table
ALTER TABLE orders
    DROP COLUMN IF EXISTS eway_bill_number,
    DROP COLUMN IF EXISTS eway_bill_date,
    DROP COLUMN IF EXISTS eway_bill_valid_upto;
