-- ─── Subscription Per-Vehicle Pricing Migration ───────────────────────────────
-- Run after: subscription_redesign_migration.sql

-- 1. Add per-vehicle pricing + feature flags to subscription_plans
ALTER TABLE subscription_plans
    ADD COLUMN price_per_vehicle DECIMAL(10,2)  DEFAULT NULL,
    ADD COLUMN min_vehicles      INT            NOT NULL DEFAULT 1,
    ADD COLUMN max_vehicles      INT            NOT NULL DEFAULT -1,
    ADD COLUMN has_fuel_logs         BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_meter_readings    BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_vehicle_services  BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_attendance        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_payroll           BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_inventory         BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_reports           BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN has_credit_notes      BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Add vehicle count + rate snapshot to subscription_history
--    Also make end_date nullable (free plan has no expiry)
ALTER TABLE subscription_history
    ADD COLUMN vehicle_count      INT            DEFAULT NULL,
    ADD COLUMN price_per_vehicle  DECIMAL(10,2)  DEFAULT NULL,
    MODIFY COLUMN end_date DATE NULL;

-- 3. Add vehicle count + rate snapshot to subscription_invoices
ALTER TABLE subscription_invoices
    ADD COLUMN vehicle_count      INT            DEFAULT NULL,
    ADD COLUMN price_per_vehicle  DECIMAL(10,2)  DEFAULT NULL;

-- 4. Deactivate any old fixed-price plans
UPDATE subscription_plans SET is_active = FALSE WHERE is_active = TRUE;

-- 5. Insert new per-vehicle plans
INSERT INTO subscription_plans
    (name, price_per_vehicle, min_vehicles, max_vehicles, max_lorries, max_users,
     price_monthly, price_yearly,
     has_fuel_logs, has_meter_readings, has_vehicle_services,
     has_attendance, has_payroll, has_inventory, has_reports, has_credit_notes,
     is_active)
VALUES
--   name         ₹/veh  min  max  maxL  maxU   pm     py    fuel   meter  svc    att    pay    inv    rep    cr
    ('Free',        0.00,  1,   2,    2,  10,   0.00,  0.00, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
    ('Starter',   499.00,  3,  19,   -1,  -1,   0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Growth',    449.00, 20,  49,   -1,  -1,   0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Business',  399.00, 50,  99,   -1,  -1,   0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Scale',     349.00,100, 249,   -1,  -1,   0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Enterprise',299.00,250, 499,   -1,  -1,   0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE);
