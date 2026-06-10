-- ============================================================
-- FEROS Tyre Retread & Scrap Enhancement Migration
-- Run this manually before starting the application
-- Zero impact on existing tyre records (all additive)
-- ============================================================

USE feros_db;

-- Add new columns to tyres table (all nullable / have defaults — no impact on existing rows)
ALTER TABLE tyres
  ADD COLUMN purchase_condition ENUM('NEW','SECOND_HAND','RETREADED') NOT NULL DEFAULT 'NEW',
  ADD COLUMN km_at_purchase DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN total_retreading_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN scrap_reason VARCHAR(500) NULL,
  ADD COLUMN scrap_date DATE NULL;

-- Retread history log — one row per retread cycle per tyre
CREATE TABLE IF NOT EXISTS tyre_retread_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  tyre_id BIGINT NOT NULL,
  retread_number INT NOT NULL,
  sent_date DATE NULL,
  return_date DATE NULL,
  retreader_name VARCHAR(255) NULL,
  km_at_send DECIMAL(12,2) NULL,
  retreading_cost DECIMAL(12,2) NULL,
  new_max_lifetime_km DECIMAL(12,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (tyre_id) REFERENCES tyres(id)
);


