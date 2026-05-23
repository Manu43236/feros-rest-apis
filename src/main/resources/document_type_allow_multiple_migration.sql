-- Add allow_multiple flag to document_types
-- When false, a vehicle/staff can only have one document of that type

ALTER TABLE document_types
    ADD COLUMN allow_multiple BOOLEAN NOT NULL DEFAULT TRUE;

-- RC is a unique document — only one per vehicle
UPDATE document_types
SET allow_multiple = FALSE
WHERE name = 'Registration Certificate (RC)';
