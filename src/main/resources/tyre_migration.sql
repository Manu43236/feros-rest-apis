-- ============================================================
-- FEROS Tyre Management Module Migration
-- Run this manually before starting the application
-- ============================================================

USE feros_db;

-- Physical tyre inventory
CREATE TABLE IF NOT EXISTS tyres (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  serial_number VARCHAR(100) NOT NULL,
  brand VARCHAR(100) NOT NULL,
  size VARCHAR(100) NOT NULL,
  tyre_type ENUM('RADIAL','BIAS','TUBELESS','TUBE_TYPE') NOT NULL,
  ply_rating VARCHAR(20) NULL,
  purchase_date DATE NULL,
  purchase_cost DECIMAL(12,2) NULL,
  status ENUM('IN_STOCK','FITTED','RETREADING','SCRAPPED','DISPOSED') NOT NULL DEFAULT 'IN_STOCK',
  retread_count INT NOT NULL DEFAULT 0,
  total_lifetime_km DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  notes TEXT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tenant_serial (tenant_id, serial_number),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Position layout per vehicle (dynamic, no hardcoded max)
CREATE TABLE IF NOT EXISTS vehicle_tyre_positions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  position_code VARCHAR(20) NOT NULL,
  position_type ENUM('STEER','DRIVE','TRAILER','SPARE') NOT NULL,
  display_order INT NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_vehicle_position (vehicle_id, position_code),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

-- Batch header for a rotation event
CREATE TABLE IF NOT EXISTS tyre_rotation_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  rotation_date DATE NOT NULL,
  odometer_km DECIMAL(12,2) NOT NULL,
  performed_by_id BIGINT NOT NULL,
  notes TEXT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
  FOREIGN KEY (performed_by_id) REFERENCES users(id)
);

-- One row per tyre-on-position stint
CREATE TABLE IF NOT EXISTS vehicle_tyre_fittings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  tyre_id BIGINT NOT NULL,
  position_id BIGINT NOT NULL,
  fitted_at_km DECIMAL(12,2) NOT NULL,
  fitted_date DATE NOT NULL,
  fitted_by_user_id BIGINT NOT NULL,
  removed_at_km DECIMAL(12,2) NULL,
  removed_date DATE NULL,
  removal_reason ENUM('ROTATION','WORN','PUNCTURE','DAMAGE','RETREAD','SCRAP','OTHER') NULL,
  removed_by_user_id BIGINT NULL,
  rotation_log_id BIGINT NULL,
  km_driven DECIMAL(12,2) GENERATED ALWAYS AS (
    CASE WHEN removed_at_km IS NOT NULL THEN removed_at_km - fitted_at_km ELSE NULL END
  ) STORED,
  notes TEXT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
  FOREIGN KEY (tyre_id) REFERENCES tyres(id),
  FOREIGN KEY (position_id) REFERENCES vehicle_tyre_positions(id),
  FOREIGN KEY (fitted_by_user_id) REFERENCES users(id),
  FOREIGN KEY (removed_by_user_id) REFERENCES users(id),
  FOREIGN KEY (rotation_log_id) REFERENCES tyre_rotation_logs(id)
);

-- One row per tyre moved in a rotation
CREATE TABLE IF NOT EXISTS tyre_rotation_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rotation_log_id BIGINT NOT NULL,
  tyre_id BIGINT NOT NULL,
  from_position_id BIGINT NOT NULL,
  to_position_id BIGINT NOT NULL,
  old_fitting_id BIGINT NOT NULL,
  new_fitting_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (rotation_log_id) REFERENCES tyre_rotation_logs(id),
  FOREIGN KEY (tyre_id) REFERENCES tyres(id),
  FOREIGN KEY (from_position_id) REFERENCES vehicle_tyre_positions(id),
  FOREIGN KEY (to_position_id) REFERENCES vehicle_tyre_positions(id),
  FOREIGN KEY (old_fitting_id) REFERENCES vehicle_tyre_fittings(id),
  FOREIGN KEY (new_fitting_id) REFERENCES vehicle_tyre_fittings(id)
);
