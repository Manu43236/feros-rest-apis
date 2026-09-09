-- Add IN_SERVICE vehicle status (scheduled / non-breakdown maintenance).
-- Safe to run multiple times — INSERT IGNORE skips duplicates.
INSERT IGNORE INTO vehicle_statuses (name, status_type, is_active, created_at, updated_at)
VALUES ('In Service', 'IN_SERVICE', 1, NOW(), NOW());
