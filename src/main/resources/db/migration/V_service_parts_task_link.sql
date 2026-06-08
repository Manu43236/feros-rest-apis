-- ============================================================
-- Phase 4: Link spare part requests to a specific service task
-- Adds optional task_id FK to service_parts table.
-- Existing rows remain valid (NULL task_id = service-level request).
-- ============================================================

ALTER TABLE service_parts
    ADD COLUMN IF NOT EXISTS task_id BIGINT NULL,
    ADD CONSTRAINT fk_service_parts_task
        FOREIGN KEY (task_id) REFERENCES vehicle_service_tasks(id);
