-- Fix: unique_staff_per_vehicle_allocation constraint blocks re-assignment after unassign.
-- MySQL uses the unique index as backing index for a FK on vehicle_allocation_id.
-- Step 1: create a plain index so the FK has a backing index after the unique is dropped.
-- Step 2: drop the old unique constraint.
-- Step 3: recreate it including is_active so soft-deleted rows don't block re-assignment.

CREATE INDEX idx_osa_vehicle_allocation_id
    ON order_staff_allocations (vehicle_allocation_id);

ALTER TABLE order_staff_allocations
    DROP INDEX unique_staff_per_vehicle_allocation;

ALTER TABLE order_staff_allocations
    ADD CONSTRAINT unique_staff_per_vehicle_allocation
        UNIQUE (user_id, vehicle_allocation_id, is_active);
