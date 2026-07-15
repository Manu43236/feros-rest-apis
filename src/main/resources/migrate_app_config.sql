CREATE TABLE IF NOT EXISTS app_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    min_version  INT     NOT NULL DEFAULT 1,
    latest_version INT   NOT NULL DEFAULT 1,
    force_update TINYINT(1) NOT NULL DEFAULT 0,
    created_at   DATETIME,
    updated_at   DATETIME
);

-- seed initial row (version code 1, no force update)
INSERT INTO app_config (min_version, latest_version, force_update, created_at, updated_at)
SELECT 1, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_config);
