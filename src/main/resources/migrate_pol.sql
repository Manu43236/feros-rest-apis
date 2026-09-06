ALTER TABLE `lrs`
    ADD COLUMN `paper_lr_number` VARCHAR(100) NULL AFTER `lr_number`;

ALTER TABLE `orders`
    ADD COLUMN `is_pol` TINYINT(1) NOT NULL DEFAULT 0;
    