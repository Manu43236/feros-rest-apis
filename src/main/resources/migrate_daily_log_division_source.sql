-- Add division snapshot + source tracking to daily logs
ALTER TABLE equipment_daily_logs
    ADD COLUMN division_name VARCHAR(100) NULL,
    ADD COLUMN source        VARCHAR(10)  NOT NULL DEFAULT 'MANUAL';
