-- Equipment Makes Seed Data
-- Run once on feros_db (and feros_stg_db for staging)
-- Uses INSERT IGNORE to be safe on re-runs

INSERT IGNORE INTO equipment_makes (name, is_active, created_at, updated_at) VALUES

-- Global brands with strong India presence
('JCB',             1, NOW(), NOW()),
('Caterpillar',     1, NOW(), NOW()),
('Komatsu',         1, NOW(), NOW()),
('Volvo',           1, NOW(), NOW()),
('Hitachi',         1, NOW(), NOW()),
('Hyundai',         1, NOW(), NOW()),
('SANY',            1, NOW(), NOW()),
('XCMG',            1, NOW(), NOW()),
('Kobelco',         1, NOW(), NOW()),
('Doosan',          1, NOW(), NOW()),
('Liebherr',        1, NOW(), NOW()),
('Case',            1, NOW(), NOW()),
('Manitou',         1, NOW(), NOW()),
('Terex',           1, NOW(), NOW()),
('Bobcat',          1, NOW(), NOW()),
('Zoomlion',        1, NOW(), NOW()),
('Schwing Stetter', 1, NOW(), NOW()),
('Putzmeister',     1, NOW(), NOW()),
('Atlas Copco',     1, NOW(), NOW()),
('Wirtgen',         1, NOW(), NOW()),
('Yanmar',          1, NOW(), NOW()),
('Kubota',          1, NOW(), NOW()),
('Takeuchi',        1, NOW(), NOW()),
('Wacker Neuson',   1, NOW(), NOW()),
('John Deere',      1, NOW(), NOW()),
('Sumitomo',        1, NOW(), NOW()),
('Tadano',          1, NOW(), NOW()),

-- Indian manufacturers
('Tata Hitachi',    1, NOW(), NOW()),
('BEML',            1, NOW(), NOW()),
('ACE',             1, NOW(), NOW()),
('Escorts',         1, NOW(), NOW()),
('Ajax Fiori',      1, NOW(), NOW()),
('TIL',             1, NOW(), NOW()),
('Greaves',         1, NOW(), NOW()),
('Mahindra',        1, NOW(), NOW());
