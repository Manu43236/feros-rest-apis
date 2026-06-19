-- Monthly Targets Migration
-- Adds monthly trip and tonnage target fields to tenant_settings

ALTER TABLE tenant_settings
    ADD COLUMN monthly_trip_target INT DEFAULT NULL COMMENT 'Monthly trip completion target set by tenant',
    ADD COLUMN monthly_ton_target DECIMAL(38,2) DEFAULT NULL COMMENT 'Monthly tonnage completion target set by tenant';
