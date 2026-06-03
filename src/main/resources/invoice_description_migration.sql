-- Move invoice_description from tenants to tenant_settings
ALTER TABLE tenants DROP COLUMN IF EXISTS invoice_description;
ALTER TABLE tenant_settings ADD COLUMN IF NOT EXISTS invoice_description TEXT;
