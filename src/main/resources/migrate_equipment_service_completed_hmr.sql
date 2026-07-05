-- Add completed_hmr column to equipment_services
-- Stores the actual HMR at which the service was completed (separate from hmr_at_service which is when it was scheduled)
ALTER TABLE equipment_services
    ADD COLUMN completed_hmr DECIMAL(10, 2) NULL AFTER completed_date;
