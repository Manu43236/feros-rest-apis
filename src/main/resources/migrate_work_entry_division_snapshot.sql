-- Snapshot division at session start time
-- Each work entry now stores which division the machine was working for at that moment
ALTER TABLE machine_work_entries
    ADD COLUMN division_name VARCHAR(100) NULL;
