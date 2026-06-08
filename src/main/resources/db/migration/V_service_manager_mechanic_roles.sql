-- ============================================================
-- Phase 1: Service Manager & Mechanic Role Migration
-- Renames SERVICE_MEN -> SERVICE_MANAGER, adds MECHANIC role,
-- and extends vehicle_service_tasks for mechanic assignment.
-- ============================================================

-- 1a. Widen the name column so SERVICE_MANAGER (15 chars) fits
ALTER TABLE roles MODIFY COLUMN name VARCHAR(30) NOT NULL;

-- 1b. Rename SERVICE_MEN role to SERVICE_MANAGER
UPDATE roles
SET name        = 'SERVICE_MANAGER',
    description = 'Vehicle Service Manager'
WHERE name = 'SERVICE_MEN';

-- 2. Add MECHANIC role (mobile-only, no web access)
INSERT IGNORE INTO roles (name, description, is_active) VALUES
('MECHANIC', 'Mechanic', 1);

-- 3. Add mechanic assignment columns to vehicle_service_tasks
ALTER TABLE vehicle_service_tasks
    ADD COLUMN IF NOT EXISTS assigned_mechanic_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS mechanic_closed_at   TIMESTAMP NULL;

-- 4. Add FK constraint for assigned_mechanic_id
ALTER TABLE vehicle_service_tasks
    ADD CONSTRAINT fk_vst_assigned_mechanic
        FOREIGN KEY (assigned_mechanic_id) REFERENCES users(id);
