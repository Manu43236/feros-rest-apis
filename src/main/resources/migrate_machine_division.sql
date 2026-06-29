-- Phase C: Division assignment on machine assignments
ALTER TABLE machine_assignments
    ADD COLUMN division_id   BIGINT       NULL,
    ADD COLUMN division_name VARCHAR(100) NULL,
    ADD CONSTRAINT fk_ma_division FOREIGN KEY (division_id) REFERENCES client_divisions(id);
