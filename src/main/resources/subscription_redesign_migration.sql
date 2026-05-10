-- Subscription Module Redesign Migration
-- Adds RENEWED status to subscription_history and amount/payment_ref to extend flow

-- 1. Add RENEWED to subscription_history status enum
ALTER TABLE subscription_history
    MODIFY COLUMN status ENUM('TRIAL','ACTIVE','EXPIRED','SUSPENDED','RENEWED') NOT NULL;

-- 2. subscription_history already has amount, gst_amount, total_amount, payment_ref columns
--    (created during initial schema) — no changes needed there.

-- 3. Index for faster enforcement filter lookups (tenant subscription status)
ALTER TABLE tenants
    ADD INDEX IF NOT EXISTS idx_tenants_subscription_status (subscription_status);
