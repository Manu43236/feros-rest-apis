-- ============================================================
-- FEROS User Sessions Migration
-- Single active session per user per device type (WEB / MOBILE)
-- Supports: JWT invalidation, force logout, push notifications, IP audit
-- Run manually before starting the application
-- ============================================================

    CREATE TABLE IF NOT EXISTS user_sessions (
        id              BIGINT          NOT NULL AUTO_INCREMENT,
        user_id         BIGINT          NOT NULL,
        device_type     ENUM('WEB','MOBILE') NOT NULL,
        token           TEXT            NOT NULL,
        fcm_token       VARCHAR(512)    NULL,
        ip_address      VARCHAR(45)     NULL,        -- supports IPv4 + IPv6
        device_info     VARCHAR(255)    NULL,         -- e.g. "iPhone 14 / iOS 17"
        app_version     VARCHAR(20)     NULL,         -- e.g. "1.0.0"
        logged_in_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
        logged_out_at   DATETIME        NULL,          -- NULL = session is active
        last_active_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

        PRIMARY KEY (id),
        UNIQUE KEY uq_user_device (user_id, device_type),
        CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
