-- Migration: add client_category to clients, create client_divisions table
-- Run manually on staging first, then prod
-- Date: 2026-06-26

ALTER TABLE clients
    ADD COLUMN client_category VARCHAR(20) DEFAULT 'COMPANY' NOT NULL;

CREATE TABLE client_divisions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id   BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cd_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
