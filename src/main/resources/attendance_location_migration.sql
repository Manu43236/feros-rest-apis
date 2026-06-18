-- Attendance location migration
-- 1. Creates global attendance_locations table for predefined named locations
-- 2. Adds location_name column to attendance table

CREATE TABLE IF NOT EXISTS attendance_locations (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    latitude      DOUBLE PRECISION NOT NULL,
    longitude     DOUBLE PRECISION NOT NULL,
    radius_meters INT NOT NULL DEFAULT 200,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE attendance ADD COLUMN IF NOT EXISTS location_name VARCHAR(500) NULL;
