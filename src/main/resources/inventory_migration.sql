-- ============================================================
-- FEROS Spare Parts Inventory Module Migration
-- Run this manually before starting the application
-- ============================================================

-- New roles
INSERT IGNORE INTO roles (name, description, is_active) VALUES
('SERVICE_MEN', 'Vehicle Service Technician', 1),
('STORE_KEEPER', 'Spare Parts Store Keeper', 1);

-- Tenant master: spare parts list
CREATE TABLE IF NOT EXISTS spare_parts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  part_number VARCHAR(100) NULL,
  category VARCHAR(100) NULL,
  unit VARCHAR(50) NOT NULL DEFAULT 'Pieces',
  min_stock_level INT NOT NULL DEFAULT 0,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Current stock levels per tenant per part (running balance)
CREATE TABLE IF NOT EXISTS spare_parts_inventory (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  spare_part_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tenant_part (tenant_id, spare_part_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id)
);

-- Parts requested per service record (approval workflow)
CREATE TABLE IF NOT EXISTS service_parts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  service_id BIGINT NOT NULL,
  spare_part_id BIGINT NOT NULL,
  quantity_requested INT NOT NULL,
  quantity_approved INT NULL,
  status ENUM('REQUESTED','APPROVED','REJECTED') NOT NULL DEFAULT 'REQUESTED',
  rejection_reason TEXT NULL,
  requested_by BIGINT NOT NULL,
  approved_by BIGINT NULL,
  approved_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (service_id) REFERENCES vehicle_services(id),
  FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id),
  FOREIGN KEY (requested_by) REFERENCES users(id),
  FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- All confirmed stock movements (only after approval or direct stock IN)
CREATE TABLE IF NOT EXISTS spare_parts_transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  spare_part_id BIGINT NOT NULL,
  transaction_type ENUM('IN','OUT','DAMAGE') NOT NULL,
  quantity INT NOT NULL,
  unit_cost DECIMAL(10,2) NULL,
  total_cost DECIMAL(10,2) NULL,
  reference_type ENUM('PURCHASE','SERVICE','DAMAGE','ADJUSTMENT') NOT NULL,
  service_part_id BIGINT NULL,
  supplier_name VARCHAR(200) NULL,
  notes TEXT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id),
  FOREIGN KEY (service_part_id) REFERENCES service_parts(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);
