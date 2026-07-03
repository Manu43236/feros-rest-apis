-- Additional Equipment Brands Seed
-- Adds: Liugong, SDLG, Shantui, Lonking, Sunward, Bell Equipment, Kato, Sennebogen
-- Safe to re-run (INSERT IGNORE)
-- Run AFTER: seed_equipment_makes.sql, seed_equipment_models.sql, seed_equipment_types.sql, migrate_equipment_type_capacity.sql

-- ── Makes ─────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO equipment_makes (name, is_active, created_at, updated_at) VALUES
('Liugong',        1, NOW(), NOW()),
('SDLG',           1, NOW(), NOW()),
('Shantui',        1, NOW(), NOW()),
('Lonking',        1, NOW(), NOW()),
('Sunward',        1, NOW(), NOW()),
('Bell Equipment', 1, NOW(), NOW()),
('Kato',           1, NOW(), NOW()),
('Sennebogen',     1, NOW(), NOW());

-- ── Models ────────────────────────────────────────────────────────────────────

-- Liugong
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG915E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG922E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG936E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG950E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG835H',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG856H',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG862H',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'B160C',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'B230C',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liugong'), 'CLG4165',  1, NOW(), NOW());

-- SDLG
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG6150E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG6210E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG6300E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG938L',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG946L',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG956L',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'LG968L',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'G9165',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SDLG'), 'G9190',   1, NOW(), NOW());

-- Shantui
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SD13',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SD16',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SD22',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SD32',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'GR165',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'GR180',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'GR215',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SL30W',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Shantui'), 'SL50W',  1, NOW(), NOW());

-- Lonking
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM833',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM835',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM856',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM860',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM6150', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM6215', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Lonking'), 'CDM6235', 1, NOW(), NOW());

-- Sunward
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Sunward'), 'SWE150E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sunward'), 'SWE215E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sunward'), 'SWE285E',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sunward'), 'SWDM150H', 1, NOW(), NOW());

-- Bell Equipment
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B20E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B25E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B30E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B35E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B40E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bell Equipment'), 'B45E', 1, NOW(), NOW());

-- Kato
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-160', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-200', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-250', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-300', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-400', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kato'), 'NK-500', 1, NOW(), NOW());

-- Sennebogen
INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '817E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '820E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '825E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '830E', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '655',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sennebogen'), '683',  1, NOW(), NOW());

-- ── Types (with capacity) ─────────────────────────────────────────────────────
-- Uses capacity column added by migrate_equipment_type_capacity.sql

-- Liugong — Excavators
INSERT IGNORE INTO equipment_types (model_id, name, default_meter_type, capacity, capacity_unit, is_active, created_at, updated_at) VALUES
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG915E'), 'Excavator', 'HMR', 15.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG922E'), 'Excavator', 'HMR', 22.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG936E'), 'Excavator', 'HMR', 36.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG950E'), 'Excavator', 'HMR', 50.0, 'T', 1, NOW(), NOW()),
-- Liugong — Wheel Loaders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG835H'), 'Wheel Loader', 'HMR', 3.5, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG856H'), 'Wheel Loader', 'HMR', 5.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG862H'), 'Wheel Loader', 'HMR', 6.0, 'T', 1, NOW(), NOW()),
-- Liugong — Bulldozers
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'B160C'),   'Bulldozer',    'HMR', 16.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'B230C'),   'Bulldozer',    'HMR', 23.0, 'T', 1, NOW(), NOW()),
-- Liugong — Motor Grader
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Liugong' AND em.name = 'CLG4165'), 'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),

-- SDLG — Excavators
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG6150E'), 'Excavator', 'HMR', 15.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG6210E'), 'Excavator', 'HMR', 21.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG6300E'), 'Excavator', 'HMR', 30.0, 'T', 1, NOW(), NOW()),
-- SDLG — Wheel Loaders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG938L'),  'Wheel Loader', 'HMR', 3.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG946L'),  'Wheel Loader', 'HMR', 4.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG956L'),  'Wheel Loader', 'HMR', 5.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'LG968L'),  'Wheel Loader', 'HMR', 6.0, 'T', 1, NOW(), NOW()),
-- SDLG — Motor Graders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'G9165'),   'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'SDLG' AND em.name = 'G9190'),   'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),

-- Shantui — Bulldozers
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SD13'),  'Bulldozer',    'HMR', 13.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SD16'),  'Bulldozer',    'HMR', 16.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SD22'),  'Bulldozer',    'HMR', 22.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SD32'),  'Bulldozer',    'HMR', 32.0, 'T', 1, NOW(), NOW()),
-- Shantui — Motor Graders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'GR165'), 'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'GR180'), 'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'GR215'), 'Motor Grader', 'HMR', NULL, NULL, 1, NOW(), NOW()),
-- Shantui — Wheel Loaders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SL30W'), 'Wheel Loader', 'HMR', 3.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Shantui' AND em.name = 'SL50W'), 'Wheel Loader', 'HMR', 5.0, 'T', 1, NOW(), NOW()),

-- Lonking — Wheel Loaders
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM833'),  'Wheel Loader', 'HMR', 3.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM835'),  'Wheel Loader', 'HMR', 3.5,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM856'),  'Wheel Loader', 'HMR', 5.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM860'),  'Wheel Loader', 'HMR', 6.0,  'T', 1, NOW(), NOW()),
-- Lonking — Excavators
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM6150'), 'Excavator',    'HMR', 15.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM6215'), 'Excavator',    'HMR', 21.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Lonking' AND em.name = 'CDM6235'), 'Excavator',    'HMR', 23.5, 'T', 1, NOW(), NOW()),

-- Sunward — Excavators
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sunward' AND em.name = 'SWE150E'),  'Excavator', 'HMR', 15.0, 'T',   1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sunward' AND em.name = 'SWE215E'),  'Excavator', 'HMR', 21.0, 'T',   1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sunward' AND em.name = 'SWE285E'),  'Excavator', 'HMR', 28.5, 'T',   1, NOW(), NOW()),
-- Sunward — Drill Rig
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sunward' AND em.name = 'SWDM150H'), 'Drill Rig', 'HMR', NULL, NULL, 1, NOW(), NOW()),

-- Bell Equipment — Articulated Dump Trucks
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B20E'), 'Articulated Dump Truck', 'OMR', 20.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B25E'), 'Articulated Dump Truck', 'OMR', 25.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B30E'), 'Articulated Dump Truck', 'OMR', 30.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B35E'), 'Articulated Dump Truck', 'OMR', 35.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B40E'), 'Articulated Dump Truck', 'OMR', 40.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Bell Equipment' AND em.name = 'B45E'), 'Articulated Dump Truck', 'OMR', 45.0, 'T', 1, NOW(), NOW()),

-- Kato — Mobile Cranes (lifting capacity)
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-160'), 'Mobile Crane', 'HMR', 16.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-200'), 'Mobile Crane', 'HMR', 20.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-250'), 'Mobile Crane', 'HMR', 25.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-300'), 'Mobile Crane', 'HMR', 30.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-400'), 'Mobile Crane', 'HMR', 40.0,  'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Kato' AND em.name = 'NK-500'), 'Mobile Crane', 'HMR', 50.0,  'T', 1, NOW(), NOW()),

-- Sennebogen — Material Handlers
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '817E'), 'Material Handler', 'HMR', 17.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '820E'), 'Material Handler', 'HMR', 20.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '825E'), 'Material Handler', 'HMR', 25.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '830E'), 'Material Handler', 'HMR', 30.0, 'T', 1, NOW(), NOW()),
-- Sennebogen — Crawler Cranes (lifting capacity)
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '655'),  'Crawler Crane',   'HMR', 55.0, 'T', 1, NOW(), NOW()),
((SELECT em.id FROM equipment_models em JOIN equipment_makes emk ON em.make_id = emk.id WHERE emk.name = 'Sennebogen' AND em.name = '683'),  'Crawler Crane',   'HMR', 83.0, 'T', 1, NOW(), NOW());
