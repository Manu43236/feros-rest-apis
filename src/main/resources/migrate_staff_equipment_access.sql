-- Add equipment module access flag to staff profiles
-- Run on staging then prod before deploying backend changes

ALTER TABLE staff_profiles
    ADD COLUMN can_access_equipment BOOLEAN NOT NULL DEFAULT FALSE;

-- For EQUIPMENT_ONLY tenants, set all existing staff to true
UPDATE staff_profiles sp
    JOIN users u ON u.id = sp.user_id
    JOIN tenants t ON t.id = u.tenant_id
SET sp.can_access_equipment = TRUE
WHERE t.module_type = 'EQUIPMENT_ONLY';
