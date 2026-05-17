-- ============================================================
-- Vehicle Compliance Refactor Migration
-- Moves all compliance fields from vehicles table → vehicle_documents
-- ============================================================

-- Step 1: Add new columns to vehicle_documents
ALTER TABLE vehicle_documents
    ADD COLUMN IF NOT EXISTS issuer_name VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS permit_type ENUM('NATIONAL', 'STATE') NULL;

-- Step 2: Ensure compliance document types exist in global master
INSERT INTO document_types (name, applicable_for, applicable_roles, is_active, created_at, updated_at)
VALUES
    ('RC',                  'VEHICLE', NULL, 1, NOW(), NOW()),
    ('Insurance',           'VEHICLE', NULL, 1, NOW(), NOW()),
    ('Permit',              'VEHICLE', NULL, 1, NOW(), NOW()),
    ('Fitness Certificate', 'VEHICLE', NULL, 1, NOW(), NOW()),
    ('PUC',                 'VEHICLE', NULL, 1, NOW(), NOW()),
    ('Road Tax',            'VEHICLE', NULL, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE is_active = 1;

-- Step 3: Migrate existing compliance data from vehicles → vehicle_documents
-- RC
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, document_number, issue_date, expiry_date, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'RC' LIMIT 1),
       v.rc_number, NULL, v.rc_expiry_date,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.rc_number IS NOT NULL OR v.rc_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- Insurance
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, document_number, issuer_name, issue_date, expiry_date, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'Insurance' LIMIT 1),
       v.insurance_policy_number, v.insurance_company_name,
       v.insurance_start_date, v.insurance_expiry_date,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.insurance_policy_number IS NOT NULL OR v.insurance_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- Permit
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, document_number, issue_date, expiry_date, permit_type, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'Permit' LIMIT 1),
       v.permit_number, v.permit_start_date, v.permit_expiry_date,
       v.permit_type,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.permit_number IS NOT NULL OR v.permit_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- Fitness Certificate
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, document_number, expiry_date, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'Fitness Certificate' LIMIT 1),
       v.fitness_certificate_number, v.fitness_certificate_expiry_date,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.fitness_certificate_number IS NOT NULL OR v.fitness_certificate_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- PUC
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, document_number, expiry_date, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'PUC' LIMIT 1),
       v.puc_number, v.puc_expiry_date,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.puc_number IS NOT NULL OR v.puc_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- Road Tax
INSERT INTO vehicle_documents
    (tenant_id, vehicle_id, document_type_id, issue_date, expiry_date, is_verified, is_active, created_at, updated_at)
SELECT v.tenant_id, v.id,
       (SELECT id FROM document_types WHERE name = 'Road Tax' LIMIT 1),
       v.road_tax_paid_date, v.road_tax_expiry_date,
       0, 1, NOW(), NOW()
FROM vehicles v
WHERE (v.road_tax_paid_date IS NOT NULL OR v.road_tax_expiry_date IS NOT NULL)
  AND v.is_active = 1;

-- Step 4: Drop compliance columns from vehicles
ALTER TABLE vehicles
    DROP COLUMN IF EXISTS rc_number,
    DROP COLUMN IF EXISTS rc_expiry_date,
    DROP COLUMN IF EXISTS insurance_company_name,
    DROP COLUMN IF EXISTS insurance_policy_number,
    DROP COLUMN IF EXISTS insurance_start_date,
    DROP COLUMN IF EXISTS insurance_expiry_date,
    DROP COLUMN IF EXISTS permit_number,
    DROP COLUMN IF EXISTS permit_type,
    DROP COLUMN IF EXISTS permit_start_date,
    DROP COLUMN IF EXISTS permit_expiry_date,
    DROP COLUMN IF EXISTS fitness_certificate_number,
    DROP COLUMN IF EXISTS fitness_certificate_expiry_date,
    DROP COLUMN IF EXISTS puc_number,
    DROP COLUMN IF EXISTS puc_expiry_date,
    DROP COLUMN IF EXISTS road_tax_paid_date,
    DROP COLUMN IF EXISTS road_tax_expiry_date;
