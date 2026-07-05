-- Equipment Models Seed Data
-- Depends on: seed_equipment_makes.sql (makes must exist first)
-- Uses INSERT IGNORE + subquery on name so IDs don't need to be known

INSERT IGNORE INTO equipment_models (make_id, name, is_active, created_at, updated_at) VALUES

-- JCB
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '3DX',        1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '4DX',        1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '3CX',        1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '4CX',        1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'JS80',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'JS131',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'JS205',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'JS220',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'JS305',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '426 ZX',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '456 ZX',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '532-70',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), '535-95',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'JCB'), 'VM117',      1, NOW(), NOW()),

-- Caterpillar
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '320',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '323',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '330',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '336',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '349',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '390',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '420',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '432',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '950 GC', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '962',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '972',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), 'D6',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), 'D7',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), 'D8',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), '740',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Caterpillar'), 'CS56',   1, NOW(), NOW()),

-- Komatsu
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC130',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC200',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC210',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC300',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC360',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'PC490',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'WA320',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'WA380',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'WA470',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'D65',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'D85',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'GD555',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Komatsu'), 'HM400',   1, NOW(), NOW()),

-- Volvo
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC140',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC200',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC210',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC220',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC300',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC350',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'EC480',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'L90',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'L110',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'L120',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'L150',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'A25G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'A30G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'A40G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Volvo'), 'SD115',  1, NOW(), NOW()),

-- Hitachi
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX130',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX200',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX210',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX220',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX300',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX350',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX450',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX500',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hitachi'), 'ZX870',  1, NOW(), NOW()),

-- Hyundai
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'R140',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'R210',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'R220',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'R300',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'R380',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'HL730',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'HL760',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Hyundai'), 'HL780',  1, NOW(), NOW()),

-- SANY
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY75',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY135',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY200',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY215',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY305',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY365',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SY500',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SL30',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SL50',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SCC500',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'SCC800',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'STC250',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'SANY'), 'STC500',  1, NOW(), NOW()),

-- XCMG
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'XE150',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'XE200',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'XE215',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'XE305',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'XE370',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'LW300',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'LW500',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'GR215',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'QY25',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'XCMG'), 'QY50',    1, NOW(), NOW()),

-- Kobelco
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK75',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK140',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK200',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK210',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK260',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK350',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kobelco'), 'SK500',  1, NOW(), NOW()),

-- Doosan
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX140',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX180',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX210',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX300',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX380',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DX500',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DL200',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Doosan'), 'DL300',  1, NOW(), NOW()),

-- Liebherr
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'R914',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'R920',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'R926',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'R945',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'R960',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'L506',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'L518',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'LTM 1030', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'LTM 1050', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'LTM 1100', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'LR 1160',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Liebherr'), 'LR 1250',  1, NOW(), NOW()),

-- Case
((SELECT id FROM equipment_makes WHERE name = 'Case'), 'CX130',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), 'CX210',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), 'CX220',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), 'CX300',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), '570T',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), '580N',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), '590SN',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), '721G',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Case'), '821G',    1, NOW(), NOW()),

-- Manitou
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MT625',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MT732',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MT932',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MT1840',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MRT2150', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Manitou'), 'MRT2660', 1, NOW(), NOW()),

-- Terex
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TC35',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TC50',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TL80',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TL100',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TA300',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Terex'), 'TA400',  1, NOW(), NOW()),

-- Bobcat
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E10',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E17',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E20',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E32',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E35',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E50',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'E85',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'S570',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'S650',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'T590',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Bobcat'), 'T650',  1, NOW(), NOW()),

-- Zoomlion
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZE150',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZE205',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZE260',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZE360',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZTC250',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Zoomlion'), 'ZTC500',  1, NOW(), NOW()),

-- Schwing Stetter
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'S 28 X', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'S 36 X', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'S 42 X', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'S 52 SX',1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'AM 6',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'AM 8',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Schwing Stetter'), 'AM 9',   1, NOW(), NOW()),

-- Putzmeister
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'M 28-4',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'M 36',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'M 42',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'M 52',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'M 58',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'BSA 14000',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Putzmeister'), 'BSA 2109',   1, NOW(), NOW()),

-- Atlas Copco
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'ROC D7',       1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'FlexiROC D50', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'FlexiROC D60', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'SmartROC D65', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'XAS 375',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Atlas Copco'), 'XAVS 550',     1, NOW(), NOW()),

-- Wirtgen
((SELECT id FROM equipment_makes WHERE name = 'Wirtgen'), 'W 100i',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wirtgen'), 'W 130i',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wirtgen'), 'W 200i',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wirtgen'), 'SP 1500',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wirtgen'), 'SP 1600',   1, NOW(), NOW()),

-- Yanmar
((SELECT id FROM equipment_makes WHERE name = 'Yanmar'), 'ViO17',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Yanmar'), 'ViO25',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Yanmar'), 'ViO35',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Yanmar'), 'ViO55',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Yanmar'), 'ViO80',  1, NOW(), NOW()),

-- Kubota
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'KX016',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'KX040',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'KX057',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'KX080',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'U17',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Kubota'), 'U48',    1, NOW(), NOW()),

-- Takeuchi
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TB216',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TB235',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TB260',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TB280',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TL8',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Takeuchi'), 'TL12',   1, NOW(), NOW()),

-- Wacker Neuson
((SELECT id FROM equipment_makes WHERE name = 'Wacker Neuson'), 'EZ17',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wacker Neuson'), 'EZ26',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wacker Neuson'), 'EZ36',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wacker Neuson'), 'EZ50',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Wacker Neuson'), '50Z3',  1, NOW(), NOW()),

-- John Deere
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '75G',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '85G',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '130G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '210G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '310SK',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '410K',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '524K',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '624K',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'John Deere'), '744K',   1, NOW(), NOW()),

-- Sumitomo
((SELECT id FROM equipment_makes WHERE name = 'Sumitomo'), 'SH130',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sumitomo'), 'SH200',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sumitomo'), 'SH210',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sumitomo'), 'SH300',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Sumitomo'), 'SH350',  1, NOW(), NOW()),

-- Tadano
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'GR-120N',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'GR-250N',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'GR-300EX',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'GR-500EX',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'ATF 80G',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tadano'), 'ATF 130G',  1, NOW(), NOW()),

-- Tata Hitachi
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'ZAXIS 140',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'ZAXIS 200',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'ZAXIS 210',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'ZAXIS 220',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'ZAXIS 300',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'SHINRAI Super', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Tata Hitachi'), 'SHINRAI Plus',  1, NOW(), NOW()),

-- BEML
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BD285',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BD355',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BH50',    1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BH100',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BH200',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BL110',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'BEML'), 'BL115',   1, NOW(), NOW()),

-- ACE
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 14XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 20XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 25XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 40XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 55XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'ACE 75XW',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'NX-25',     1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'ACE'), 'DI-348',    1, NOW(), NOW()),

-- Escorts
((SELECT id FROM equipment_makes WHERE name = 'Escorts'), 'Digmax II',  1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Escorts'), 'FX 55',      1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Escorts'), 'Escort 210', 1, NOW(), NOW()),

-- Ajax Fiori
((SELECT id FROM equipment_makes WHERE name = 'Ajax Fiori'), 'ARGO 3000', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Ajax Fiori'), 'ARGO 4000', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Ajax Fiori'), 'ARGO 6000', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Ajax Fiori'), 'AT 4000',   1, NOW(), NOW()),

-- TIL
((SELECT id FROM equipment_makes WHERE name = 'TIL'), 'GMK 3060',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'TIL'), 'GMK 5130',   1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'TIL'), 'Manitowoc 2250', 1, NOW(), NOW()),

-- Mahindra
((SELECT id FROM equipment_makes WHERE name = 'Mahindra'), 'EarthMaster SX', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Mahindra'), 'EarthMaster VX', 1, NOW(), NOW()),
((SELECT id FROM equipment_makes WHERE name = 'Mahindra'), 'EarthMaster SFX',1, NOW(), NOW());
