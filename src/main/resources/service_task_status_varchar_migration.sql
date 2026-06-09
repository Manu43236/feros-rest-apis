-- Convert vehicle_service_tasks.status from ENUM('PENDING','COMPLETED') to VARCHAR
-- Required because ASSIGNED, IN_PROGRESS, MECHANIC_CLOSED were added to ServiceTaskStatus
-- after the table was created, but the MySQL ENUM column was never updated.
ALTER TABLE vehicle_service_tasks
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
