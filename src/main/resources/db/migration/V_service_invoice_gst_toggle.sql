-- Add GST toggle for service invoices — allows tenants to opt out of GST on internal service invoices
ALTER TABLE tenant_settings
    ADD COLUMN service_invoice_gst_enabled TINYINT(1) NOT NULL DEFAULT 1;
