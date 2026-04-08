-- ============================================================
-- FEROS Meter Reading Module Migration
-- Run this manually before starting the application
-- ============================================================

CREATE TABLE IF NOT EXISTS vehicle_meter_readings (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id       BIGINT NOT NULL,
  vehicle_id      BIGINT NOT NULL,
  reading_km      DECIMAL(10,1) NOT NULL,
  reading_type    ENUM('TRIP_START','TRIP_END','FUEL_FILL','GENERAL') NOT NULL,
  lr_id           BIGINT NULL,
  photo_url       VARCHAR(500) NULL,
  recorded_by     BIGINT NOT NULL,
  recorded_at     DATETIME NOT NULL,
  notes           TEXT NULL,
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id)   REFERENCES tenants(id),
  FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id),
  FOREIGN KEY (lr_id)       REFERENCES lrs(id),
  FOREIGN KEY (recorded_by) REFERENCES users(id)
);
