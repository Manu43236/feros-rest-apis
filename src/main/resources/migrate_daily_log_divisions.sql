-- Migrate daily logs to support multiple division lines per log
-- Run ONCE on staging, then prod

-- 1. Create division lines child table
CREATE TABLE daily_log_divisions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_log_id     BIGINT         NOT NULL,
    division_name    VARCHAR(100)   NULL,
    start_hour_meter DECIMAL(10, 2) NULL,
    end_hour_meter   DECIMAL(10, 2) NULL,
    hours_worked     DECIMAL(10, 2) NULL,
    notes            TEXT           NULL,
    CONSTRAINT fk_dld_log FOREIGN KEY (daily_log_id) REFERENCES equipment_daily_logs (id) ON DELETE CASCADE
);

-- 2. Migrate existing flat log data into division rows (one row per existing log)
INSERT INTO daily_log_divisions (daily_log_id, division_name, start_hour_meter, end_hour_meter, hours_worked)
SELECT id,
       CASE WHEN EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = DATABASE()
                           AND table_name   = 'equipment_daily_logs'
                           AND column_name  = 'division_name')
            THEN division_name ELSE NULL END,
       start_hour_meter,
       end_hour_meter,
       hours_worked
FROM equipment_daily_logs;

-- 3. Drop division_name from logs header (now lives in child table)
ALTER TABLE equipment_daily_logs DROP COLUMN IF EXISTS division_name;

-- 4. Ensure source column exists (in case migrate_daily_log_division_source.sql was not run)
ALTER TABLE equipment_daily_logs
    ADD COLUMN IF NOT EXISTS source VARCHAR(10) NOT NULL DEFAULT 'MANUAL';
