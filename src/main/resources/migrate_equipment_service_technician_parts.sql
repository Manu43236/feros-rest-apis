-- Equipment service parity with vehicles: technician assignment on tasks + parts.
-- Hibernate (ddl-auto=update) creates these on deploy; this is the explicit prod path.

-- 1. Technician assignment on equipment service tasks (mirror vehicle_service_tasks)
ALTER TABLE equipment_service_tasks
    ADD COLUMN assigned_mechanic_id BIGINT      NULL,
    ADD COLUMN mechanic_started_at  DATETIME     NULL,
    ADD COLUMN mechanic_closed_at   DATETIME     NULL;

-- 2. Parts requested against equipment service tasks (mirror service_parts; shares spare_parts)
CREATE TABLE IF NOT EXISTS equipment_service_parts (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id         BIGINT       NOT NULL,
    task_id            BIGINT       NULL,
    spare_part_id      BIGINT       NOT NULL,
    quantity_requested INT          NOT NULL,
    quantity_approved  INT          NULL,
    status             VARCHAR(30)  NOT NULL DEFAULT 'REQUESTED',
    rejection_reason   TEXT         NULL,
    requested_by       BIGINT       NOT NULL,
    approved_by        BIGINT       NULL,
    approved_at        DATETIME     NULL,
    is_active          TINYINT(1)   DEFAULT 1,
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
