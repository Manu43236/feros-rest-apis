-- Part Categories backfill — fixes "missing categories" in the picker.
-- Root causes: (1) rows seeded with is_active = NULL are dropped by findAllByIsActiveTrue();
--              (2) free-text categories on spare_parts that were never added to the master.
-- Safe + idempotent. Run once on each environment (staging, then prod). Back up first.

-- 1. Activate any category rows left with NULL is_active (the is_active NULL seed gotcha).
UPDATE part_categories
SET is_active = 1
WHERE is_active IS NULL;

-- 2. Backfill the master with any distinct category used on parts but missing from part_categories.
--    (name is UNIQUE on part_categories, so INSERT ... WHERE NOT EXISTS keeps it idempotent.)
INSERT INTO part_categories (name, is_active)
SELECT DISTINCT TRIM(sp.category), 1
FROM spare_parts sp
WHERE sp.category IS NOT NULL
  AND TRIM(sp.category) <> ''
  AND NOT EXISTS (
    SELECT 1 FROM part_categories pc
    WHERE pc.name = TRIM(sp.category)
  );
