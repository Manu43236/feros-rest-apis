-- Add trip_scope to vehicles to classify fleet as intra-state or inter-state
ALTER TABLE vehicles
    ADD COLUMN trip_scope ENUM('INTRA_STATE', 'INTER_STATE') NULL;
