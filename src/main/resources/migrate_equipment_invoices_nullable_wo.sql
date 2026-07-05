-- Make work_order_id nullable on equipment_invoices
-- Allows client-level invoices that span multiple work orders
-- Run on staging then prod

ALTER TABLE equipment_invoices MODIFY COLUMN work_order_id BIGINT NULL;
