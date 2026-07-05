-- Equipment Invoices: header + line items
-- Run on staging then prod after deploying equipment invoice backend.

CREATE TABLE IF NOT EXISTS equipment_invoices (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT          NOT NULL,
    work_order_id   BIGINT          NOT NULL,
    client_id       BIGINT          NOT NULL,
    invoice_number  VARCHAR(60)     UNIQUE,
    invoice_date    DATE            NOT NULL,
    due_date        DATE,
    billing_period_start DATE,
    billing_period_end   DATE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    subtotal        DECIMAL(14, 2),
    tax_percent     DECIMAL(5, 2)   NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(14, 2)  NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(14, 2),
    notes           TEXT,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_einv_tenant   FOREIGN KEY (tenant_id)     REFERENCES tenants(id),
    CONSTRAINT fk_einv_wo       FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    CONSTRAINT fk_einv_client   FOREIGN KEY (client_id)     REFERENCES clients(id),
    INDEX idx_einv_tenant_status (tenant_id, status),
    INDEX idx_einv_wo (work_order_id)
);

CREATE TABLE IF NOT EXISTS equipment_invoice_items (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    invoice_id            BIGINT          NOT NULL,
    item_type             VARCHAR(20)     NOT NULL,
    description           VARCHAR(500)    NOT NULL,
    machine_assignment_id BIGINT,
    serial_number         VARCHAR(100),
    equipment_type_name   VARCHAR(200),
    billing_type          VARCHAR(20),
    quantity              DECIMAL(10, 3),
    rate                  DECIMAL(12, 2),
    amount                DECIMAL(14, 2),
    sort_order            INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_einv_item_invoice FOREIGN KEY (invoice_id) REFERENCES equipment_invoices(id) ON DELETE CASCADE,
    INDEX idx_einv_item_invoice (invoice_id)
);
