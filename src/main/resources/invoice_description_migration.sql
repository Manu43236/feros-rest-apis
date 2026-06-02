-- Add invoice_description column to tenants table
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS invoice_description TEXT;
