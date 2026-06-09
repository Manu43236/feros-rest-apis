-- Add mechanic_started_at to vehicle_service_tasks
-- Captures when a mechanic starts working on a task (for duration tracking)
ALTER TABLE vehicle_service_tasks
    ADD COLUMN IF NOT EXISTS mechanic_started_at TIMESTAMP NULL;
