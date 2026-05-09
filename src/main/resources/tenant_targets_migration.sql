-- ============================================================
-- FEROS Tenant Targets Migration
-- Run this manually before starting the application
-- ============================================================

USE feros_db;

CREATE TABLE IF NOT EXISTS tenant_targets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  year INT NOT NULL,
  month INT NOT NULL,
  target_trips INT NULL,
  target_tons DECIMAL(12,2) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tenant_year_month (tenant_id, year, month),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
