-- ============================================================
-- Migration: Backfill driver_id / cleaner_id on lrs table
-- Context : Staff-to-Vehicle assignment model refactor.
--           Previously drivers/cleaners were linked to orders
--           via order_staff_allocations. They are now linked
--           directly to vehicles (current_driver_id / current_cleaner_id
--           on the vehicles table), and LRs snapshot the
--           driver/cleaner at creation time.
--
-- This script backfills driver_id and cleaner_id on existing
-- LR rows by reading the most recent active DRIVER / CLEANER
-- allocation from order_staff_allocations for the same
-- vehicle_allocation_id.
--
-- Run ONCE before deploying the new code to production.
-- Safe to re-run (WHERE ... IS NULL guard prevents double-write).
-- ============================================================

-- ── 0. Add columns (Hibernate would do this on app start,
--        but we need them now to run the backfill) ────────────
ALTER TABLE lrs
    ADD COLUMN IF NOT EXISTS driver_id  BIGINT NULL,
    ADD COLUMN IF NOT EXISTS cleaner_id BIGINT NULL;

-- Optional: add FK constraints (safe to skip if app will manage them)
-- ALTER TABLE lrs ADD CONSTRAINT fk_lr_driver  FOREIGN KEY (driver_id)  REFERENCES users(id);
-- ALTER TABLE lrs ADD CONSTRAINT fk_lr_cleaner FOREIGN KEY (cleaner_id) REFERENCES users(id);


-- ── 1. Preview (run SELECT first to verify counts) ──────────
SELECT
    l.id          AS lr_id,
    l.lr_number,
    l.vehicle_allocation_id,
    drv.user_id   AS driver_user_id,
    clr.user_id   AS cleaner_user_id
FROM lrs l
LEFT JOIN (
    SELECT osa.vehicle_allocation_id, osa.user_id
    FROM order_staff_allocations osa
    JOIN roles r ON r.id = osa.role_id
    WHERE r.name = 'DRIVER'
      AND osa.is_active = 1
) drv ON drv.vehicle_allocation_id = l.vehicle_allocation_id
LEFT JOIN (
    SELECT osa.vehicle_allocation_id, osa.user_id
    FROM order_staff_allocations osa
    JOIN roles r ON r.id = osa.role_id
    WHERE r.name = 'CLEANER'
      AND osa.is_active = 1
) clr ON clr.vehicle_allocation_id = l.vehicle_allocation_id
WHERE l.driver_id IS NULL OR l.cleaner_id IS NULL;


-- ── 2 & 3. Backfill driver_id + cleaner_id ──────────────────
SET SQL_SAFE_UPDATES = 0;

UPDATE lrs l
JOIN order_staff_allocations osa
    ON osa.vehicle_allocation_id = l.vehicle_allocation_id
JOIN roles r
    ON r.id = osa.role_id
SET l.driver_id = osa.user_id
WHERE r.name    = 'DRIVER'
  AND osa.is_active = 1
  AND l.driver_id  IS NULL;

UPDATE lrs l
JOIN order_staff_allocations osa
    ON osa.vehicle_allocation_id = l.vehicle_allocation_id
JOIN roles r
    ON r.id = osa.role_id
SET l.cleaner_id = osa.user_id
WHERE r.name     = 'CLEANER'
  AND osa.is_active = 1
  AND l.cleaner_id IS NULL;

SET SQL_SAFE_UPDATES = 1;


-- ── 4. Verify result ─────────────────────────────────────────
SELECT
    l.id,
    l.lr_number,
    l.driver_id,
    d.name   AS driver_name,
    l.cleaner_id,
    c.name   AS cleaner_name
FROM lrs l
LEFT JOIN users d ON d.id = l.driver_id
LEFT JOIN users c ON c.id = l.cleaner_id
ORDER BY l.id;
