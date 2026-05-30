-- ============================================================
-- FEROS Trip Expense Migration
-- Run manually on feros_db
-- ============================================================

-- 1. Add trip batta settings to tenant_settings
ALTER TABLE tenant_settings
    ADD COLUMN  driver_batta_rate   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN  cleaner_batta_rate  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN  trip_mamulu_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- 2. Create lr_trip_expenses table (one record per LR)
CREATE TABLE  lr_trip_expenses (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL REFERENCES tenants(id),
    lr_id               BIGINT        NOT NULL UNIQUE REFERENCES lrs(id),
    advance_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    trip_days           INTEGER       NOT NULL DEFAULT 1,
    driver_batta        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cleaner_batta       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    trip_mamulu         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_by_id       BIGINT        NOT NULL REFERENCES users(id),
    submitted_by_id     BIGINT        REFERENCES users(id),
    submitted_at        TIMESTAMP,
    approved_by_id      BIGINT        REFERENCES users(id),
    approved_at         TIMESTAMP,
    settlement_amount   DECIMAL(10,2),
    settlement_note     TEXT,
    settled_by_id       BIGINT        REFERENCES users(id),
    settled_at          TIMESTAMP,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- 3. Create lr_trip_expense_items table (line items per expense sheet)
CREATE TABLE  lr_trip_expense_items (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL REFERENCES tenants(id),
    trip_expense_id     BIGINT        NOT NULL REFERENCES lr_trip_expenses(id),
    description         VARCHAR(255)  NOT NULL,
    amount              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    approved_amount     DECIMAL(10,2),
    receipt_url         VARCHAR(500),
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- 4. Indexes
CREATE INDEX  idx_lr_trip_expenses_tenant   ON lr_trip_expenses(tenant_id);
CREATE INDEX  idx_lr_trip_expenses_lr       ON lr_trip_expenses(lr_id);
CREATE INDEX  idx_lr_trip_expenses_status   ON lr_trip_expenses(status);
CREATE INDEX  idx_lr_trip_expense_items_exp ON lr_trip_expense_items(trip_expense_id);
