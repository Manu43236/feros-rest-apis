-- ============================================================
-- FEROS User Sessions Migration
-- Multiple concurrent sessions allowed per user per device type
-- Supports: JWT invalidation, logout, push notifications, IP audit
-- Run manually before starting the application
-- ============================================================

CREATE TABLE IF NOT EXISTS user_sessions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    device_type     ENUM('WEB','MOBILE') NOT NULL,
    token           TEXT            NOT NULL,
    fcm_token       VARCHAR(512)    NULL,
    ip_address      VARCHAR(45)     NULL,
    device_info     VARCHAR(255)    NULL,
    app_version     VARCHAR(20)     NULL,
    logged_in_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logged_out_at   DATETIME        NULL,
    last_active_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- If upgrading an existing DB, run this to drop the old unique constraint:
-- ALTER TABLE user_sessions DROP INDEX uq_user_device;
