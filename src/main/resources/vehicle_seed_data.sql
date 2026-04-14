-- ============================================================
--  FEROS — Vehicle Brands & Models Seed Data
--  Indian road-freight fleet (run once after migrations)
-- ============================================================

-- ── Brands ───────────────────────────────────────────────────
INSERT INTO vehicle_brands (name, is_active) VALUES
  ('Tata Motors',    1),
  ('Ashok Leyland',  1),
  ('Mahindra',       1),
  ('Eicher',         1),
  ('BharatBenz',     1),
  ('Force Motors',   1),
  ('AMW',            1),
  ('Volvo',          1),
  ('Scania',         1),
  ('MAN',            1)
ON DUPLICATE KEY UPDATE name = name;

-- ── Helper: resolve brand_id by name ─────────────────────────
-- All model inserts use subquery (SELECT id FROM vehicle_brands WHERE name = '...' LIMIT 1)
-- Safe to run multiple times — no unique key on (brand_id, name) so wrap in NOT EXISTS check

-- ============================================================
--  TATA MOTORS
-- ============================================================

-- Light Commercial Vehicles (4 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ace HT', 4, 0.75, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ace HT');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ace Gold', 4, 1.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ace Gold');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Intra V10', 4, 1.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Intra V10');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Intra V20', 4, 1.50, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Intra V20');

-- Medium Trucks — 6 tyres
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '407', 6, 4.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '407');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ultra 812', 6, 8.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ultra 812');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ultra 1012', 6, 10.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ultra 1012');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 709', 6, 7.50, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 709');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 909', 6, 9.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 909');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 1109', 6, 11.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 1109');

-- Heavy Trucks — 10 tyres
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 1613', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 1613');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 1615', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 1615');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 2515', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 2515');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 2516', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 2516');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 3116', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 3116');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 3118', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 3118');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Signa 1923', 10, 19.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Signa 1923');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Signa 2823', 10, 28.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Signa 2823');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ultra 1518', 10, 15.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ultra 1518');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ultra 1914', 10, 19.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ultra 1914');

-- Multi-axle — 14 tyres
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Signa 3523', 14, 35.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Signa 3523');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 3723', 14, 37.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 3723');

-- Heavy Multi-axle / Tractor-Trailer — 18 tyres
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'LPT 4923', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'LPT 4923');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Prima 4028.S', 18, 40.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Prima 4028.S');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Prima 5530.S', 18, 55.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Prima 5530.S');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Signa 4923.S', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Tata Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Signa 4923.S');

-- ============================================================
--  ASHOK LEYLAND
-- ============================================================

-- Light (4 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Dost', 4, 1.25, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Dost');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Dost Strong', 4, 1.50, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Dost Strong');

-- Medium (6 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Partner', 6, 5.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Partner');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ecomet 1015', 6, 10.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ecomet 1015');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Ecomet 1215', 6, 12.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Ecomet 1215');

-- Heavy (10 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '1616', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '1616');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '1916', 10, 19.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '1916');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '2516', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '2516');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '2518', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '2518');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '3116', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '3116');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '3118', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '3118');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'U-Truck 1616', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'U-Truck 1616');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'U-Truck 2616', 10, 26.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'U-Truck 2616');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'U-Truck 3116', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'U-Truck 3116');

-- Multi-axle (14 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Captain 3718', 14, 37.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Captain 3718');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Captain 4018', 14, 40.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Captain 4018');

-- Tractor-Trailer (18 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Captain 4923', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Captain 4923');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Captain 5523', 18, 55.00, 1 FROM vehicle_brands b WHERE b.name = 'Ashok Leyland'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Captain 5523');

-- ============================================================
--  MAHINDRA
-- ============================================================

-- Light (4 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Bolero Pickup', 4, 1.50, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Bolero Pickup');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Jeeto Plus', 4, 0.75, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Jeeto Plus');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Supro Profit Truck', 4, 1.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Supro Profit Truck');

-- Medium (6 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Furio 7', 6, 7.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Furio 7');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Furio 10', 6, 10.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Furio 10');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Furio 11', 6, 11.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Furio 11');

-- Heavy (10 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Furio 14', 10, 14.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Furio 14');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Blazo 25', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Blazo 25');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Blazo 31', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Blazo 31');

-- Multi-axle (14 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Blazo 37', 14, 37.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Blazo 37');

-- Tractor-Trailer (18 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Blazo X 49', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Mahindra'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Blazo X 49');

-- ============================================================
--  EICHER
-- ============================================================

-- Medium (6 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 1059', 6, 6.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 1059');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 1075', 6, 7.50, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 1075');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 1095', 6, 9.50, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 1095');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 1110', 6, 11.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 1110');

-- Heavy (10 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 6016', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 6016');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 6025', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 6025');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 6031', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 6031');

-- Multi-axle (14 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 8035', 14, 35.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 8035');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 8049', 14, 40.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 8049');

-- Tractor-Trailer (18 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Pro 6049', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Eicher'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Pro 6049');

-- ============================================================
--  BHARATBENZ
-- ============================================================

-- Medium (6 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '914R', 6, 9.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '914R');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '1214R', 6, 12.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '1214R');

-- Heavy (10 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '1617R', 10, 16.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '1617R');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '2523R', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '2523R');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '3128R', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '3128R');

-- Multi-axle (14 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '3728R', 14, 37.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '3728R');

-- Tractor-Trailer (18 tyres)
INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '4928R', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '4928R');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '5528S', 18, 55.00, 1 FROM vehicle_brands b WHERE b.name = 'BharatBenz'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '5528S');

-- ============================================================
--  FORCE MOTORS
-- ============================================================

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Kargo King 750', 4, 0.75, 1 FROM vehicle_brands b WHERE b.name = 'Force Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Kargo King 750');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Kargo King', 6, 5.00, 1 FROM vehicle_brands b WHERE b.name = 'Force Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Kargo King');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'Traveller Toofan', 4, 1.00, 1 FROM vehicle_brands b WHERE b.name = 'Force Motors'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'Traveller Toofan');

-- ============================================================
--  AMW (Asia Motor Works)
-- ============================================================

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '2518', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'AMW'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '2518');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '2523', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'AMW'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '2523');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '3116', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'AMW'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '3116');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, '4923', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'AMW'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = '4923');

-- ============================================================
--  VOLVO (Premium long-haul)
-- ============================================================

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'FM 400', 18, 40.00, 1 FROM vehicle_brands b WHERE b.name = 'Volvo'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'FM 400');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'FH 440', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Volvo'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'FH 440');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'FH16 600', 18, 55.00, 1 FROM vehicle_brands b WHERE b.name = 'Volvo'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'FH16 600');

-- ============================================================
--  SCANIA (Premium long-haul)
-- ============================================================

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'P410', 18, 40.00, 1 FROM vehicle_brands b WHERE b.name = 'Scania'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'P410');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'R410', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'Scania'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'R410');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'G500', 18, 55.00, 1 FROM vehicle_brands b WHERE b.name = 'Scania'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'G500');

-- ============================================================
--  MAN
-- ============================================================

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'CLA 25.280', 10, 25.00, 1 FROM vehicle_brands b WHERE b.name = 'MAN'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'CLA 25.280');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'CLA 31.280', 10, 31.00, 1 FROM vehicle_brands b WHERE b.name = 'MAN'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'CLA 31.280');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'TGS 37.480', 14, 37.00, 1 FROM vehicle_brands b WHERE b.name = 'MAN'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'TGS 37.480');

INSERT INTO vehicle_models (brand_id, name, tyre_count, capacity_in_tons, is_active)
SELECT b.id, 'TGX 49.560', 18, 49.00, 1 FROM vehicle_brands b WHERE b.name = 'MAN'
  AND NOT EXISTS (SELECT 1 FROM vehicle_models m WHERE m.brand_id = b.id AND m.name = 'TGX 49.560');
