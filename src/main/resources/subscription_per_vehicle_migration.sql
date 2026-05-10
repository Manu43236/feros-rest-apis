-- ─── Subscription Per-Vehicle Pricing Migration ───────────────────────────────
-- NOTE: Column additions (ALTER TABLE) are handled automatically by Hibernate
--       (spring.jpa.hibernate.ddl-auto=update) when the app starts.
--       Only run the data operations below manually.

-- 1. Make end_date nullable in subscription_history (free plan has no expiry)
--    Skip if already nullable.
ALTER TABLE subscription_history MODIFY COLUMN end_date DATE NULL;

-- 2. Deactivate old fixed-price plans
UPDATE subscription_plans SET is_active = FALSE WHERE is_active = TRUE;

-- 3. Insert new per-vehicle plans
INSERT INTO subscription_plans
    (name, price_per_vehicle, min_vehicles, max_vehicles, max_lorries, max_users,
     price_monthly, price_yearly,
     has_fuel_logs, has_meter_readings, has_vehicle_services,
     has_attendance, has_payroll, has_inventory, has_reports, has_credit_notes,
     is_active)
VALUES
--   name           ₹/veh   min  max  maxL  maxU    pm     py    fuel   meter  svc    att    pay    inv    rep    cr
    ('Free',         0.00,   1,   2,   2,   10,    0.00,  0.00, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
    ('Starter',    499.00,   3,  19,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Growth',     449.00,  20,  49,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Business',   399.00,  50,  99,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Scale',      349.00, 100, 249,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Enterprise',         299.00, 250, 499,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('Premium Enterprise', 249.00, 500,  -1,  -1,   -1,    0.00,  0.00, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  TRUE);
