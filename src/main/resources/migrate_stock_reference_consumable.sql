-- DEF/AdBlue consumable issue — ADDITIVE ONLY.
-- spare_parts_transactions.reference_type is a MySQL ENUM (Hibernate 6.6 maps
-- @Enumerated(STRING) to a native ENUM). ddl-auto=update does NOT add new values
-- to an existing enum column, so issuing a CONSUMABLE transaction fails with
-- error 1265 (data truncated to '') until this ALTER runs.
--
-- The ALTER only ADDS 'CONSUMABLE'; PURCHASE/SERVICE/DAMAGE/ADJUSTMENT are preserved
-- (all existing inventory transactions unaffected). Run on staging then prod.

ALTER TABLE spare_parts_transactions
  MODIFY COLUMN reference_type ENUM('PURCHASE','SERVICE','DAMAGE','ADJUSTMENT','CONSUMABLE') NOT NULL;
