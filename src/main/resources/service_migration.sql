-- ============================================================
-- FEROS Vehicle Service Module Migration
-- Run this manually before starting the application
-- ============================================================

DROP TABLE IF EXISTS vehicle_service_tasks;
DROP TABLE IF EXISTS vehicle_services;

-- Global master: service task types
CREATE TABLE IF NOT EXISTS service_task_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO service_task_types (name) VALUES
('Oil Change'), ('Filter Change'), ('Air Filter Change'),
('Tyre Change'), ('Brake Service'), ('Engine Repair'),
('Electrical'), ('Body Repair'), ('General Service'),
('Coolant Change'), ('Battery Check');

-- New vehicle_services
CREATE TABLE vehicle_services (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  vehicle_id BIGINT NOT NULL,
  service_number VARCHAR(50) UNIQUE,
  triggered_by ENUM('SCHEDULED','BREAKDOWN') NOT NULL,
  breakdown_id BIGINT NULL,
  service_type ENUM('INTERNAL','EXTERNAL') NOT NULL,
  vendor_name VARCHAR(200) NULL,
  location VARCHAR(300) NULL,
  status ENUM('OPEN','COMPLETED') NOT NULL DEFAULT 'OPEN',
  due_at_odometer INT NULL,
  service_date DATE NULL,
  completed_date DATE NULL,
  odometer INT NULL,
  notes TEXT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

-- Service tasks
CREATE TABLE vehicle_service_tasks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  service_id BIGINT NOT NULL,
  task_type_id BIGINT NULL,
  custom_name VARCHAR(200) NULL,
  is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
  frequency_km INT NULL,
  cost DECIMAL(10,2) NULL,
  status ENUM('PENDING','COMPLETED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (service_id) REFERENCES vehicle_services(id),
  FOREIGN KEY (task_type_id) REFERENCES service_task_types(id)
);

