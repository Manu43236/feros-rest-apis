-- Equipment Types Seed Data
-- Depends on: seed_equipment_makes.sql + seed_equipment_models.sql (both must exist first)
-- Subquery joins make + model name to get model_id safely

INSERT IGNORE INTO equipment_types (model_id, name, default_meter_type, is_active, created_at, updated_at) VALUES

-- JCB
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '3DX'),    'Backhoe Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '4DX'),    'Backhoe Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '3CX'),    'Backhoe Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '4CX'),    'Backhoe Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'JS80'),   'Mini Excavator',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'JS131'),  'Excavator',       'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'JS205'),  'Excavator',       'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'JS220'),  'Excavator',       'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'JS305'),  'Excavator',       'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '426 ZX'), 'Wheel Loader',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '456 ZX'), 'Wheel Loader',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '532-70'), 'Telehandler',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = '535-95'), 'Telehandler',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'JCB' AND em.name = 'VM117'),  'Soil Compactor',  'HMR', 1, NOW(), NOW()),

-- Caterpillar
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '320'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '323'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '330'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '336'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '349'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '390'),    'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '420'),    'Backhoe Loader',        'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '432'),    'Backhoe Loader',        'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '950 GC'), 'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '962'),    'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '972'),    'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = 'D6'),     'Bulldozer',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = 'D7'),     'Bulldozer',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = 'D8'),     'Bulldozer',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = '740'),    'Articulated Dump Truck','OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Caterpillar' AND em.name = 'CS56'),   'Soil Compactor',        'HMR', 1, NOW(), NOW()),

-- Komatsu
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC130'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC200'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC210'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC300'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC360'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'PC490'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'WA320'),  'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'WA380'),  'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'WA470'),  'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'D65'),    'Bulldozer',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'D85'),    'Bulldozer',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'GD555'),  'Motor Grader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Komatsu' AND em.name = 'HM400'),  'Articulated Dump Truck','OMR', 1, NOW(), NOW()),

-- Volvo
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC140'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC200'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC210'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC220'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC300'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC350'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'EC480'),  'Excavator',             'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'L90'),    'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'L110'),   'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'L120'),   'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'L150'),   'Wheel Loader',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'A25G'),   'Articulated Dump Truck','OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'A30G'),   'Articulated Dump Truck','OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'A40G'),   'Articulated Dump Truck','OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Volvo' AND em.name = 'SD115'),  'Soil Compactor',        'HMR', 1, NOW(), NOW()),

-- Hitachi
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX130'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX200'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX210'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX220'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX300'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX350'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX450'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX500'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hitachi' AND em.name = 'ZX870'),  'Excavator', 'HMR', 1, NOW(), NOW()),

-- Hyundai
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'R140'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'R210'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'R220'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'R300'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'R380'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'HL730'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'HL760'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Hyundai' AND em.name = 'HL780'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),

-- SANY
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY75'),    'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY135'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY200'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY215'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY305'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY365'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SY500'),   'Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SL30'),    'Wheel Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SL50'),    'Wheel Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SCC500'),  'Crawler Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'SCC800'),  'Crawler Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'STC250'),  'Truck Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SANY' AND em.name = 'STC500'),  'Truck Crane',   'HMR', 1, NOW(), NOW()),

-- XCMG
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'XE150'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'XE200'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'XE215'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'XE305'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'XE370'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'LW300'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'LW500'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'GR215'),  'Motor Grader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'QY25'),   'Truck Crane',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'XCMG' AND em.name = 'QY50'),   'Truck Crane',  'HMR', 1, NOW(), NOW()),

-- Kobelco
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK75'),   'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK140'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK200'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK210'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK260'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK350'),  'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kobelco' AND em.name = 'SK500'),  'Excavator', 'HMR', 1, NOW(), NOW()),

-- Doosan
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX140'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX180'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX210'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX300'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX380'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DX500'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DL200'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Doosan' AND em.name = 'DL300'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),

-- Liebherr
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'R914'),     'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'R920'),     'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'R926'),     'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'R945'),     'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'R960'),     'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'L506'),     'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'L518'),     'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1030'), 'Mobile Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1050'), 'Mobile Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1100'), 'Mobile Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'LR 1160'),  'Crawler Crane','HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liebherr' AND em.name = 'LR 1250'),  'Crawler Crane','HMR', 1, NOW(), NOW()),

-- Case
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = 'CX130'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = 'CX210'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = 'CX220'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = 'CX300'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = '570T'),   'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = '580N'),   'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = '590SN'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = '721G'),   'Wheel Loader',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Case' AND em.name = '821G'),   'Wheel Loader',   'HMR', 1, NOW(), NOW()),

-- Manitou
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MT625'),   'Telehandler',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MT732'),   'Telehandler',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MT932'),   'Telehandler',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MT1840'),  'Telehandler',          'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MRT2150'), 'Rotating Telehandler', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Manitou' AND em.name = 'MRT2660'), 'Rotating Telehandler', 'HMR', 1, NOW(), NOW()),

-- Terex
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TC35'),  'Mini Excavator',        'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TC50'),  'Mini Excavator',        'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TL80'),  'Telehandler',           'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TL100'), 'Telehandler',           'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TA300'), 'Articulated Dump Truck','OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Terex' AND em.name = 'TA400'), 'Articulated Dump Truck','OMR', 1, NOW(), NOW()),

-- Bobcat
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E10'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E17'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E20'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E32'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E35'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E50'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'E85'),  'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'S570'), 'Skid Steer Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'S650'), 'Skid Steer Loader',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'T590'), 'Compact Track Loader','HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bobcat' AND em.name = 'T650'), 'Compact Track Loader','HMR', 1, NOW(), NOW()),

-- Zoomlion
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZE150'),  'Excavator',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZE205'),  'Excavator',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZE260'),  'Excavator',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZE360'),  'Excavator',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZTC250'), 'Truck Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Zoomlion' AND em.name = 'ZTC500'), 'Truck Crane', 'HMR', 1, NOW(), NOW()),

-- Schwing Stetter
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 28 X'),  'Concrete Boom Pump', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 36 X'),  'Concrete Boom Pump', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 42 X'),  'Concrete Boom Pump', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 52 SX'), 'Concrete Boom Pump', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 6'),    'Transit Mixer',      'OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 8'),    'Transit Mixer',      'OMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 9'),    'Transit Mixer',      'OMR', 1, NOW(), NOW()),

-- Putzmeister
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'M 28-4'),    'Concrete Boom Pump',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'M 36'),      'Concrete Boom Pump',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'M 42'),      'Concrete Boom Pump',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'M 52'),      'Concrete Boom Pump',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'M 58'),      'Concrete Boom Pump',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'BSA 14000'), 'Stationary Concrete Pump','HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Putzmeister' AND em.name = 'BSA 2109'),  'Stationary Concrete Pump','HMR', 1, NOW(), NOW()),

-- Atlas Copco
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'ROC D7'),       'Drill Rig',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'FlexiROC D50'), 'Drill Rig',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'FlexiROC D60'), 'Drill Rig',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'SmartROC D65'), 'Drill Rig',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'XAS 375'),      'Air Compressor','HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Atlas Copco' AND em.name = 'XAVS 550'),     'Air Compressor','HMR', 1, NOW(), NOW()),

-- Wirtgen
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wirtgen' AND em.name = 'W 100i'),   'Cold Planer',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wirtgen' AND em.name = 'W 130i'),   'Cold Planer',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wirtgen' AND em.name = 'W 200i'),   'Cold Planer',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wirtgen' AND em.name = 'SP 1500'),  'Slipform Paver', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wirtgen' AND em.name = 'SP 1600'),  'Slipform Paver', 'HMR', 1, NOW(), NOW()),

-- Yanmar
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Yanmar' AND em.name = 'ViO17'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Yanmar' AND em.name = 'ViO25'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Yanmar' AND em.name = 'ViO35'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Yanmar' AND em.name = 'ViO55'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Yanmar' AND em.name = 'ViO80'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),

-- Kubota
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'KX016'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'KX040'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'KX057'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'KX080'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'U17'),   'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kubota' AND em.name = 'U48'),   'Mini Excavator', 'HMR', 1, NOW(), NOW()),

-- Takeuchi
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TB216'), 'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TB235'), 'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TB260'), 'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TB280'), 'Mini Excavator',     'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TL8'),   'Compact Track Loader','HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Takeuchi' AND em.name = 'TL12'),  'Compact Track Loader','HMR', 1, NOW(), NOW()),

-- Wacker Neuson
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ17'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ26'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ36'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ50'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Wacker Neuson' AND em.name = '50Z3'), 'Mini Excavator', 'HMR', 1, NOW(), NOW()),

-- John Deere
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '75G'),   'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '85G'),   'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '130G'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '210G'),  'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '310SK'), 'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '410K'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '524K'),  'Wheel Loader',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '624K'),  'Wheel Loader',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'John Deere' AND em.name = '744K'),  'Wheel Loader',   'HMR', 1, NOW(), NOW()),

-- Sumitomo
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sumitomo' AND em.name = 'SH130'), 'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sumitomo' AND em.name = 'SH200'), 'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sumitomo' AND em.name = 'SH210'), 'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sumitomo' AND em.name = 'SH300'), 'Excavator', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sumitomo' AND em.name = 'SH350'), 'Excavator', 'HMR', 1, NOW(), NOW()),

-- Tadano
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'GR-120N'),  'Rough Terrain Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'GR-250N'),  'Rough Terrain Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'GR-300EX'), 'Rough Terrain Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'GR-500EX'), 'Rough Terrain Crane', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_makes emk JOIN equipment_models em ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'ATF 80G'),  'All Terrain Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_makes emk JOIN equipment_models em ON em.make_id = emk.id WHERE emk.name = 'Tadano' AND em.name = 'ATF 130G'), 'All Terrain Crane',   'HMR', 1, NOW(), NOW()),

-- Tata Hitachi
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 140'),     'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 200'),     'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 210'),     'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 220'),     'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 300'),     'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'SHINRAI Super'), 'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Tata Hitachi' AND em.name = 'SHINRAI Plus'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),

-- BEML
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BD285'),  'Bulldozer',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BD355'),  'Bulldozer',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BH50'),   'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BH100'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BH200'),  'Excavator',    'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BL110'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'BEML' AND em.name = 'BL115'),  'Wheel Loader', 'HMR', 1, NOW(), NOW()),

-- ACE
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 14XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 20XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 25XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 40XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 55XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'ACE 75XW'), 'Mobile Crane',   'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'NX-25'),    'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'ACE' AND em.name = 'DI-348'),   'Backhoe Loader', 'HMR', 1, NOW(), NOW()),

-- Escorts
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Escorts' AND em.name = 'Digmax II'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Escorts' AND em.name = 'FX 55'),      'Excavator',      'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Escorts' AND em.name = 'Escort 210'), 'Excavator',      'HMR', 1, NOW(), NOW()),

-- Ajax Fiori
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Ajax Fiori' AND em.name = 'ARGO 3000'), 'Self Loading Mixer', 'BOTH', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Ajax Fiori' AND em.name = 'ARGO 4000'), 'Self Loading Mixer', 'BOTH', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Ajax Fiori' AND em.name = 'ARGO 6000'), 'Self Loading Mixer', 'BOTH', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Ajax Fiori' AND em.name = 'AT 4000'),   'Self Loading Mixer', 'BOTH', 1, NOW(), NOW()),

-- TIL
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'TIL' AND em.name = 'GMK 3060'),       'Mobile Crane',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'TIL' AND em.name = 'GMK 5130'),       'Mobile Crane',  'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'TIL' AND em.name = 'Manitowoc 2250'), 'Crawler Crane', 'HMR', 1, NOW(), NOW()),

-- Mahindra
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Mahindra' AND em.name = 'EarthMaster SX'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Mahindra' AND em.name = 'EarthMaster VX'),  'Backhoe Loader', 'HMR', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Mahindra' AND em.name = 'EarthMaster SFX'), 'Backhoe Loader', 'HMR', 1, NOW(), NOW());
