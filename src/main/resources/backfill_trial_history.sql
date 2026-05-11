-- ─── Backfill trial history for existing tenants ────────────────────────────
-- Run once. Inserts a TRIAL SubscriptionHistory for every active tenant that
-- has no history record yet. Uses the tenant's existing trial_start_date /
-- trial_end_date if set, otherwise defaults to today / today+30.

INSERT INTO subscription_history
    (tenant_id, plan_id, status, start_date, end_date,
     amount, gst_amount, total_amount, notes, created_at)
SELECT
    t.id,
    NULL,
    'TRIAL',
    COALESCE(t.trial_start_date, CURDATE()),
    COALESCE(t.trial_end_date,   DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
    0.00,
    0.00,
    0.00,
    '30-day free trial — backfilled for existing tenant',
    NOW()
FROM tenants t
WHERE t.is_active = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM subscription_history sh WHERE sh.tenant_id = t.id
  );
