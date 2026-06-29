-- Machine work entries — tracks start/stop sessions per machine assignment
CREATE TABLE machine_work_entries (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    machine_assignment_id BIGINT NOT NULL,
    operator_type       VARCHAR(20),
    operator_staff_id   BIGINT,
    hired_operator_name VARCHAR(100),
    start_time          DATETIME NOT NULL,
    end_time            DATETIME,
    start_meter         DECIMAL(10,2),
    end_meter           DECIMAL(10,2),
    hours_worked        DECIMAL(8,2),
    notes               TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mwe_assignment FOREIGN KEY (machine_assignment_id) REFERENCES machine_assignments(id),
    CONSTRAINT fk_mwe_operator_staff FOREIGN KEY (operator_staff_id) REFERENCES staff_profiles(id)
);
