-- ============================================================
-- FEROS Vehicle Service Enhancement Migration
-- Adds: payer_type, warranty scenarios, insurance, compliance
-- Run this manually after service_migration.sql
-- ============================================================

-- Step 1: Rename EXTERNAL → THIRD_PARTY (update existing data first)
UPDATE vehicle_services SET service_type = 'THIRD_PARTY' WHERE service_type = 'EXTERNAL';

-- Step 2: Expand triggered_by enum
ALTER TABLE vehicle_services
  MODIFY COLUMN triggered_by ENUM('SCHEDULED','BREAKDOWN','ACCIDENT','COMPLIANCE','WARRANTY') NOT NULL;

-- Step 3: Expand service_type enum (EXTERNAL kept temporarily for safety, removed after data update above)
ALTER TABLE vehicle_services
  MODIFY COLUMN service_type ENUM('INTERNAL','THIRD_PARTY','OEM_CENTER') NOT NULL;

-- Step 4: Add new columns
ALTER TABLE vehicle_services
  ADD COLUMN payer_type             ENUM('OWN_EXPENSE','WARRANTY_OEM','WARRANTY_ANC','INSURANCE','AMC')
                                    NULL DEFAULT 'OWN_EXPENSE' AFTER service_type,
  ADD COLUMN total_cost             DECIMAL(10,2)   NULL AFTER payer_type,
  ADD COLUMN insurance_claim_no     VARCHAR(100)    NULL AFTER total_cost,
  ADD COLUMN insurance_claim_amt    DECIMAL(10,2)   NULL AFTER insurance_claim_no,
  ADD COLUMN certificate_number     VARCHAR(100)    NULL AFTER insurance_claim_amt,
  ADD COLUMN certificate_valid_until DATE           NULL AFTER certificate_number,
  ADD COLUMN is_escalated           BOOLEAN         NOT NULL DEFAULT FALSE AFTER certificate_valid_until;
