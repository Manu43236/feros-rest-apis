-- Migration: rename lorry_count → total_slots_count, drop equipment_count
-- Run manually on staging first, then prod
-- Date: 2026-06-26

ALTER TABLE tenants
    RENAME COLUMN lorry_count TO total_slots_count;

ALTER TABLE tenants
    DROP COLUMN equipment_count;
