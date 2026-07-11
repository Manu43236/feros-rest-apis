-- Equipment document types — ADDITIVE ONLY.
-- Global master (document_types), scoped applicable_for='EQUIPMENT' so they NEVER
-- appear in vehicle/driver document lists. Distinct names avoid the unique-name
-- collision with existing vehicle doc types (Insurance/RC/PUC/Permit/Fitness).
-- Idempotent: INSERT IGNORE skips rows whose unique name already exists.
-- is_active=1 explicitly (avoids the is_active NULL empty-dropdown gotcha).
--
-- IMPORTANT: applicable_for is a MySQL ENUM. Hibernate (ddl-auto=update) does NOT
-- add new values to an existing enum column, so the ALTER below is REQUIRED before
-- inserting 'EQUIPMENT' — otherwise the value is truncated to '' (error 1265).
-- The ALTER only ADDS 'EQUIPMENT'; VEHICLE/DRIVER/BOTH are preserved (vehicles unaffected).

ALTER TABLE document_types
  MODIFY COLUMN applicable_for ENUM('VEHICLE','DRIVER','BOTH','EQUIPMENT') NOT NULL;

INSERT IGNORE INTO document_types (name, applicable_for, is_active, allow_multiple, created_at, updated_at) VALUES
  ('Equipment Insurance',           'EQUIPMENT', 1, 1, NOW(), NOW()),
  ('Equipment Fitness Certificate', 'EQUIPMENT', 1, 1, NOW(), NOW()),
  ('Crane Load-Test Certificate',   'EQUIPMENT', 1, 1, NOW(), NOW()),
  ('Equipment Registration (RC)',   'EQUIPMENT', 1, 1, NOW(), NOW()),
  ('Equipment PUC / Pollution',     'EQUIPMENT', 1, 1, NOW(), NOW()),
  ('Equipment Permit',              'EQUIPMENT', 1, 1, NOW(), NOW());

-- Repair any rows truncated to '' by a prior run (before the ALTER was added).
UPDATE document_types SET applicable_for = 'EQUIPMENT'
 WHERE applicable_for = '' AND name IN (
   'Equipment Insurance','Equipment Fitness Certificate','Crane Load-Test Certificate',
   'Equipment Registration (RC)','Equipment PUC / Pollution','Equipment Permit');
