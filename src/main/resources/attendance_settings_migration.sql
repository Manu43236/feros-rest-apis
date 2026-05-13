-- Attendance gate and missed-attendance notification settings
ALTER TABLE tenant_settings
    ADD COLUMN IF NOT EXISTS attendance_enforced TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS attendance_deadline_time TIME DEFAULT '08:00:00';
