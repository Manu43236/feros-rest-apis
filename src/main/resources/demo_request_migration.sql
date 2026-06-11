-- Demo Request migration
-- Stores leads from the feros_business website contact form

CREATE TABLE IF NOT EXISTS demo_requests (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    company      VARCHAR(150) NOT NULL,
    email        VARCHAR(150),
    fleet_size   VARCHAR(50),
    city         VARCHAR(100),
    status       VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    notes        TEXT,
    created_at   DATETIME(6),
    updated_at   DATETIME(6)
);
