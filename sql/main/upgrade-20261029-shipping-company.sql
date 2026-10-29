-- ============================================================
-- 2026-10-29 升级脚本：“货主”更名为“船运公司”（表名、字段名、索引名、注释、菜单名）
-- 仅用于已初始化过的数据库；全新安装直接执行 init.sql 即可（init.sql 已是新名称），无需执行本脚本。
-- 执行前请先执行 init.sql 中 2026-10-28、2026-10-29 追加的各段（biz_ship_load 表需已存在）。
-- ============================================================
RENAME TABLE `biz_shipper` TO `biz_shipping_company`;

ALTER TABLE `biz_shipping_company`
  CHANGE COLUMN `shipper_name` `company_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司名称',
  RENAME INDEX `idx_shipper_tenant_name` TO `idx_company_tenant_name`,
  COMMENT = '船运公司';

ALTER TABLE `biz_ship`
  CHANGE COLUMN `shipper_id` `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '所属船运公司ID',
  RENAME INDEX `idx_ship_tenant_shipper` TO `idx_ship_tenant_company`;

ALTER TABLE `biz_berth_cargo_ship`
  CHANGE COLUMN `shipper_id` `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司ID';

ALTER TABLE `biz_ship_load`
  CHANGE COLUMN `shipper_id` `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司ID',
  RENAME INDEX `idx_sl_tenant_shipper_status` TO `idx_sl_tenant_company_status`;

UPDATE `sys_menu` SET `menu_name` = '编辑船运公司/船只' WHERE `id` = 'm_shipper_edit';
