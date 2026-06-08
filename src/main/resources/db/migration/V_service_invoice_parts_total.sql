-- Add parts_total to service_invoices to track spare parts cost in the invoice
ALTER TABLE service_invoices
    ADD COLUMN parts_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
