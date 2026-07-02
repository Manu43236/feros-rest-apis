-- Add capacity fields to equipment_types
ALTER TABLE equipment_types
    ADD COLUMN capacity DECIMAL(10,2) NULL,
    ADD COLUMN capacity_unit VARCHAR(10) NULL;
