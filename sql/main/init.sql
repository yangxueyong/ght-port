-- ============================================================
-- 港慧通 · 港口物流管理系统 数据库初始化脚本
-- 分支：main
-- 数据库：MySQL 8.x
-- 使用：CREATE DATABASE ght_port DEFAULT CHARSET utf8mb3 COLLATE utf8mb3_bin; USE ght_port; SOURCE init.sql;
-- @author yangxueyong
-- ============================================================
SET NAMES utf8mb3;
SET FOREIGN_KEY_CHECKS = 0;

-- 租户
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_code` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '租户编码（登录时使用）',
  `tenant_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '租户名称',
  `contact_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系电话',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `expire_time` datetime DEFAULT NULL COMMENT '到期时间',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  UNIQUE KEY `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='租户';

-- 系统用户
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `username` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '登录账号',
  `password` varchar(128) COLLATE utf8mb3_bin NOT NULL COMMENT '密码密文',
  `salt` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '密码盐值',
  `nickname` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `user_type` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'STAFF' COMMENT '用户类型：ADMIN-管理员，STAFF-港口员工，CARRIER-承运商，DRIVER-司机，GUARD-门卫',
  `ref_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '关联业务ID（承运商账号关联承运商ID，司机账号关联车辆ID）',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_user_tenant_username` (`tenant_id`, `username`),
  KEY `idx_user_tenant_ref` (`tenant_id`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='系统用户';

-- 角色
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `role_code` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '角色名称',
  `builtin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否内置角色：1-是，0-否（内置角色不可删除）',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_role_tenant_code` (`tenant_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='角色';

-- 菜单权限
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `parent_id` varchar(36) COLLATE utf8mb3_bin NOT NULL DEFAULT '0' COMMENT '父菜单ID，顶级为0',
  `menu_name` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '菜单名称',
  `menu_type` varchar(16) COLLATE utf8mb3_bin NOT NULL COMMENT '菜单类型：DIR-目录，MENU-菜单，BUTTON-按钮',
  `path` varchar(200) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '前端组件路径',
  `perms` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示：1-显示，0-隐藏',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_menu_parent_sort` (`parent_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='菜单权限';

-- 用户角色关联
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `user_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '用户ID',
  `role_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '角色ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_ur_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_ur_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户角色关联';

-- 角色菜单关联
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `role_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '角色ID',
  `menu_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '菜单ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_rm_tenant_role` (`tenant_id`, `role_id`),
  KEY `idx_rm_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='角色菜单关联';

-- 港口
DROP TABLE IF EXISTS `biz_port`;
CREATE TABLE `biz_port` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `port_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '港口名称',
  `port_code` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '港口编码',
  `address` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '港口地址',
  `canvas_width` int NOT NULL DEFAULT 1600 COMMENT '俯视图画布宽度（像素）',
  `canvas_height` int NOT NULL DEFAULT 900 COMMENT '俯视图画布高度（像素）',
  `theme` varchar(32) COLLATE utf8mb3_bin NOT NULL DEFAULT 'DAY' COMMENT '俯视图主题：DAY-白昼，DUSK-黄昏，NIGHT-夜间',
  `description` varchar(500) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '港口简介',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_port_tenant_name` (`tenant_id`, `port_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='港口';

-- 港口俯视图元素
DROP TABLE IF EXISTS `biz_port_element`;
CREATE TABLE `biz_port_element` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `port_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '港口ID',
  `element_type` varchar(20) COLLATE utf8mb3_bin NOT NULL COMMENT '元素类型：RIVER/LAND/BERTH/ROAD/YARD/WAREHOUSE/CRANE/GATE/BUILDING/TANK/TREE/SHIP/LABEL',
  `pos_x` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '左上角X坐标',
  `pos_y` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '左上角Y坐标',
  `width` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '宽度',
  `height` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '高度',
  `rotation` decimal(6,2) NOT NULL DEFAULT 0 COMMENT '旋转角度（度）',
  `layer_index` int NOT NULL DEFAULT 0 COMMENT '图层顺序，越大越靠上',
  `props` text DEFAULT NULL COMMENT '外观属性（JSON文本，由前端解释，如颜色、文字、变体）',
  `berth_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '关联泊位ID（仅泊位元素有值）',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_pe_tenant_port` (`tenant_id`, `port_id`),
  KEY `idx_pe_berth` (`berth_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='港口俯视图元素';

-- 泊位
DROP TABLE IF EXISTS `biz_berth`;
CREATE TABLE `biz_berth` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `port_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '港口ID',
  `berth_code` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位编号',
  `berth_name` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位名称',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'IDLE' COMMENT '泊位状态：IDLE-未作业，WORKING-正在作业，MAINTENANCE-维修中',
  `work_start_time` time DEFAULT NULL COMMENT '每日作业开始时间',
  `work_end_time` time DEFAULT NULL COMMENT '每日作业结束时间',
  `berth_length` decimal(10,2) DEFAULT NULL COMMENT '泊位长度（米）',
  `water_depth` decimal(6,2) DEFAULT NULL COMMENT '前沿水深（米）',
  `max_tonnage` decimal(12,2) DEFAULT NULL COMMENT '最大靠泊吨级（吨）',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_berth_tenant_port_status` (`tenant_id`, `port_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='泊位';

-- 货物类型
DROP TABLE IF EXISTS `biz_cargo_type`;
CREATE TABLE `biz_cargo_type` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `type_name` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '类型名称',
  `type_code` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '类型编码',
  `color` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '展示颜色',
  `sort` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_ct_tenant_name` (`tenant_id`, `type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='货物类型';

-- 船运公司
DROP TABLE IF EXISTS `biz_shipping_company`;
CREATE TABLE `biz_shipping_company` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `company_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司名称',
  `short_name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '简称',
  `contact_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `credit_code` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '统一社会信用代码',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_company_tenant_name` (`tenant_id`, `company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='船运公司';

-- 船只
DROP TABLE IF EXISTS `biz_ship`;
CREATE TABLE `biz_ship` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '所属船运公司ID',
  `ship_name` varchar(50) COLLATE utf8mb3_bin NOT NULL COMMENT '船名',
  `ship_no` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '船舶识别号',
  `ship_type` varchar(20) COLLATE utf8mb3_bin NOT NULL COMMENT '船舶类型：BULK-散货船，CONTAINER-集装箱船，TANKER-油船，GENERAL-杂货船，RORO-滚装船，BARGE-驳船',
  `ship_length` decimal(8,2) DEFAULT NULL COMMENT '船长（米）',
  `deadweight` decimal(12,2) DEFAULT NULL COMMENT '载重吨',
  `hull_color` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '船体颜色',
  `captain_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '船长姓名',
  `captain_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '船长电话',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_ship_tenant_company` (`tenant_id`, `company_id`),
  KEY `idx_ship_tenant_name` (`tenant_id`, `ship_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='船只';

-- 泊位到货
DROP TABLE IF EXISTS `biz_berth_cargo`;
CREATE TABLE `biz_berth_cargo` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `port_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '港口ID',
  `berth_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位ID',
  `cargo_type_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '货物类型ID',
  `cargo_type_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '货物类型名称（冗余）',
  `cargo_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '货物名称',
  `tonnage` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '货物吨位',
  `volume` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '货物方数',
  `pieces` int NOT NULL DEFAULT 0 COMMENT '货物件数',
  `loaded_tonnage` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '已装车吨位',
  `loaded_volume` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '已装车方数',
  `loaded_pieces` int NOT NULL DEFAULT 0 COMMENT '已装车件数',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS-作业中，COMPLETED-已完成',
  `arrive_time` datetime DEFAULT NULL COMMENT '到货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_bc_tenant_berth_status` (`tenant_id`, `berth_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='泊位到货';

-- 到货与船只关联
DROP TABLE IF EXISTS `biz_berth_cargo_ship`;
CREATE TABLE `biz_berth_cargo_ship` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `cargo_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位到货ID',
  `ship_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船只ID',
  `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_bcs_tenant_cargo` (`tenant_id`, `cargo_id`),
  KEY `idx_bcs_ship` (`ship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='到货与船只关联';

-- 承运商
DROP TABLE IF EXISTS `biz_carrier`;
CREATE TABLE `biz_carrier` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `carrier_name` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '承运商名称',
  `short_name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '简称',
  `contact_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) COLLATE utf8mb3_bin NOT NULL COMMENT '联系电话（同时作为承运商登录账号）',
  `address` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `credit_code` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '统一社会信用代码',
  `brand_color` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '品牌色（用于车辆涂装展示）',
  `account_user_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '承运商登录账号用户ID',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_carrier_tenant_name` (`tenant_id`, `carrier_name`),
  KEY `idx_carrier_tenant_phone` (`tenant_id`, `contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='承运商';

-- 货车
DROP TABLE IF EXISTS `biz_vehicle`;
CREATE TABLE `biz_vehicle` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `carrier_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '所属承运商ID',
  `plate_no` varchar(16) COLLATE utf8mb3_bin NOT NULL COMMENT '车牌号',
  `vehicle_type` varchar(20) COLLATE utf8mb3_bin NOT NULL COMMENT '车型：LIGHT_VAN/MEDIUM_BOX/HEAVY_CARGO/SEMI_TRAILER/DUMP/TANKER/FLATBED/CONTAINER',
  `vehicle_length` decimal(6,2) DEFAULT NULL COMMENT '车长（米）',
  `load_capacity` decimal(10,2) DEFAULT NULL COMMENT '核定载重（吨）',
  `body_color` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '车身颜色',
  `driver_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机姓名',
  `driver_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机电话（同时作为司机登录账号）',
  `driver_user_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机登录账号用户ID',
  `transport_status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'IDLE' COMMENT '运输状态：IDLE-空闲，IN_TRANSIT-运输中',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用，DISABLED-停用',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_vehicle_tenant_carrier` (`tenant_id`, `carrier_id`, `transport_status`),
  KEY `idx_vehicle_tenant_plate` (`tenant_id`, `plate_no`),
  KEY `idx_vehicle_tenant_driver` (`tenant_id`, `driver_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='货车';

-- 分派承运
DROP TABLE IF EXISTS `biz_dispatch`;
CREATE TABLE `biz_dispatch` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `berth_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位ID',
  `cargo_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位到货ID',
  `carrier_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '承运商ID',
  `vehicle_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '车辆ID',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'WAITING' COMMENT '状态：WAITING-待装载，DEPARTED-已出发，CANCELLED-已取消',
  `trip_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '出发后生成的运输单ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_dispatch_tenant_berth_status` (`tenant_id`, `berth_id`, `status`),
  KEY `idx_dispatch_tenant_cargo` (`tenant_id`, `cargo_id`),
  KEY `idx_dispatch_vehicle` (`vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='分派承运';

-- 运输单
DROP TABLE IF EXISTS `biz_transport_trip`;
CREATE TABLE `biz_transport_trip` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `trip_no` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '运输单号',
  `dispatch_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '分派承运ID',
  `port_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '港口ID',
  `berth_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位ID',
  `cargo_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位到货ID',
  `carrier_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '承运商ID',
  `vehicle_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '车辆ID',
  `plate_no` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '车牌号（冗余）',
  `driver_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机姓名（冗余）',
  `driver_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机电话（冗余）',
  `cargo_name` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '货物名称（冗余）',
  `cargo_type_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '货物类型名称（冗余）',
  `load_tonnage` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装载吨数',
  `load_volume` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装载方数',
  `load_pieces` int NOT NULL DEFAULT 0 COMMENT '装载件数',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'IN_TRANSIT' COMMENT '状态：IN_TRANSIT-运输中，COMPLETED-已完成',
  `depart_time` datetime DEFAULT NULL COMMENT '出发时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成运输时间',
  `complete_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '设置完成运输的操作人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_trip_tenant_carrier_status` (`tenant_id`, `carrier_id`, `status`),
  KEY `idx_trip_tenant_vehicle_status` (`tenant_id`, `vehicle_id`, `status`),
  KEY `idx_trip_tenant_cargo` (`tenant_id`, `cargo_id`),
  KEY `idx_trip_tenant_depart` (`tenant_id`, `depart_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='运输单';

-- 出门条
DROP TABLE IF EXISTS `biz_gate_pass`;
CREATE TABLE `biz_gate_pass` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `pass_no` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '出门条编号',
  `access_token` varchar(64) COLLATE utf8mb3_bin NOT NULL COMMENT '访问令牌（司机链接与二维码使用，不可猜测）',
  `trip_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '运输单ID',
  `carrier_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '承运商ID',
  `carrier_name` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '承运商名称（快照）',
  `vehicle_id` varchar(36) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '车辆ID',
  `plate_no` varchar(16) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '车牌号（快照）',
  `vehicle_type` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '车型（快照）',
  `driver_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机姓名（快照）',
  `driver_phone` varchar(20) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '司机电话（快照）',
  `port_name` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '港口名称（快照）',
  `berth_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '泊位名称（快照）',
  `cargo_name` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '货物名称（快照）',
  `cargo_type_name` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '货物类型（快照）',
  `load_tonnage` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装载吨数（快照）',
  `load_volume` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装载方数（快照）',
  `load_pieces` int NOT NULL DEFAULT 0 COMMENT '装载件数（快照）',
  `issue_time` datetime DEFAULT NULL COMMENT '开具时间',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未核销，VERIFIED-已核销',
  `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
  `verify_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '核销人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  UNIQUE KEY `uk_gate_pass_token` (`access_token`),
  KEY `idx_gp_tenant_trip` (`tenant_id`, `trip_id`),
  KEY `idx_gp_tenant_status_issue` (`tenant_id`, `status`, `issue_time`),
  KEY `idx_gp_tenant_vehicle` (`tenant_id`, `vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='出门条';

-- ============================================================
-- 初始化数据：平台租户、演示租户（涪陵龙头港）、菜单、内置角色、演示业务数据
-- 默认密码均为 123456
-- ============================================================
INSERT INTO `sys_tenant` (`id`, `tenant_code`, `tenant_name`, `contact_name`, `contact_phone`, `status`, `remark`, `create_by`) VALUES
  ('000000', 'platform', '港慧通平台', '平台管理员', '13000000000', 'ENABLED', '平台租户，内置角色模板所在租户', 'system'),
  ('t-fllt', 'fllt', '涪陵龙头港', '港务调度中心', '02372000000', 'ENABLED', '演示租户', 'system');

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `create_by`) VALUES
  ('m_dashboard', '0', '工作台', 'MENU', '/dashboard', 'dashboard/index', 'dashboard:view', 'Odometer', 1, 1, 'ENABLED', 'system'),
  ('m_port', '0', '港口管理', 'MENU', '/port', 'port/index', 'port:map:list', 'MapLocation', 2, 1, 'ENABLED', 'system'),
  ('m_port_edit', 'm_port', '绘制/编辑港口', 'BUTTON', NULL, NULL, 'port:map:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_berth', '0', '泊位管理', 'MENU', '/berth', 'berth/index', 'berth:board:list', 'Ship', 3, 1, 'ENABLED', 'system'),
  ('m_berth_arrive', 'm_berth', '泊位到货', 'BUTTON', NULL, NULL, 'berth:cargo:arrive', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_berth_assign', 'm_berth', '分派承运', 'BUTTON', NULL, NULL, 'berth:dispatch:assign', NULL, 2, 1, 'ENABLED', 'system'),
  ('m_berth_depart', 'm_berth', '装载出发', 'BUTTON', NULL, NULL, 'berth:dispatch:depart', NULL, 3, 1, 'ENABLED', 'system'),
  ('m_berth_work', 'm_berth', '作业设置', 'BUTTON', NULL, NULL, 'berth:work:edit', NULL, 4, 1, 'ENABLED', 'system'),
  ('m_carrier', '0', '承运商管理', 'MENU', '/carrier', 'carrier/index', 'carrier:list', 'Van', 4, 1, 'ENABLED', 'system'),
  ('m_carrier_edit', 'm_carrier', '编辑承运商', 'BUTTON', NULL, NULL, 'carrier:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_vehicle_list', 'm_carrier', '查看车辆', 'BUTTON', NULL, NULL, 'vehicle:list', NULL, 2, 1, 'ENABLED', 'system'),
  ('m_vehicle_edit', 'm_carrier', '编辑车辆', 'BUTTON', NULL, NULL, 'vehicle:edit', NULL, 3, 1, 'ENABLED', 'system'),
  ('m_trip_list', 'm_carrier', '查看运输单', 'BUTTON', NULL, NULL, 'trip:list', NULL, 4, 1, 'ENABLED', 'system'),
  ('m_trip_complete', 'm_carrier', '设置完成运输', 'BUTTON', NULL, NULL, 'trip:complete', NULL, 5, 1, 'ENABLED', 'system'),
  ('m_my_fleet', '0', '我的车队', 'MENU', '/my-fleet', 'carrier/mine', 'carrier:self', 'Van', 5, 1, 'ENABLED', 'system'),
  ('m_shipper', '0', '船运公司管理', 'MENU', '/shipper', 'shipper/index', 'shipper:list', 'UserFilled', 6, 1, 'ENABLED', 'system'),
  ('m_shipper_edit', 'm_shipper', '编辑船运公司/船只', 'BUTTON', NULL, NULL, 'shipper:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_gate', '0', '出门条', 'DIR', '/gate', NULL, NULL, 'Tickets', 7, 1, 'ENABLED', 'system'),
  ('m_gate_list', 'm_gate', '出门条记录', 'MENU', '/gate/pass', 'gatepass/index', 'gate:pass:list', 'Document', 1, 1, 'ENABLED', 'system'),
  ('m_gate_verify', 'm_gate', '扫码核销', 'MENU', '/gate/scan', 'gatepass/scan', 'gate:pass:verify', 'Aim', 2, 1, 'ENABLED', 'system'),
  ('m_gate_mine', 'm_gate', '我的出门条', 'MENU', '/gate/mine', 'gatepass/mine', 'gate:pass:mine', 'Postcard', 3, 1, 'ENABLED', 'system'),
  ('m_base', '0', '基础数据', 'DIR', '/base', NULL, NULL, 'Collection', 8, 1, 'ENABLED', 'system'),
  ('m_cargo_type', 'm_base', '货物类型', 'MENU', '/base/cargo-type', 'base/cargo-type', 'cargo:type:list', 'Box', 1, 1, 'ENABLED', 'system'),
  ('m_cargo_type_edit', 'm_cargo_type', '编辑货物类型', 'BUTTON', NULL, NULL, 'cargo:type:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_sys', '0', '系统管理', 'DIR', '/system', NULL, NULL, 'Setting', 9, 1, 'ENABLED', 'system'),
  ('m_sys_user', 'm_sys', '用户管理', 'MENU', '/system/user', 'system/user', 'sys:user:list', 'User', 1, 1, 'ENABLED', 'system'),
  ('m_sys_user_edit', 'm_sys_user', '编辑用户', 'BUTTON', NULL, NULL, 'sys:user:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_sys_role', 'm_sys', '角色管理', 'MENU', '/system/role', 'system/role', 'sys:role:list', 'Avatar', 2, 1, 'ENABLED', 'system'),
  ('m_sys_role_edit', 'm_sys_role', '编辑角色', 'BUTTON', NULL, NULL, 'sys:role:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_sys_menu', 'm_sys', '菜单管理', 'MENU', '/system/menu', 'system/menu', 'sys:menu:list', 'Menu', 3, 1, 'ENABLED', 'system'),
  ('m_sys_menu_edit', 'm_sys_menu', '编辑菜单', 'BUTTON', NULL, NULL, 'sys:menu:edit', NULL, 1, 1, 'ENABLED', 'system'),
  ('m_sys_tenant', 'm_sys', '租户管理', 'MENU', '/system/tenant', 'system/tenant', 'sys:tenant:list', 'OfficeBuilding', 4, 1, 'ENABLED', 'system'),
  ('m_sys_tenant_edit', 'm_sys_tenant', '编辑租户', 'BUTTON', NULL, NULL, 'sys:tenant:edit', NULL, 1, 1, 'ENABLED', 'system');

INSERT INTO `sys_role` (`id`, `tenant_id`, `role_code`, `role_name`, `builtin`, `sort`, `status`, `remark`, `create_by`) VALUES
  ('r_p_super', '000000', 'SUPER_ADMIN', '超级管理员', 1, 1, 'ENABLED', '平台超级管理员，拥有全部权限', 'system'),
  ('r_p_tenant_admin', '000000', 'TENANT_ADMIN', '租户管理员', 1, 2, 'ENABLED', '港口租户的最高管理员', 'system'),
  ('r_p_port_operator', '000000', 'PORT_OPERATOR', '港口调度员', 1, 3, 'ENABLED', '负责泊位作业与承运调度', 'system'),
  ('r_p_carrier', '000000', 'CARRIER', '承运商', 1, 4, 'ENABLED', '新增承运商时自动赋予', 'system'),
  ('r_p_driver', '000000', 'DRIVER', '司机', 1, 5, 'ENABLED', '新增车辆时自动赋予司机', 'system'),
  ('r_p_guard', '000000', 'GUARD', '门卫', 1, 6, 'ENABLED', '扫码核销出门条', 'system'),
  ('r_f_tenant_admin', 't-fllt', 'TENANT_ADMIN', '租户管理员', 1, 2, 'ENABLED', '港口租户的最高管理员', 'system'),
  ('r_f_port_operator', 't-fllt', 'PORT_OPERATOR', '港口调度员', 1, 3, 'ENABLED', '负责泊位作业与承运调度', 'system'),
  ('r_f_carrier', 't-fllt', 'CARRIER', '承运商', 1, 4, 'ENABLED', '新增承运商时自动赋予', 'system'),
  ('r_f_driver', 't-fllt', 'DRIVER', '司机', 1, 5, 'ENABLED', '新增车辆时自动赋予司机', 'system'),
  ('r_f_guard', 't-fllt', 'GUARD', '门卫', 1, 6, 'ENABLED', '扫码核销出门条', 'system');

INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_by`) VALUES
  ('rm_p_super_m_dashboard', '000000', 'r_p_super', 'm_dashboard', 'system'),
  ('rm_p_super_m_port', '000000', 'r_p_super', 'm_port', 'system'),
  ('rm_p_super_m_port_edit', '000000', 'r_p_super', 'm_port_edit', 'system'),
  ('rm_p_super_m_berth', '000000', 'r_p_super', 'm_berth', 'system'),
  ('rm_p_super_m_berth_arrive', '000000', 'r_p_super', 'm_berth_arrive', 'system'),
  ('rm_p_super_m_berth_assign', '000000', 'r_p_super', 'm_berth_assign', 'system'),
  ('rm_p_super_m_berth_depart', '000000', 'r_p_super', 'm_berth_depart', 'system'),
  ('rm_p_super_m_berth_work', '000000', 'r_p_super', 'm_berth_work', 'system'),
  ('rm_p_super_m_carrier', '000000', 'r_p_super', 'm_carrier', 'system'),
  ('rm_p_super_m_carrier_edit', '000000', 'r_p_super', 'm_carrier_edit', 'system'),
  ('rm_p_super_m_vehicle_list', '000000', 'r_p_super', 'm_vehicle_list', 'system'),
  ('rm_p_super_m_vehicle_edit', '000000', 'r_p_super', 'm_vehicle_edit', 'system'),
  ('rm_p_super_m_trip_list', '000000', 'r_p_super', 'm_trip_list', 'system'),
  ('rm_p_super_m_trip_complete', '000000', 'r_p_super', 'm_trip_complete', 'system'),
  ('rm_p_super_m_my_fleet', '000000', 'r_p_super', 'm_my_fleet', 'system'),
  ('rm_p_super_m_shipper', '000000', 'r_p_super', 'm_shipper', 'system'),
  ('rm_p_super_m_shipper_edit', '000000', 'r_p_super', 'm_shipper_edit', 'system'),
  ('rm_p_super_m_gate', '000000', 'r_p_super', 'm_gate', 'system'),
  ('rm_p_super_m_gate_list', '000000', 'r_p_super', 'm_gate_list', 'system'),
  ('rm_p_super_m_gate_verify', '000000', 'r_p_super', 'm_gate_verify', 'system'),
  ('rm_p_super_m_gate_mine', '000000', 'r_p_super', 'm_gate_mine', 'system'),
  ('rm_p_super_m_base', '000000', 'r_p_super', 'm_base', 'system'),
  ('rm_p_super_m_cargo_type', '000000', 'r_p_super', 'm_cargo_type', 'system'),
  ('rm_p_super_m_cargo_type_edit', '000000', 'r_p_super', 'm_cargo_type_edit', 'system'),
  ('rm_p_super_m_sys', '000000', 'r_p_super', 'm_sys', 'system'),
  ('rm_p_super_m_sys_user', '000000', 'r_p_super', 'm_sys_user', 'system'),
  ('rm_p_super_m_sys_user_edit', '000000', 'r_p_super', 'm_sys_user_edit', 'system'),
  ('rm_p_super_m_sys_role', '000000', 'r_p_super', 'm_sys_role', 'system'),
  ('rm_p_super_m_sys_role_edit', '000000', 'r_p_super', 'm_sys_role_edit', 'system'),
  ('rm_p_super_m_sys_menu', '000000', 'r_p_super', 'm_sys_menu', 'system'),
  ('rm_p_super_m_sys_menu_edit', '000000', 'r_p_super', 'm_sys_menu_edit', 'system'),
  ('rm_p_super_m_sys_tenant', '000000', 'r_p_super', 'm_sys_tenant', 'system'),
  ('rm_p_super_m_sys_tenant_edit', '000000', 'r_p_super', 'm_sys_tenant_edit', 'system'),
  ('rm_p_tenant_admin_m_dashboard', '000000', 'r_p_tenant_admin', 'm_dashboard', 'system'),
  ('rm_p_tenant_admin_m_port', '000000', 'r_p_tenant_admin', 'm_port', 'system'),
  ('rm_p_tenant_admin_m_port_edit', '000000', 'r_p_tenant_admin', 'm_port_edit', 'system'),
  ('rm_p_tenant_admin_m_berth', '000000', 'r_p_tenant_admin', 'm_berth', 'system'),
  ('rm_p_tenant_admin_m_berth_arrive', '000000', 'r_p_tenant_admin', 'm_berth_arrive', 'system'),
  ('rm_p_tenant_admin_m_berth_assign', '000000', 'r_p_tenant_admin', 'm_berth_assign', 'system'),
  ('rm_p_tenant_admin_m_berth_depart', '000000', 'r_p_tenant_admin', 'm_berth_depart', 'system'),
  ('rm_p_tenant_admin_m_berth_work', '000000', 'r_p_tenant_admin', 'm_berth_work', 'system'),
  ('rm_p_tenant_admin_m_carrier', '000000', 'r_p_tenant_admin', 'm_carrier', 'system'),
  ('rm_p_tenant_admin_m_carrier_edit', '000000', 'r_p_tenant_admin', 'm_carrier_edit', 'system'),
  ('rm_p_tenant_admin_m_vehicle_list', '000000', 'r_p_tenant_admin', 'm_vehicle_list', 'system'),
  ('rm_p_tenant_admin_m_vehicle_edit', '000000', 'r_p_tenant_admin', 'm_vehicle_edit', 'system'),
  ('rm_p_tenant_admin_m_trip_list', '000000', 'r_p_tenant_admin', 'm_trip_list', 'system'),
  ('rm_p_tenant_admin_m_trip_complete', '000000', 'r_p_tenant_admin', 'm_trip_complete', 'system'),
  ('rm_p_tenant_admin_m_shipper', '000000', 'r_p_tenant_admin', 'm_shipper', 'system'),
  ('rm_p_tenant_admin_m_shipper_edit', '000000', 'r_p_tenant_admin', 'm_shipper_edit', 'system'),
  ('rm_p_tenant_admin_m_gate', '000000', 'r_p_tenant_admin', 'm_gate', 'system'),
  ('rm_p_tenant_admin_m_gate_list', '000000', 'r_p_tenant_admin', 'm_gate_list', 'system'),
  ('rm_p_tenant_admin_m_gate_verify', '000000', 'r_p_tenant_admin', 'm_gate_verify', 'system'),
  ('rm_p_tenant_admin_m_base', '000000', 'r_p_tenant_admin', 'm_base', 'system'),
  ('rm_p_tenant_admin_m_cargo_type', '000000', 'r_p_tenant_admin', 'm_cargo_type', 'system'),
  ('rm_p_tenant_admin_m_cargo_type_edit', '000000', 'r_p_tenant_admin', 'm_cargo_type_edit', 'system'),
  ('rm_p_tenant_admin_m_sys', '000000', 'r_p_tenant_admin', 'm_sys', 'system'),
  ('rm_p_tenant_admin_m_sys_user', '000000', 'r_p_tenant_admin', 'm_sys_user', 'system'),
  ('rm_p_tenant_admin_m_sys_user_edit', '000000', 'r_p_tenant_admin', 'm_sys_user_edit', 'system'),
  ('rm_p_tenant_admin_m_sys_role', '000000', 'r_p_tenant_admin', 'm_sys_role', 'system'),
  ('rm_p_tenant_admin_m_sys_role_edit', '000000', 'r_p_tenant_admin', 'm_sys_role_edit', 'system'),
  ('rm_p_port_operator_m_dashboard', '000000', 'r_p_port_operator', 'm_dashboard', 'system'),
  ('rm_p_port_operator_m_port', '000000', 'r_p_port_operator', 'm_port', 'system'),
  ('rm_p_port_operator_m_port_edit', '000000', 'r_p_port_operator', 'm_port_edit', 'system'),
  ('rm_p_port_operator_m_berth', '000000', 'r_p_port_operator', 'm_berth', 'system'),
  ('rm_p_port_operator_m_berth_arrive', '000000', 'r_p_port_operator', 'm_berth_arrive', 'system'),
  ('rm_p_port_operator_m_berth_assign', '000000', 'r_p_port_operator', 'm_berth_assign', 'system'),
  ('rm_p_port_operator_m_berth_depart', '000000', 'r_p_port_operator', 'm_berth_depart', 'system'),
  ('rm_p_port_operator_m_berth_work', '000000', 'r_p_port_operator', 'm_berth_work', 'system'),
  ('rm_p_port_operator_m_carrier', '000000', 'r_p_port_operator', 'm_carrier', 'system'),
  ('rm_p_port_operator_m_carrier_edit', '000000', 'r_p_port_operator', 'm_carrier_edit', 'system'),
  ('rm_p_port_operator_m_vehicle_list', '000000', 'r_p_port_operator', 'm_vehicle_list', 'system'),
  ('rm_p_port_operator_m_vehicle_edit', '000000', 'r_p_port_operator', 'm_vehicle_edit', 'system'),
  ('rm_p_port_operator_m_trip_list', '000000', 'r_p_port_operator', 'm_trip_list', 'system'),
  ('rm_p_port_operator_m_trip_complete', '000000', 'r_p_port_operator', 'm_trip_complete', 'system'),
  ('rm_p_port_operator_m_shipper', '000000', 'r_p_port_operator', 'm_shipper', 'system'),
  ('rm_p_port_operator_m_shipper_edit', '000000', 'r_p_port_operator', 'm_shipper_edit', 'system'),
  ('rm_p_port_operator_m_gate', '000000', 'r_p_port_operator', 'm_gate', 'system'),
  ('rm_p_port_operator_m_gate_list', '000000', 'r_p_port_operator', 'm_gate_list', 'system'),
  ('rm_p_port_operator_m_base', '000000', 'r_p_port_operator', 'm_base', 'system'),
  ('rm_p_port_operator_m_cargo_type', '000000', 'r_p_port_operator', 'm_cargo_type', 'system'),
  ('rm_p_port_operator_m_cargo_type_edit', '000000', 'r_p_port_operator', 'm_cargo_type_edit', 'system'),
  ('rm_p_carrier_m_my_fleet', '000000', 'r_p_carrier', 'm_my_fleet', 'system'),
  ('rm_p_carrier_m_vehicle_list', '000000', 'r_p_carrier', 'm_vehicle_list', 'system'),
  ('rm_p_carrier_m_vehicle_edit', '000000', 'r_p_carrier', 'm_vehicle_edit', 'system'),
  ('rm_p_carrier_m_trip_complete', '000000', 'r_p_carrier', 'm_trip_complete', 'system'),
  ('rm_p_carrier_m_gate', '000000', 'r_p_carrier', 'm_gate', 'system'),
  ('rm_p_carrier_m_gate_list', '000000', 'r_p_carrier', 'm_gate_list', 'system'),
  ('rm_p_driver_m_gate', '000000', 'r_p_driver', 'm_gate', 'system'),
  ('rm_p_driver_m_gate_mine', '000000', 'r_p_driver', 'm_gate_mine', 'system'),
  ('rm_p_guard_m_gate', '000000', 'r_p_guard', 'm_gate', 'system'),
  ('rm_p_guard_m_gate_verify', '000000', 'r_p_guard', 'm_gate_verify', 'system'),
  ('rm_p_guard_m_gate_list', '000000', 'r_p_guard', 'm_gate_list', 'system'),
  ('rm_f_tenant_admin_m_dashboard', 't-fllt', 'r_f_tenant_admin', 'm_dashboard', 'system'),
  ('rm_f_tenant_admin_m_port', 't-fllt', 'r_f_tenant_admin', 'm_port', 'system'),
  ('rm_f_tenant_admin_m_port_edit', 't-fllt', 'r_f_tenant_admin', 'm_port_edit', 'system'),
  ('rm_f_tenant_admin_m_berth', 't-fllt', 'r_f_tenant_admin', 'm_berth', 'system'),
  ('rm_f_tenant_admin_m_berth_arrive', 't-fllt', 'r_f_tenant_admin', 'm_berth_arrive', 'system'),
  ('rm_f_tenant_admin_m_berth_assign', 't-fllt', 'r_f_tenant_admin', 'm_berth_assign', 'system'),
  ('rm_f_tenant_admin_m_berth_depart', 't-fllt', 'r_f_tenant_admin', 'm_berth_depart', 'system'),
  ('rm_f_tenant_admin_m_berth_work', 't-fllt', 'r_f_tenant_admin', 'm_berth_work', 'system'),
  ('rm_f_tenant_admin_m_carrier', 't-fllt', 'r_f_tenant_admin', 'm_carrier', 'system'),
  ('rm_f_tenant_admin_m_carrier_edit', 't-fllt', 'r_f_tenant_admin', 'm_carrier_edit', 'system'),
  ('rm_f_tenant_admin_m_vehicle_list', 't-fllt', 'r_f_tenant_admin', 'm_vehicle_list', 'system'),
  ('rm_f_tenant_admin_m_vehicle_edit', 't-fllt', 'r_f_tenant_admin', 'm_vehicle_edit', 'system'),
  ('rm_f_tenant_admin_m_trip_list', 't-fllt', 'r_f_tenant_admin', 'm_trip_list', 'system'),
  ('rm_f_tenant_admin_m_trip_complete', 't-fllt', 'r_f_tenant_admin', 'm_trip_complete', 'system'),
  ('rm_f_tenant_admin_m_shipper', 't-fllt', 'r_f_tenant_admin', 'm_shipper', 'system'),
  ('rm_f_tenant_admin_m_shipper_edit', 't-fllt', 'r_f_tenant_admin', 'm_shipper_edit', 'system'),
  ('rm_f_tenant_admin_m_gate', 't-fllt', 'r_f_tenant_admin', 'm_gate', 'system'),
  ('rm_f_tenant_admin_m_gate_list', 't-fllt', 'r_f_tenant_admin', 'm_gate_list', 'system'),
  ('rm_f_tenant_admin_m_gate_verify', 't-fllt', 'r_f_tenant_admin', 'm_gate_verify', 'system'),
  ('rm_f_tenant_admin_m_base', 't-fllt', 'r_f_tenant_admin', 'm_base', 'system'),
  ('rm_f_tenant_admin_m_cargo_type', 't-fllt', 'r_f_tenant_admin', 'm_cargo_type', 'system'),
  ('rm_f_tenant_admin_m_cargo_type_edit', 't-fllt', 'r_f_tenant_admin', 'm_cargo_type_edit', 'system'),
  ('rm_f_tenant_admin_m_sys', 't-fllt', 'r_f_tenant_admin', 'm_sys', 'system'),
  ('rm_f_tenant_admin_m_sys_user', 't-fllt', 'r_f_tenant_admin', 'm_sys_user', 'system'),
  ('rm_f_tenant_admin_m_sys_user_edit', 't-fllt', 'r_f_tenant_admin', 'm_sys_user_edit', 'system'),
  ('rm_f_tenant_admin_m_sys_role', 't-fllt', 'r_f_tenant_admin', 'm_sys_role', 'system'),
  ('rm_f_tenant_admin_m_sys_role_edit', 't-fllt', 'r_f_tenant_admin', 'm_sys_role_edit', 'system'),
  ('rm_f_port_operator_m_dashboard', 't-fllt', 'r_f_port_operator', 'm_dashboard', 'system'),
  ('rm_f_port_operator_m_port', 't-fllt', 'r_f_port_operator', 'm_port', 'system'),
  ('rm_f_port_operator_m_port_edit', 't-fllt', 'r_f_port_operator', 'm_port_edit', 'system'),
  ('rm_f_port_operator_m_berth', 't-fllt', 'r_f_port_operator', 'm_berth', 'system'),
  ('rm_f_port_operator_m_berth_arrive', 't-fllt', 'r_f_port_operator', 'm_berth_arrive', 'system'),
  ('rm_f_port_operator_m_berth_assign', 't-fllt', 'r_f_port_operator', 'm_berth_assign', 'system'),
  ('rm_f_port_operator_m_berth_depart', 't-fllt', 'r_f_port_operator', 'm_berth_depart', 'system'),
  ('rm_f_port_operator_m_berth_work', 't-fllt', 'r_f_port_operator', 'm_berth_work', 'system'),
  ('rm_f_port_operator_m_carrier', 't-fllt', 'r_f_port_operator', 'm_carrier', 'system'),
  ('rm_f_port_operator_m_carrier_edit', 't-fllt', 'r_f_port_operator', 'm_carrier_edit', 'system'),
  ('rm_f_port_operator_m_vehicle_list', 't-fllt', 'r_f_port_operator', 'm_vehicle_list', 'system'),
  ('rm_f_port_operator_m_vehicle_edit', 't-fllt', 'r_f_port_operator', 'm_vehicle_edit', 'system'),
  ('rm_f_port_operator_m_trip_list', 't-fllt', 'r_f_port_operator', 'm_trip_list', 'system'),
  ('rm_f_port_operator_m_trip_complete', 't-fllt', 'r_f_port_operator', 'm_trip_complete', 'system'),
  ('rm_f_port_operator_m_shipper', 't-fllt', 'r_f_port_operator', 'm_shipper', 'system'),
  ('rm_f_port_operator_m_shipper_edit', 't-fllt', 'r_f_port_operator', 'm_shipper_edit', 'system'),
  ('rm_f_port_operator_m_gate', 't-fllt', 'r_f_port_operator', 'm_gate', 'system'),
  ('rm_f_port_operator_m_gate_list', 't-fllt', 'r_f_port_operator', 'm_gate_list', 'system'),
  ('rm_f_port_operator_m_base', 't-fllt', 'r_f_port_operator', 'm_base', 'system'),
  ('rm_f_port_operator_m_cargo_type', 't-fllt', 'r_f_port_operator', 'm_cargo_type', 'system'),
  ('rm_f_port_operator_m_cargo_type_edit', 't-fllt', 'r_f_port_operator', 'm_cargo_type_edit', 'system'),
  ('rm_f_carrier_m_my_fleet', 't-fllt', 'r_f_carrier', 'm_my_fleet', 'system'),
  ('rm_f_carrier_m_vehicle_list', 't-fllt', 'r_f_carrier', 'm_vehicle_list', 'system'),
  ('rm_f_carrier_m_vehicle_edit', 't-fllt', 'r_f_carrier', 'm_vehicle_edit', 'system'),
  ('rm_f_carrier_m_trip_complete', 't-fllt', 'r_f_carrier', 'm_trip_complete', 'system'),
  ('rm_f_carrier_m_gate', 't-fllt', 'r_f_carrier', 'm_gate', 'system'),
  ('rm_f_carrier_m_gate_list', 't-fllt', 'r_f_carrier', 'm_gate_list', 'system'),
  ('rm_f_driver_m_gate', 't-fllt', 'r_f_driver', 'm_gate', 'system'),
  ('rm_f_driver_m_gate_mine', 't-fllt', 'r_f_driver', 'm_gate_mine', 'system'),
  ('rm_f_guard_m_gate', 't-fllt', 'r_f_guard', 'm_gate', 'system'),
  ('rm_f_guard_m_gate_verify', 't-fllt', 'r_f_guard', 'm_gate_verify', 'system'),
  ('rm_f_guard_m_gate_list', 't-fllt', 'r_f_guard', 'm_gate_list', 'system');

INSERT INTO `biz_cargo_type` (`id`, `tenant_id`, `type_name`, `type_code`, `color`, `sort`, `create_by`) VALUES
  ('ct_coal', 't-fllt', '煤炭', 'COAL', '#3b3b3b', 1, 'system'),
  ('ct_ore', 't-fllt', '矿石', 'ORE', '#8c5a3c', 2, 'system'),
  ('ct_steel', 't-fllt', '钢材', 'STEEL', '#5b6b7b', 3, 'system'),
  ('ct_grain', 't-fllt', '粮食', 'GRAIN', '#d8a537', 4, 'system'),
  ('ct_container', 't-fllt', '集装箱', 'CONTAINER', '#1f6fd1', 5, 'system'),
  ('ct_sand', 't-fllt', '砂石建材', 'SAND', '#c2a27a', 6, 'system'),
  ('ct_mustard', 't-fllt', '榨菜及农副产品', 'AGRI', '#5aa34a', 7, 'system'),
  ('ct_chem', 't-fllt', '化工品', 'CHEM', '#9b59b6', 8, 'system');

INSERT INTO `biz_shipping_company` (`id`, `tenant_id`, `company_name`, `short_name`, `contact_name`, `contact_phone`, `address`, `create_by`) VALUES
  ('sp1', 't-fllt', '川东煤业有限公司', '川东煤业', '陈经理', '13500000001', '重庆市涪陵区李渡街道', 'system'),
  ('sp2', 't-fllt', '渝江钢铁贸易有限公司', '渝江钢贸', '刘经理', '13500000002', '重庆市涪陵区龙桥街道', 'system'),
  ('sp3', 't-fllt', '涪州粮油集团', '涪州粮油', '周经理', '13500000003', '重庆市涪陵区江北街道', 'system'),
  ('sp4', 't-fllt', '长平建材有限公司', '长平建材', '吴经理', '13500000004', '重庆市涪陵区白涛街道', 'system');

INSERT INTO `biz_ship` (`id`, `tenant_id`, `company_id`, `ship_name`, `ship_no`, `ship_type`, `ship_length`, `deadweight`, `hull_color`, `captain_name`, `captain_phone`, `create_by`) VALUES
  ('sh1', 't-fllt', 'sp1', '川涪货1688', 'CN-CQ-1688', 'BULK', 96, 5200, '#c0392b', '船长88', '13700000000', 'system'),
  ('sh2', 't-fllt', 'sp1', '川涪货2266', 'CN-CQ-2266', 'BARGE', 68, 2800, '#2c3e50', '船长66', '13700000001', 'system'),
  ('sh3', 't-fllt', 'sp2', '渝江之星', 'CN-CQ-3301', 'GENERAL', 88, 4500, '#1f6fd1', '船长之星', '13700000002', 'system'),
  ('sh4', 't-fllt', 'sp3', '涪州粮运8号', 'CN-CQ-4808', 'BULK', 82, 3600, '#27ae60', '船长8号', '13700000003', 'system'),
  ('sh5', 't-fllt', 'sp4', '长平12号', 'CN-CQ-5112', 'BARGE', 60, 2200, '#d35400', '船长2号', '13700000004', 'system'),
  ('sh6', 't-fllt', 'sp2', '渝江集装箱01', 'CN-CQ-6001', 'CONTAINER', 110, 7800, '#16a085', '船长01', '13700000005', 'system');

INSERT INTO `biz_carrier` (`id`, `tenant_id`, `carrier_name`, `short_name`, `contact_name`, `contact_phone`, `address`, `brand_color`, `account_user_id`, `status`, `create_by`) VALUES
  ('ca1', 't-fllt', '涪陵顺达物流有限公司', '顺达物流', '张顺达', '13800000001', '重庆市涪陵区', '#e67e22', 'u_f_ca1', 'ENABLED', 'system'),
  ('ca2', 't-fllt', '重庆渝运通运输有限公司', '渝运通', '李运通', '13800000002', '重庆市涪陵区', '#2980b9', 'u_f_ca2', 'ENABLED', 'system'),
  ('ca3', 't-fllt', '长江联运车队', '长江联运', '王联运', '13800000003', '重庆市涪陵区', '#27ae60', 'u_f_ca3', 'ENABLED', 'system');

INSERT INTO `biz_vehicle` (`id`, `tenant_id`, `carrier_id`, `plate_no`, `vehicle_type`, `vehicle_length`, `load_capacity`, `body_color`, `driver_name`, `driver_phone`, `driver_user_id`, `transport_status`, `status`, `create_by`) VALUES
  ('v1', 't-fllt', 'ca1', '渝G·A1688', 'SEMI_TRAILER', 17.5, 34, '#e67e22', '赵师傅', '13900000001', 'u_f_v1', 'IN_TRANSIT', 'ENABLED', 'system'),
  ('v2', 't-fllt', 'ca1', '渝G·A2399', 'DUMP', 8.6, 25, '#f1c40f', '钱师傅', '13900000002', 'u_f_v2', 'IDLE', 'ENABLED', 'system'),
  ('v3', 't-fllt', 'ca1', '渝G·A3520', 'HEAVY_CARGO', 9.6, 18, '#e74c3c', '孙师傅', '13900000003', 'u_f_v3', 'IDLE', 'ENABLED', 'system'),
  ('v4', 't-fllt', 'ca2', '渝A·B6620', 'CONTAINER', 12.5, 30, '#2980b9', '周师傅', '13900000004', 'u_f_v4', 'IN_TRANSIT', 'ENABLED', 'system'),
  ('v5', 't-fllt', 'ca2', '渝A·B7731', 'MEDIUM_BOX', 6.8, 8, '#8e44ad', '吴师傅', '13900000005', 'u_f_v5', 'IDLE', 'ENABLED', 'system'),
  ('v6', 't-fllt', 'ca2', '渝A·B8842', 'FLATBED', 13, 32, '#34495e', '郑师傅', '13900000006', 'u_f_v6', 'IDLE', 'ENABLED', 'system'),
  ('v7', 't-fllt', 'ca3', '渝G·C1001', 'TANKER', 11, 20, '#95a5a6', '冯师傅', '13900000007', 'u_f_v7', 'IDLE', 'ENABLED', 'system'),
  ('v8', 't-fllt', 'ca3', '渝G·C2002', 'LIGHT_VAN', 4.2, 2, '#1abc9c', '陈师傅', '13900000008', 'u_f_v8', 'IDLE', 'ENABLED', 'system'),
  ('v9', 't-fllt', 'ca3', '渝G·C3003', 'DUMP', 8.6, 25, '#27ae60', '褚师傅', '13900000009', 'u_f_v9', 'IN_TRANSIT', 'ENABLED', 'system');

INSERT INTO `sys_user` (`id`, `tenant_id`, `username`, `password`, `salt`, `nickname`, `phone`, `user_type`, `ref_id`, `status`, `create_by`) VALUES
  ('u_p_admin', '000000', 'admin', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '平台管理员', '13000000000', 'ADMIN', NULL, 'ENABLED', 'system'),
  ('u_f_admin', 't-fllt', 'admin', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '龙头港管理员', '13100000000', 'ADMIN', NULL, 'ENABLED', 'system'),
  ('u_f_dd', 't-fllt', 'diaodu', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '调度员小王', '13100000001', 'STAFF', NULL, 'ENABLED', 'system'),
  ('u_f_mw', 't-fllt', 'menwei', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '门卫老李', '13100000002', 'GUARD', NULL, 'ENABLED', 'system'),
  ('u_f_ca1', 't-fllt', '13800000001', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '张顺达', '13800000001', 'CARRIER', 'ca1', 'ENABLED', 'system'),
  ('u_f_ca2', 't-fllt', '13800000002', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '李运通', '13800000002', 'CARRIER', 'ca2', 'ENABLED', 'system'),
  ('u_f_ca3', 't-fllt', '13800000003', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '王联运', '13800000003', 'CARRIER', 'ca3', 'ENABLED', 'system'),
  ('u_f_v1', 't-fllt', '13900000001', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '赵师傅', '13900000001', 'DRIVER', 'v1', 'ENABLED', 'system'),
  ('u_f_v2', 't-fllt', '13900000002', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '钱师傅', '13900000002', 'DRIVER', 'v2', 'ENABLED', 'system'),
  ('u_f_v3', 't-fllt', '13900000003', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '孙师傅', '13900000003', 'DRIVER', 'v3', 'ENABLED', 'system'),
  ('u_f_v4', 't-fllt', '13900000004', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '周师傅', '13900000004', 'DRIVER', 'v4', 'ENABLED', 'system'),
  ('u_f_v5', 't-fllt', '13900000005', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '吴师傅', '13900000005', 'DRIVER', 'v5', 'ENABLED', 'system'),
  ('u_f_v6', 't-fllt', '13900000006', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '郑师傅', '13900000006', 'DRIVER', 'v6', 'ENABLED', 'system'),
  ('u_f_v7', 't-fllt', '13900000007', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '冯师傅', '13900000007', 'DRIVER', 'v7', 'ENABLED', 'system'),
  ('u_f_v8', 't-fllt', '13900000008', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '陈师傅', '13900000008', 'DRIVER', 'v8', 'ENABLED', 'system'),
  ('u_f_v9', 't-fllt', '13900000009', '9d17cb274051c13c15c5763bbe0548c2ca28f46f492b5ee593af4d434df9a7dd', 'ght2026salt00001', '褚师傅', '13900000009', 'DRIVER', 'v9', 'ENABLED', 'system');

INSERT INTO `sys_user_role` (`id`, `tenant_id`, `user_id`, `role_id`, `create_by`) VALUES
  ('ur_u_p_admin', '000000', 'u_p_admin', 'r_p_super', 'system'),
  ('ur_u_f_admin', 't-fllt', 'u_f_admin', 'r_f_tenant_admin', 'system'),
  ('ur_u_f_dd', 't-fllt', 'u_f_dd', 'r_f_port_operator', 'system'),
  ('ur_u_f_mw', 't-fllt', 'u_f_mw', 'r_f_guard', 'system'),
  ('ur_u_f_ca1', 't-fllt', 'u_f_ca1', 'r_f_carrier', 'system'),
  ('ur_u_f_ca2', 't-fllt', 'u_f_ca2', 'r_f_carrier', 'system'),
  ('ur_u_f_ca3', 't-fllt', 'u_f_ca3', 'r_f_carrier', 'system'),
  ('ur_u_f_v1', 't-fllt', 'u_f_v1', 'r_f_driver', 'system'),
  ('ur_u_f_v2', 't-fllt', 'u_f_v2', 'r_f_driver', 'system'),
  ('ur_u_f_v3', 't-fllt', 'u_f_v3', 'r_f_driver', 'system'),
  ('ur_u_f_v4', 't-fllt', 'u_f_v4', 'r_f_driver', 'system'),
  ('ur_u_f_v5', 't-fllt', 'u_f_v5', 'r_f_driver', 'system'),
  ('ur_u_f_v6', 't-fllt', 'u_f_v6', 'r_f_driver', 'system'),
  ('ur_u_f_v7', 't-fllt', 'u_f_v7', 'r_f_driver', 'system'),
  ('ur_u_f_v8', 't-fllt', 'u_f_v8', 'r_f_driver', 'system'),
  ('ur_u_f_v9', 't-fllt', 'u_f_v9', 'r_f_driver', 'system');

INSERT INTO `biz_port` (`id`, `tenant_id`, `port_name`, `port_code`, `address`, `canvas_width`, `canvas_height`, `theme`, `description`, `create_by`) VALUES
  ('port-fllt', 't-fllt', '涪陵龙头港', 'FLLT', '重庆市涪陵区龙头港区', 1600, 900, 'DAY', '位于长江与乌江交汇处的综合性枢纽港区，承担煤炭、钢材、粮食、集装箱等货类的水陆联运。', 'system');

INSERT INTO `biz_berth` (`id`, `tenant_id`, `port_id`, `berth_code`, `berth_name`, `status`, `work_start_time`, `work_end_time`, `berth_length`, `water_depth`, `max_tonnage`, `create_by`) VALUES
  ('b1', 't-fllt', 'port-fllt', 'B1', '1号煤炭泊位', 'WORKING', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system'),
  ('b2', 't-fllt', 'port-fllt', 'B2', '2号集装箱泊位', 'WORKING', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system'),
  ('b3', 't-fllt', 'port-fllt', 'B3', '3号件杂泊位', 'IDLE', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system'),
  ('b4', 't-fllt', 'port-fllt', 'B4', '4号钢材泊位', 'MAINTENANCE', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system'),
  ('b5', 't-fllt', 'port-fllt', 'B5', '5号粮食泊位', 'IDLE', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system'),
  ('b6', 't-fllt', 'port-fllt', 'B6', '6号砂石泊位', 'WORKING', '07:00:00', '22:00:00', 180, 6.5, 5000, 'system');

INSERT INTO `biz_port_element` (`id`, `tenant_id`, `port_id`, `element_type`, `pos_x`, `pos_y`, `width`, `height`, `rotation`, `layer_index`, `props`, `berth_id`, `create_by`) VALUES
  ('pe001', 't-fllt', 'port-fllt', 'LAND', 0, 300, 1460, 600, 0, 0, '{"variant": "concrete"}', NULL, 'system'),
  ('pe002', 't-fllt', 'port-fllt', 'RIVER', 0, 30, 1600, 270, 0, 1, '{"label": "长  江", "flow": "east"}', NULL, 'system'),
  ('pe003', 't-fllt', 'port-fllt', 'RIVER', 1460, 300, 140, 600, 0, 2, '{"label": "乌江", "flow": "north"}', NULL, 'system'),
  ('pe004', 't-fllt', 'port-fllt', 'ROAD', 0, 420, 1460, 54, 0, 3, '{}', NULL, 'system'),
  ('pe005', 't-fllt', 'port-fllt', 'ROAD', 700, 474, 54, 346, 0, 4, '{}', NULL, 'system'),
  ('pe006', 't-fllt', 'port-fllt', 'BERTH', 80, 228, 190, 86, 0, 5, '{}', 'b1', 'system'),
  ('pe007', 't-fllt', 'port-fllt', 'BERTH', 300, 228, 190, 86, 0, 6, '{}', 'b2', 'system'),
  ('pe008', 't-fllt', 'port-fllt', 'BERTH', 520, 228, 190, 86, 0, 7, '{}', 'b3', 'system'),
  ('pe009', 't-fllt', 'port-fllt', 'BERTH', 740, 228, 190, 86, 0, 8, '{}', 'b4', 'system'),
  ('pe010', 't-fllt', 'port-fllt', 'BERTH', 960, 228, 190, 86, 0, 9, '{}', 'b5', 'system'),
  ('pe011', 't-fllt', 'port-fllt', 'BERTH', 1180, 228, 190, 86, 0, 10, '{}', 'b6', 'system'),
  ('pe012', 't-fllt', 'port-fllt', 'CRANE', 116, 206, 36, 104, 0, 11, '{}', NULL, 'system'),
  ('pe013', 't-fllt', 'port-fllt', 'CRANE', 198, 206, 36, 104, 0, 12, '{}', NULL, 'system'),
  ('pe014', 't-fllt', 'port-fllt', 'CRANE', 336, 206, 36, 104, 0, 13, '{}', NULL, 'system'),
  ('pe015', 't-fllt', 'port-fllt', 'CRANE', 418, 206, 36, 104, 0, 14, '{}', NULL, 'system'),
  ('pe016', 't-fllt', 'port-fllt', 'CRANE', 1216, 206, 36, 104, 0, 15, '{}', NULL, 'system'),
  ('pe017', 't-fllt', 'port-fllt', 'CRANE', 1298, 206, 36, 104, 0, 16, '{}', NULL, 'system'),
  ('pe018', 't-fllt', 'port-fllt', 'CRANE', 556, 206, 36, 104, 0, 17, '{}', NULL, 'system'),
  ('pe019', 't-fllt', 'port-fllt', 'CRANE', 996, 206, 36, 104, 0, 18, '{}', NULL, 'system'),
  ('pe020', 't-fllt', 'port-fllt', 'YARD', 70, 500, 300, 150, 0, 19, '{"label": "煤炭堆场"}', NULL, 'system'),
  ('pe021', 't-fllt', 'port-fllt', 'YARD', 400, 500, 270, 150, 0, 20, '{"label": "集装箱堆场", "containers": true}', NULL, 'system'),
  ('pe022', 't-fllt', 'port-fllt', 'WAREHOUSE', 790, 500, 260, 150, 0, 21, '{"label": "1号仓库"}', NULL, 'system'),
  ('pe023', 't-fllt', 'port-fllt', 'WAREHOUSE', 1080, 500, 300, 150, 0, 22, '{"label": "粮食筒仓"}', NULL, 'system'),
  ('pe024', 't-fllt', 'port-fllt', 'BUILDING', 90, 700, 220, 130, 0, 23, '{"label": "港务调度中心"}', NULL, 'system'),
  ('pe025', 't-fllt', 'port-fllt', 'TANK', 1130, 700, 80, 80, 0, 24, '{}', NULL, 'system'),
  ('pe026', 't-fllt', 'port-fllt', 'TANK', 1230, 700, 80, 80, 0, 25, '{}', NULL, 'system'),
  ('pe027', 't-fllt', 'port-fllt', 'TANK', 1330, 700, 80, 80, 0, 26, '{}', NULL, 'system'),
  ('pe028', 't-fllt', 'port-fllt', 'TANK', 1180, 795, 70, 70, 0, 27, '{}', NULL, 'system'),
  ('pe029', 't-fllt', 'port-fllt', 'TANK', 1280, 795, 70, 70, 0, 28, '{}', NULL, 'system'),
  ('pe030', 't-fllt', 'port-fllt', 'GATE', 672, 822, 110, 64, 0, 29, '{"label": "1号闸口"}', NULL, 'system'),
  ('pe031', 't-fllt', 'port-fllt', 'TREE', 360, 700, 56, 56, 0, 30, '{}', NULL, 'system'),
  ('pe032', 't-fllt', 'port-fllt', 'TREE', 430, 760, 56, 56, 0, 31, '{}', NULL, 'system'),
  ('pe033', 't-fllt', 'port-fllt', 'TREE', 520, 700, 56, 56, 0, 32, '{}', NULL, 'system'),
  ('pe034', 't-fllt', 'port-fllt', 'TREE', 600, 780, 56, 56, 0, 33, '{}', NULL, 'system'),
  ('pe035', 't-fllt', 'port-fllt', 'TREE', 860, 700, 56, 56, 0, 34, '{}', NULL, 'system'),
  ('pe036', 't-fllt', 'port-fllt', 'TREE', 930, 770, 56, 56, 0, 35, '{}', NULL, 'system'),
  ('pe037', 't-fllt', 'port-fllt', 'TREE', 1010, 700, 56, 56, 0, 36, '{}', NULL, 'system'),
  ('pe038', 't-fllt', 'port-fllt', 'TREE', 1040, 800, 56, 56, 0, 37, '{}', NULL, 'system'),
  ('pe039', 't-fllt', 'port-fllt', 'SHIP', 360, 92, 240, 60, 0, 38, '{"color": "#c0392b", "label": "川涪货2266"}', NULL, 'system'),
  ('pe040', 't-fllt', 'port-fllt', 'SHIP', 960, 130, 280, 64, 0, 39, '{"color": "#16a085", "label": "渝江集装箱01"}', NULL, 'system'),
  ('pe041', 't-fllt', 'port-fllt', 'LABEL', 30, 860, 300, 30, 0, 40, '{"text": "涪陵龙头港 · 港区俯视图", "fontSize": 18, "color": "#0f2a6b"}', NULL, 'system');

INSERT INTO `biz_berth_cargo` (`id`, `tenant_id`, `port_id`, `berth_id`, `cargo_type_id`, `cargo_type_name`, `cargo_name`, `tonnage`, `volume`, `pieces`, `loaded_tonnage`, `loaded_volume`, `loaded_pieces`, `status`, `arrive_time`, `create_by`) VALUES
  ('bc1', 't-fllt', 'port-fllt', 'b1', 'ct_coal', '煤炭', '动力煤（5500大卡）', 3000, 3300, 0, 102, 112, 0, 'IN_PROGRESS', '2026-10-28 08:10:00', 'system'),
  ('bc2', 't-fllt', 'port-fllt', 'b2', 'ct_container', '集装箱', '20GP 标准箱（日用百货）', 960, 2100, 48, 28, 60, 2, 'IN_PROGRESS', '2026-10-28 08:10:00', 'system'),
  ('bc3', 't-fllt', 'port-fllt', 'b6', 'ct_sand', '砂石建材', '机制砂', 1800, 1200, 0, 25, 17, 0, 'IN_PROGRESS', '2026-10-28 08:10:00', 'system');

INSERT INTO `biz_berth_cargo_ship` (`id`, `tenant_id`, `cargo_id`, `ship_id`, `company_id`, `create_by`) VALUES
  ('bcs1', 't-fllt', 'bc1', 'sh1', 'sp1', 'system'),
  ('bcs2', 't-fllt', 'bc1', 'sh2', 'sp1', 'system'),
  ('bcs3', 't-fllt', 'bc2', 'sh6', 'sp2', 'system'),
  ('bcs4', 't-fllt', 'bc3', 'sh5', 'sp4', 'system');

INSERT INTO `biz_dispatch` (`id`, `tenant_id`, `berth_id`, `cargo_id`, `carrier_id`, `vehicle_id`, `status`, `trip_id`, `create_by`) VALUES
  ('d1', 't-fllt', 'b1', 'bc1', 'ca1', 'v1', 'DEPARTED', 'tr1', 'system'),
  ('d2', 't-fllt', 'b1', 'bc1', 'ca1', 'v2', 'DEPARTED', 'tr2', 'system'),
  ('d3', 't-fllt', 'b1', 'bc1', 'ca1', 'v3', 'WAITING', NULL, 'system'),
  ('d4', 't-fllt', 'b2', 'bc2', 'ca2', 'v4', 'DEPARTED', 'tr3', 'system'),
  ('d5', 't-fllt', 'b2', 'bc2', 'ca2', 'v6', 'WAITING', NULL, 'system'),
  ('d6', 't-fllt', 'b6', 'bc3', 'ca3', 'v9', 'DEPARTED', 'tr4', 'system');

INSERT INTO `biz_transport_trip` (`id`, `tenant_id`, `trip_no`, `dispatch_id`, `port_id`, `berth_id`, `cargo_id`, `carrier_id`, `vehicle_id`, `plate_no`, `driver_name`, `driver_phone`, `cargo_name`, `cargo_type_name`, `load_tonnage`, `load_volume`, `load_pieces`, `status`, `depart_time`, `complete_time`, `create_by`) VALUES
  ('tr1', 't-fllt', 'TR202610280001', 'd1', 'port-fllt', 'b1', 'bc1', 'ca1', 'v1', '渝G·A1688', '赵师傅', '13900000001', '动力煤（5500大卡）', '煤炭', 34, 37, 0, 'IN_TRANSIT', '2026-10-28 09:20:00', NULL, 'system'),
  ('tr2', 't-fllt', 'TR202610280002', 'd2', 'port-fllt', 'b1', 'bc1', 'ca1', 'v2', '渝G·A2399', '钱师傅', '13900000002', '动力煤（5500大卡）', '煤炭', 25, 28, 0, 'COMPLETED', '2026-10-28 08:40:00', '2026-10-28 10:05:00', 'system'),
  ('tr3', 't-fllt', 'TR202610280003', 'd4', 'port-fllt', 'b2', 'bc2', 'ca2', 'v4', '渝A·B6620', '周师傅', '13900000004', '20GP 标准箱（日用百货）', '集装箱', 28, 60, 2, 'IN_TRANSIT', '2026-10-28 09:50:00', NULL, 'system'),
  ('tr4', 't-fllt', 'TR202610280004', 'd6', 'port-fllt', 'b6', 'bc3', 'ca3', 'v9', '渝G·C3003', '褚师傅', '13900000009', '机制砂', '砂石建材', 25, 17, 0, 'IN_TRANSIT', '2026-10-28 10:15:00', NULL, 'system');

INSERT INTO `biz_gate_pass` (`id`, `tenant_id`, `pass_no`, `access_token`, `trip_id`, `carrier_id`, `carrier_name`, `vehicle_id`, `plate_no`, `vehicle_type`, `driver_name`, `driver_phone`, `port_name`, `berth_name`, `cargo_name`, `cargo_type_name`, `load_tonnage`, `load_volume`, `load_pieces`, `issue_time`, `status`, `verify_time`, `verify_by`, `create_by`) VALUES
  ('gp1', 't-fllt', 'GP20261028000001', 'demo0token0000000000000000000001', 'tr1', 'ca1', '涪陵顺达物流有限公司', 'v1', '渝G·A1688', 'SEMI_TRAILER', '赵师傅', '13900000001', '涪陵龙头港', '1号煤炭泊位', '动力煤（5500大卡）', '煤炭', 34, 37, 0, '2026-10-28 09:20:00', 'UNUSED', NULL, NULL, 'system'),
  ('gp2', 't-fllt', 'GP20261028000002', 'demo0token0000000000000000000002', 'tr2', 'ca1', '涪陵顺达物流有限公司', 'v2', '渝G·A2399', 'DUMP', '钱师傅', '13900000002', '涪陵龙头港', '1号煤炭泊位', '动力煤（5500大卡）', '煤炭', 25, 28, 0, '2026-10-28 08:40:00', 'VERIFIED', '2026-10-28 08:52:00', 'menwei', 'system'),
  ('gp3', 't-fllt', 'GP20261028000003', 'demo0token0000000000000000000003', 'tr3', 'ca2', '重庆渝运通运输有限公司', 'v4', '渝A·B6620', 'CONTAINER', '周师傅', '13900000004', '涪陵龙头港', '2号集装箱泊位', '20GP 标准箱（日用百货）', '集装箱', 28, 60, 2, '2026-10-28 09:50:00', 'UNUSED', NULL, NULL, 'system'),
  ('gp4', 't-fllt', 'GP20261028000004', 'demo0token0000000000000000000004', 'tr4', 'ca3', '长江联运车队', 'v9', '渝G·C3003', 'DUMP', '褚师傅', '13900000009', '涪陵龙头港', '6号砂石泊位', '机制砂', '砂石建材', 25, 17, 0, '2026-10-28 10:15:00', 'UNUSED', NULL, NULL, 'system');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 2026-10-28 追加：江河支持拖拽弯度（props.points 为归一化控制点，props.riverWidth 为河道宽度）
-- 演示数据：乌江改为弯曲河道，并与长江交汇
-- ============================================================
UPDATE `biz_port_element`
SET `props` = '{"label": "乌江", "riverWidth": 120, "points": [{"u": 0.5, "v": -0.12}, {"u": 0.4, "v": 0.3}, {"u": 0.62, "v": 0.66}, {"u": 0.5, "v": 1}]}'
WHERE `id` = 'pe003';

-- ============================================================
-- 2026-10-28 追加：港口改为 3D 数字孪生场景
-- 泊位模型自带岸桥，移除演示数据中独立的门机元素；乌江西岸新增 7 号泊位，演示河道两岸均可设泊位（旋转 90°，临水侧朝东）
-- ============================================================
DELETE FROM `biz_port_element`
WHERE `id` IN ('pe012', 'pe013', 'pe014', 'pe015', 'pe016', 'pe017', 'pe018', 'pe019');

INSERT INTO `biz_berth` (`id`, `tenant_id`, `port_id`, `berth_code`, `berth_name`, `status`, `work_start_time`, `work_end_time`, `berth_length`, `water_depth`, `max_tonnage`, `create_by`) VALUES
  ('b7', 't-fllt', 'port-fllt', 'B7', '7号乌江泊位', 'IDLE', '08:00:00', '18:00:00', 160, 5.5, 3000, 'system');

INSERT INTO `biz_port_element` (`id`, `tenant_id`, `port_id`, `element_type`, `pos_x`, `pos_y`, `width`, `height`, `rotation`, `layer_index`, `props`, `berth_id`, `create_by`) VALUES
  ('pe050', 't-fllt', 'port-fllt', 'BERTH', 1329, 520, 190, 86, 90, 40, '{}', 'b7', 'system');

-- ============================================================
-- 2026-10-28 追加：泊位装货（货物装船并分派给船只）、货物形态（集装箱/散货）、“船运公司管理”更名为“船只管理”
-- ============================================================
ALTER TABLE `biz_berth_cargo`
  ADD COLUMN `direction` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'UNLOAD' COMMENT '作业方向：UNLOAD-到货（卸船），LOAD-装货（装船）' AFTER `berth_id`,
  ADD COLUMN `pack_type` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'BULK' COMMENT '货物形态：CONTAINER-集装箱，BULK-散货' AFTER `cargo_name`;

UPDATE `biz_berth_cargo` SET `pack_type` = 'CONTAINER' WHERE `id` = 'bc2';

-- 装船分派
DROP TABLE IF EXISTS `biz_ship_load`;
CREATE TABLE `biz_ship_load` (
  `id` varchar(36) COLLATE utf8mb3_bin primary key NOT NULL,
  `tenant_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '租户ID',
  `port_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '港口ID',
  `berth_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位ID',
  `cargo_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '泊位货物ID（装货）',
  `ship_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船只ID',
  `company_id` varchar(36) COLLATE utf8mb3_bin NOT NULL COMMENT '船运公司ID',
  `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'WAITING' COMMENT '状态：WAITING-待装船，LOADED-已装船，CANCELLED-已取消',
  `load_tonnage` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装船吨数',
  `load_volume` decimal(14,3) NOT NULL DEFAULT 0 COMMENT '装船方数',
  `load_pieces` int NOT NULL DEFAULT 0 COMMENT '装船件数',
  `load_time` datetime DEFAULT NULL COMMENT '装船完成时间',
  `remark` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_by` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
  KEY `idx_sl_tenant_berth_status` (`tenant_id`, `berth_id`, `status`),
  KEY `idx_sl_tenant_cargo` (`tenant_id`, `cargo_id`),
  KEY `idx_sl_tenant_company_status` (`tenant_id`, `company_id`, `status`),
  KEY `idx_sl_ship` (`ship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='装船分派';

-- 演示数据：5号粮食泊位装货（玉米装船：渝江之星已装 450 吨，涪州粮运8号待装船）
INSERT INTO `biz_berth_cargo` (`id`, `tenant_id`, `port_id`, `berth_id`, `direction`, `cargo_type_id`, `cargo_type_name`, `cargo_name`, `pack_type`, `tonnage`, `volume`, `pieces`, `loaded_tonnage`, `loaded_volume`, `loaded_pieces`, `status`, `arrive_time`, `create_by`) VALUES
  ('bc4', 't-fllt', 'port-fllt', 'b5', 'LOAD', 'ct_grain', '粮食', '玉米（散粮）', 'BULK', 1200, 1600, 0, 450, 600, 0, 'IN_PROGRESS', '2026-10-28 09:00:00', 'system');

INSERT INTO `biz_ship_load` (`id`, `tenant_id`, `port_id`, `berth_id`, `cargo_id`, `ship_id`, `company_id`, `status`, `load_tonnage`, `load_volume`, `load_pieces`, `load_time`, `create_by`) VALUES
  ('sl1', 't-fllt', 'port-fllt', 'b5', 'bc4', 'sh3', 'sp2', 'LOADED', 450, 600, 0, '2026-10-28 10:30:00', 'system'),
  ('sl2', 't-fllt', 'port-fllt', 'b5', 'bc4', 'sh4', 'sp3', 'WAITING', 0, 0, 0, NULL, 'system');

UPDATE `biz_berth` SET `status` = 'WORKING' WHERE `id` = 'b5';

-- 菜单：船运公司管理更名为船只管理；新增“泊位装货”按钮，并授予已有“泊位到货”权限的角色
UPDATE `sys_menu` SET `menu_name` = '船只管理' WHERE `id` = 'm_shipper';
UPDATE `sys_menu` SET `menu_name` = '编辑船运公司/船只' WHERE `id` = 'm_shipper_edit';
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `create_by`) VALUES
  ('m_berth_load', 'm_berth', '泊位装货', 'BUTTON', NULL, NULL, 'berth:cargo:load', NULL, 5, 1, 'ENABLED', 'system');
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_by`)
SELECT REPLACE(`id`, 'm_berth_arrive', 'm_berth_load'), `tenant_id`, `role_id`, 'm_berth_load', 'system'
FROM `sys_role_menu` WHERE `menu_id` = 'm_berth_arrive' AND `deleted` = 0;

-- ============================================================
-- 2026-10-29 追加：船只可单独完成卸货作业（到货与船只关联增加卸货状态）
-- ============================================================
ALTER TABLE `biz_berth_cargo_ship`
  ADD COLUMN `status` varchar(16) COLLATE utf8mb3_bin NOT NULL DEFAULT 'WORKING' COMMENT '船只卸货状态：WORKING-卸货中，COMPLETED-已完成' AFTER `company_id`,
  ADD COLUMN `finish_time` datetime DEFAULT NULL COMMENT '卸货完成时间' AFTER `status`,
  ADD KEY `idx_bcs_tenant_ship_status` (`tenant_id`, `ship_id`, `status`);

-- 已完成的到货，其船只同步为已完成
UPDATE `biz_berth_cargo_ship` cs
INNER JOIN `biz_berth_cargo` c ON c.`id` = cs.`cargo_id`
SET cs.`status` = 'COMPLETED', cs.`finish_time` = c.`finish_time`
WHERE c.`status` = 'COMPLETED';

-- ============================================================
-- 2026-10-29 追加：港口俯视图新增“山体”元素（element_type = MOUNTAIN，props.peak 为山体高度，props.seed 为地貌种子）
-- ============================================================
ALTER TABLE `biz_port_element`
  MODIFY COLUMN `element_type` varchar(20) COLLATE utf8mb3_bin NOT NULL COMMENT '元素类型：RIVER/LAND/BERTH/ROAD/YARD/WAREHOUSE/CRANE/GATE/BUILDING/TANK/TREE/SHIP/LABEL/MOUNTAIN';

-- ============================================================
-- 2026-10-29 追加：大屏驾驶舱菜单（授予已有“工作台”权限的角色）
-- ============================================================
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `create_by`) VALUES
  ('m_cockpit', '0', '驾驶舱', 'MENU', '/cockpit', 'cockpit/index', 'cockpit:view', 'Monitor', 1, 1, 'ENABLED', 'system');
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_by`)
SELECT REPLACE(`id`, 'm_dashboard', 'm_cockpit'), `tenant_id`, `role_id`, 'm_cockpit', 'system'
FROM `sys_role_menu` WHERE `menu_id` = 'm_dashboard' AND `deleted` = 0;
