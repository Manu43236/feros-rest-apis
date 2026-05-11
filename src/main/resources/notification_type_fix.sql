-- Fix: notifications.type column was created as a MySQL ENUM with only the original
-- values. New enum values added later (UPGRADE_REQUEST, SUBSCRIPTION_SUSPENDED,
-- TYRE_FITTED, TYRE_ROTATION, TYRE_EXPIRY, TYRE_ROTATION_DUE) cause
-- "Data truncated" on insert. Convert to VARCHAR(50) to support all current
-- and future NotificationType values without schema changes.

ALTER TABLE notifications
    MODIFY COLUMN type VARCHAR(50) NOT NULL;
