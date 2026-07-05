-- Change reading_date from DATE to DATETIME to store time (HH:MM) of HMR reading
ALTER TABLE equipment_meter_readings
    MODIFY COLUMN reading_date DATETIME NOT NULL;
