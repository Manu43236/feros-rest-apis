-- Add OPERATOR role to roles table
-- Run on staging and prod after deploying equipment module
INSERT IGNORE INTO roles (name, description, is_active) VALUES ('OPERATOR', 'Machine Operator', true);
