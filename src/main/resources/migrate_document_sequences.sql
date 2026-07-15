CREATE TABLE IF NOT EXISTS document_sequences (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT      NOT NULL,
    doc_type  VARCHAR(20) NOT NULL,
    period    VARCHAR(10) NOT NULL,  -- '2627' for FY | '0126' for monthly | 'MASTER' for master data
    last_seq  BIGINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_doc_seq (tenant_id, doc_type, period)
);
