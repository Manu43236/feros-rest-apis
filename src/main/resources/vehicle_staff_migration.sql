-- Vehicle staff assignment migration
-- Adds current_driver_id and current_cleaner_id to vehicles table

ALTER TABLE vehicles
    ADD COLUMN current_driver_id  BIGINT NULL,
    ADD COLUMN current_cleaner_id BIGINT NULL,
    ADD CONSTRAINT fk_vehicle_current_driver  FOREIGN KEY (current_driver_id)  REFERENCES users(id),
    ADD CONSTRAINT fk_vehicle_current_cleaner FOREIGN KEY (current_cleaner_id) REFERENCES users(id);
