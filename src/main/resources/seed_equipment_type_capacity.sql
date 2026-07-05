-- Equipment Type Capacity Seed
-- Updates capacity and capacity_unit for all equipment types
-- Capacity meaning by type:
--   Excavators / Mini Excavators / Bulldozers → operating weight (T)
--   Cranes (all) → lifting capacity (T)
--   Dump Trucks / Wheel Loaders / Backhoe Loaders → payload / bucket capacity (T or m³)
--   Telehandlers → rated lifting capacity (T)
--   Transit Mixers → drum capacity (m³)
--   Concrete Boom Pumps → boom reach (m)
--
-- Safe to re-run (UPDATE is idempotent).

-- ── JCB ──────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'JCB' AND em.name IN ('3DX','4DX','3CX','4CX');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = 'JS80';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = 'JS131';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.5, et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = 'JS205';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = 'JS220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.5, et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = 'JS305';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.2,  et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = '532-70';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.5,  et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = '535-95';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.0,  et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = '426 ZX';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.5,  et.capacity_unit = 'T'  WHERE emk.name = 'JCB' AND em.name = '456 ZX';

-- ── Caterpillar ───────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '320';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 23.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '323';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '330';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '336';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 49.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '349';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 90.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '390';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Caterpillar' AND em.name IN ('420','432');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 40.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '740';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '950 GC';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '962';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = '972';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = 'D6';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 29.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = 'D7';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 38.0, et.capacity_unit = 'T'  WHERE emk.name = 'Caterpillar' AND em.name = 'D8';

-- ── Volvo ─────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC140';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC350';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 48.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'EC480';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'A25G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'A30G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 40.0, et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'A40G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'L90';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'L110';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'L120';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 7.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Volvo' AND em.name = 'L150';

-- ── Komatsu ───────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC130';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC360';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 49.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'PC490';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 40.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'HM400';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'WA320';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'WA380';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'WA470';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 19.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'D65';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Komatsu' AND em.name = 'D85';

-- ── Hitachi ───────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX130';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX350';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 45.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX450';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX500';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 87.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hitachi' AND em.name = 'ZX870';

-- ── Tata Hitachi ──────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Tata Hitachi' AND em.name IN ('SHINRAI Plus','SHINRAI Super');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 140';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tata Hitachi' AND em.name = 'ZAXIS 300';

-- ── Kobelco ───────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 7.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK75';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK140';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 26.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK260';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK350';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Kobelco' AND em.name = 'SK500';

-- ── Hyundai ───────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'R140';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'R210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'R220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'R300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 38.0, et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'R380';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'HL730';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'HL760';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Hyundai' AND em.name = 'HL780';

-- ── Doosan ────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX140';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 18.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX180';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 38.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX380';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DX500';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DL200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Doosan' AND em.name = 'DL300';

-- ── Case ──────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Case' AND em.name IN ('570T','580N','590SN');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = 'CX130';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = 'CX210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 22.0, et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = 'CX220';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = 'CX300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = '721G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Case' AND em.name = '821G';

-- ── SANY ──────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 7.5,  et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY75';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.5, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY135';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.5, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY215';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.5, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY305';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.5, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY365';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SY500';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SL30';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SL50';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SCC500';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 80.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'SCC800';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'STC250';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'SANY' AND em.name = 'STC500';

-- ── Liebherr ──────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'R914';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'R920';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 26.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'R926';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 45.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'R945';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 60.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'R960';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1030';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1050';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 100.0,et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'LTM 1100';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 160.0,et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'LR 1160';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 250.0,et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'LR 1250';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'L506';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Liebherr' AND em.name = 'L518';

-- ── ACE ───────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 14.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 14XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 20XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 25XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 40.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 40XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 55.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 55XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 75.0, et.capacity_unit = 'T'  WHERE emk.name = 'ACE' AND em.name = 'ACE 75XW';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'ACE' AND em.name IN ('NX-25','DI-348');

-- ── TIL (Grove cranes) ────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 60.0, et.capacity_unit = 'T'  WHERE emk.name = 'TIL' AND em.name = 'GMK 3060';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 130.0,et.capacity_unit = 'T'  WHERE emk.name = 'TIL' AND em.name = 'GMK 5130';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 250.0,et.capacity_unit = 'T'  WHERE emk.name = 'TIL' AND em.name = 'Manitowoc 2250';

-- ── Tadano ────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 12.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'GR-120N';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'GR-250N';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'GR-300EX';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'GR-500EX';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 80.0, et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'ATF 80G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 130.0,et.capacity_unit = 'T'  WHERE emk.name = 'Tadano' AND em.name = 'ATF 130G';

-- ── XCMG ──────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 15.0, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'XE150';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'XE200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.5, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'XE215';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.5, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'XE305';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 37.0, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'XE370';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'QY25';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'QY50';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'LW300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'XCMG' AND em.name = 'LW500';

-- ── Zoomlion ──────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 15.0, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZE150';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.5, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZE205';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 26.0, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZE260';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.0, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZE360';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 25.0, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZTC250';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 50.0, et.capacity_unit = 'T'  WHERE emk.name = 'Zoomlion' AND em.name = 'ZTC500';

-- ── Sumitomo ──────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'Sumitomo' AND em.name = 'SH130';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'Sumitomo' AND em.name = 'SH200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'Sumitomo' AND em.name = 'SH210';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Sumitomo' AND em.name = 'SH300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.0, et.capacity_unit = 'T'  WHERE emk.name = 'Sumitomo' AND em.name = 'SH350';

-- ── John Deere ────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 7.5,  et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '75G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.5,  et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '85G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 13.0, et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '130G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 21.0, et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '210G';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'John Deere' AND em.name IN ('310SK','410K');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '524K';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '624K';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 6.0,  et.capacity_unit = 'T'  WHERE emk.name = 'John Deere' AND em.name = '744K';

-- ── Mahindra ──────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Mahindra' AND em.name IN ('EarthMaster SFX','EarthMaster SX','EarthMaster VX');

-- ── Kubota ────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.7,  et.capacity_unit = 'T'  WHERE emk.name = 'Kubota' AND em.name IN ('KX016','U17');
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Kubota' AND em.name = 'KX040';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Kubota' AND em.name = 'KX057';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Kubota' AND em.name IN ('KX080','U48');

-- ── Bobcat ────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E10';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.7,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E17';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E20';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.2,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E32';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E35';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E50';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Bobcat' AND em.name = 'E85';

-- ── Takeuchi ──────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.7,  et.capacity_unit = 'T'  WHERE emk.name = 'Takeuchi' AND em.name = 'TB216';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Takeuchi' AND em.name = 'TB235';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 6.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Takeuchi' AND em.name = 'TB260';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Takeuchi' AND em.name = 'TB280';

-- ── Yanmar ────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.7,  et.capacity_unit = 'T'  WHERE emk.name = 'Yanmar' AND em.name = 'ViO17';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Yanmar' AND em.name = 'ViO25';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Yanmar' AND em.name = 'ViO35';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Yanmar' AND em.name = 'ViO55';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Yanmar' AND em.name = 'ViO80';

-- ── Wacker Neuson ─────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 1.7,  et.capacity_unit = 'T'  WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ17';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.6,  et.capacity_unit = 'T'  WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ26';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.6,  et.capacity_unit = 'T'  WHERE emk.name = 'Wacker Neuson' AND em.name = 'EZ36';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Wacker Neuson' AND em.name IN ('EZ50','50Z3');

-- ── Terex ─────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TC35';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TC50';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TL80';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TL100';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 30.0, et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TA300';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 40.0, et.capacity_unit = 'T'  WHERE emk.name = 'Terex' AND em.name = 'TA400';

-- ── Manitou ───────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 2.5,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MT625';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.2,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MT732';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 3.2,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MT932';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 4.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MT1840';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MRT2150';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 6.0,  et.capacity_unit = 'T'  WHERE emk.name = 'Manitou' AND em.name = 'MRT2660';

-- ── BEML ──────────────────────────────────────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 28.5, et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BD285';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 35.5, et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BD355';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BH50';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 10.0, et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BH100';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 20.0, et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BH200';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BL110';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 5.0,  et.capacity_unit = 'T'  WHERE emk.name = 'BEML' AND em.name = 'BL115';

-- ── Transit Mixers — drum capacity (m³) ──────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 6.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 6';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 8';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 9.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Schwing Stetter' AND em.name = 'AM 9';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 6.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Ajax Fiori' AND em.name = 'ARGO 3000';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 8.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Ajax Fiori' AND em.name = 'ARGO 4000';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 9.0,  et.capacity_unit = 'm³' WHERE emk.name = 'Ajax Fiori' AND em.name IN ('ARGO 6000','AT 4000');

-- ── Concrete Boom Pumps — boom reach (m) ─────────────────────────────────────
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 28.0, et.capacity_unit = 'm'  WHERE emk.name = 'Putzmeister' AND em.name = 'M 28-4';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.0, et.capacity_unit = 'm'  WHERE emk.name = 'Putzmeister' AND em.name = 'M 36';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 42.0, et.capacity_unit = 'm'  WHERE emk.name = 'Putzmeister' AND em.name = 'M 42';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 52.0, et.capacity_unit = 'm'  WHERE emk.name = 'Putzmeister' AND em.name = 'M 52';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 58.0, et.capacity_unit = 'm'  WHERE emk.name = 'Putzmeister' AND em.name = 'M 58';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 28.0, et.capacity_unit = 'm'  WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 28 X';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 36.0, et.capacity_unit = 'm'  WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 36 X';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 42.0, et.capacity_unit = 'm'  WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 42 X';
UPDATE equipment_types et JOIN equipment_models em ON et.model_id = em.id JOIN equipment_makes emk ON em.make_id = emk.id SET et.capacity = 52.0, et.capacity_unit = 'm'  WHERE emk.name = 'Schwing Stetter' AND em.name = 'S 52 SX';
