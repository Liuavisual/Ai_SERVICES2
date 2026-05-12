-- ============================================
-- 生产环境数据库初始化脚本
-- 项目: Delta AI Customer Service (三角洲行动陪玩俱乐部AI客服系统)
-- 数据库: delta_ai_customer_service
-- 版本: v2.0 (生产环境就绪)
-- 日期: 2026-05-11
-- 作者: 刘建国
-- 
-- 说明:
--   本脚本用于在新环境中一键初始化生产数据库，执行后可直接投入使用。
--   包含完整的表结构定义、索引、分区、初始配置数据和系统管理员账号。
-- 
-- 脚本内容:
--   1. 创建数据库和Flyway迁移历史表
--   2. 创建全部45张业务表（含审计后优化的索引和约束）
--   3. 初始化系统权限数据（74项权限）
--   4. 初始化系统角色（4个角色）
--   5. 初始化角色-权限关联（超级管理员拥有全部权限）
--   6. 创建系统管理员账号（admin / Admin@123456）
--   7. 初始化定价方案和客户预警规则
-- 
-- 管理员账号:
--   用户名: admin
--   密码: Admin@123456
--   角色: SYS_ADMIN（超级管理员，拥有完整系统权限）
--   BCrypt加密: 由Spring Security BCryptPasswordEncoder(强度10)生成
-- 
-- 执行方式:
--   mysql -u root -p < init_production.sql
-- 
-- 前置条件:
--   MySQL 5.7+ / 8.0+
--   InnoDB引擎支持
--   无同名数据库或已手动删除旧数据库
-- ============================================

-- ============================================
-- 第一部分: 创建数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `delta_ai_customer_service`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `delta_ai_customer_service`;

-- ============================================
-- 第二部分: Flyway迁移历史表
-- ============================================
CREATE TABLE IF NOT EXISTS `flyway_schema_history` (
    `installed_rank` INT NOT NULL,
    `version` VARCHAR(50) DEFAULT NULL,
    `description` VARCHAR(200) NOT NULL,
    `type` VARCHAR(20) NOT NULL,
    `script` VARCHAR(1000) NOT NULL,
    `checksum` INT DEFAULT NULL,
    `installed_by` VARCHAR(100) NOT NULL,
    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `execution_time` INT NOT NULL,
    `success` TINYINT(1) NOT NULL,
    PRIMARY KEY (`installed_rank`),
    INDEX `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 记录本次初始化脚本执行（使Flyway跳过V1.0迁移）
INSERT IGNORE INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
(1, '1.0', 'init schema', 'SQL', 'V1.0__init_schema.sql', 0, 'init_production', NOW(), 0, 1);

-- ============================================
-- 第三部分: 业务表结构定义（按模块分组）
-- ============================================

-- ==================== 系统管理模块 ====================

-- 系统用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(50) NOT NULL DEFAULT 'CS_STAFF' COMMENT '角色：SYS_ADMIN-系统管理员，CS_LEADER-客服负责人，CS_STAFF-普通客服',
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：PENDING-待审核，ACTIVE-正常，DISABLED-禁用',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `two_factor_enabled` TINYINT(1) DEFAULT '0' COMMENT '是否启用双因素认证：0-未启用，1-已启用',
    `two_factor_secret` VARCHAR(64) DEFAULT NULL COMMENT '双因素密钥（Base32编码）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_sys_user_username` (`username`) USING BTREE,
    KEY `idx_sys_user_role` (`role`) USING BTREE,
    KEY `idx_sys_user_status` (`status`) USING BTREE,
    KEY `idx_sys_user_role_status` (`role`,`status`) USING BTREE,
    KEY `idx_sys_user_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 系统角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
    `is_system` TINYINT DEFAULT '0' COMMENT '是否系统内置角色：1-是，0-否',
    `status` TINYINT DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sr_code` (`role_code`),
    KEY `idx_sr_status` (`status`),
    KEY `idx_sr_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 系统权限表
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限主键ID',
    `perm_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `perm_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `perm_group` VARCHAR(50) DEFAULT NULL COMMENT '权限分组',
    `action_type` VARCHAR(20) DEFAULT NULL COMMENT '操作类型',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '权限描述',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号',
    `status` TINYINT DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sp_code` (`perm_code`),
    KEY `idx_sp_group` (`perm_group`),
    KEY `idx_sp_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 角色-权限关联表
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `perm_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`,`perm_id`),
    KEY `idx_srp_role_id` (`role_id`),
    KEY `idx_srp_perm_id` (`perm_id`),
    KEY `idx_srp_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- 用户-角色关联表
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
    KEY `idx_sur_user_id` (`user_id`),
    KEY `idx_sur_role_id` (`role_id`),
    KEY `idx_sur_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- 操作日志表
CREATE TABLE `operation_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operation_time` DATETIME DEFAULT NULL COMMENT '操作时间',
    `operator` VARCHAR(100) DEFAULT NULL COMMENT '操作人',
    `operation_type` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    `operation_target` VARCHAR(200) DEFAULT NULL COMMENT '操作目标',
    `operation_content` TEXT COMMENT '操作内容',
    `operation_result` VARCHAR(50) DEFAULT NULL COMMENT '操作结果',
    `affected_rows` INT DEFAULT NULL COMMENT '影响行数',
    `duration_ms` INT DEFAULT NULL COMMENT '耗时（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `rollback_info` TEXT COMMENT '回滚信息',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_operation_logs_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ==================== 客户管理模块 ====================

-- 客户用户表
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '来源平台：wechat、test等',
    `platform_user_id` VARCHAR(255) DEFAULT NULL COMMENT '平台侧用户ID，如微信的openid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '客户昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `ai_enabled` TINYINT(1) DEFAULT '1' COMMENT '是否启用AI自动回复：1-AI回复，0-仅人工',
    `assigned_cs_user_id` BIGINT DEFAULT NULL COMMENT '分配的专属客服ID（sys_user表），NULL表示未分配',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_users_platform_user` (`platform`,`platform_user_id`) USING BTREE,
    KEY `idx_users_platform` (`platform`) USING BTREE,
    KEY `idx_users_created_at` (`created_at`) USING BTREE,
    KEY `idx_users_assigned_cs` (`assigned_cs_user_id`) USING BTREE,
    KEY `idx_users_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户用户表';

-- 客户画像表
CREATE TABLE `customer_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `rfm_recency_score` INT DEFAULT NULL COMMENT 'RFM最近消费得分',
    `rfm_frequency_score` INT DEFAULT NULL COMMENT 'RFM消费频率得分',
    `rfm_monetary_score` INT DEFAULT NULL COMMENT 'RFM消费金额得分',
    `rfm_total_score` INT DEFAULT NULL COMMENT 'RFM综合得分',
    `rfm_segment` VARCHAR(50) DEFAULT NULL COMMENT 'RFM分群标签：CHAMPION-冠军，LOYAL-忠实，POTENTIAL-潜力，NEW-新客，AT_RISK-风险，HIBERNATE-休眠，LOST-流失',
    `total_orders` INT DEFAULT '0' COMMENT '总订单数',
    `total_spent` DECIMAL(12,2) DEFAULT '0.00' COMMENT '总消费金额',
    `spending_level` VARCHAR(20) DEFAULT NULL COMMENT '消费水平（HIGH/MEDIUM/LOW）',
    `avg_order_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '平均订单金额',
    `max_order_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最大订单金额',
    `spending_trend` VARCHAR(50) DEFAULT NULL COMMENT '消费趋势：INCREASING-增长，STABLE-稳定，DECREASING-下降',
    `repurchase_rate` DECIMAL(4,2) DEFAULT NULL COMMENT '复购率',
    `estimated_ltv` DECIMAL(12,2) DEFAULT NULL COMMENT '预估客户终身价值',
    `avg_service_duration` DECIMAL(10,2) DEFAULT NULL COMMENT '平均服务时长',
    `last_order_at` DATETIME DEFAULT NULL COMMENT '最后下单时间',
    `favorite_companion_id` BIGINT DEFAULT NULL COMMENT '最喜欢的陪玩师ID',
    `favorite_game_type` VARCHAR(100) DEFAULT NULL COMMENT '最喜欢的游戏类型',
    `game_preferences` TEXT COMMENT '游戏偏好',
    `preferred_time_slot` VARCHAR(50) DEFAULT NULL COMMENT '偏好时段',
    `preferred_companion_level` VARCHAR(50) DEFAULT NULL COMMENT '偏好陪玩师等级',
    `preferred_order_type` VARCHAR(50) DEFAULT NULL COMMENT '偏好订单类型',
    `companion_diversity` INT DEFAULT '0' COMMENT '陪玩师多样性',
    `first_contact_at` DATETIME DEFAULT NULL COMMENT '首次接触时间',
    `last_active_at` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
    `active_days` INT DEFAULT '0' COMMENT '活跃天数',
    `total_messages` INT DEFAULT '0' COMMENT '总消息数',
    `ai_interaction_count` INT DEFAULT '0' COMMENT 'AI交互次数',
    `manual_interaction_count` INT DEFAULT '0' COMMENT '人工交互次数',
    `ai_ratio` DECIMAL(4,2) DEFAULT NULL COMMENT 'AI交互占比',
    `human_handoff_count` INT DEFAULT '0' COMMENT '转人工次数',
    `top_handoff_reason` VARCHAR(200) DEFAULT NULL COMMENT '最常见转人工原因',
    `emotion_trigger_count` INT DEFAULT '0' COMMENT '情绪触发次数',
    `order_intent_count` INT DEFAULT '0' COMMENT '下单意向次数',
    `satisfaction_score` DECIMAL(3,2) DEFAULT NULL COMMENT '满意度评分',
    `satisfaction_trend` VARCHAR(50) DEFAULT NULL COMMENT '满意度趋势',
    `complaint_count` INT DEFAULT '0' COMMENT '投诉次数',
    `refund_count` INT DEFAULT '0' COMMENT '退款次数',
    `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
    `lifecycle_stage` VARCHAR(50) DEFAULT NULL COMMENT '生命周期阶段：NEW-新客，ACTIVE-活跃，LOYAL-忠实，AT_RISK-流失风险，CHURNED-已流失',
    `member_level` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '会员等级：NORMAL-普通，BRONZE-铜牌，SILVER-银牌，GOLD-金牌，PLATINUM-白金，DIAMOND-钻石',
    `risk_level` VARCHAR(20) DEFAULT NULL COMMENT '风险等级：LOW-低，MEDIUM-中，HIGH-高',
    `churn_risk_score` DECIMAL(5,2) DEFAULT NULL COMMENT '流失风险评分',
    `primary_need_type` VARCHAR(50) DEFAULT NULL COMMENT '主要需求类型',
    `need_tags` VARCHAR(500) DEFAULT NULL COMMENT '需求标签',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `remark` TEXT COMMENT '备注',
    `assigned_cs_user_id` BIGINT DEFAULT NULL COMMENT '分配的客服用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_customer_profile_user_id` (`user_id`) USING BTREE,
    KEY `idx_customer_profile_stage_active` (`lifecycle_stage`,`last_active_at`),
    KEY `idx_customer_profile_rfm_risk` (`rfm_segment`,`risk_level`),
    KEY `idx_customer_profile_member_level` (`member_level`),
    KEY `idx_customer_profile_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户画像表';

-- 客户满意度表
CREATE TABLE `customer_satisfaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '客户ID',
    `related_order_id` BIGINT DEFAULT NULL COMMENT '关联服务追踪ID',
    `related_companion_id` BIGINT DEFAULT NULL COMMENT '陪玩师ID',
    `satisfaction_score` INT DEFAULT NULL COMMENT '评分：1-5',
    `comment` TEXT COMMENT '反馈内容',
    `feedback_tags` VARCHAR(500) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `is_anonymous` INT DEFAULT '0' COMMENT '是否匿名：0-否，1-是',
    `service_type` VARCHAR(100) DEFAULT NULL COMMENT '服务类型',
    `reply_content` TEXT COMMENT '回复内容',
    `replied_by` BIGINT DEFAULT NULL COMMENT '回复人ID',
    `replied_at` DATETIME DEFAULT NULL COMMENT '回复时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_cs_user_id` (`user_id`) USING BTREE,
    KEY `idx_cs_order_id` (`related_order_id`) USING BTREE,
    KEY `idx_cs_companion_id` (`related_companion_id`) USING BTREE,
    KEY `idx_cs_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户满意度表';

-- 客户订单记录表
CREATE TABLE `customer_order_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
    `companion_id` BIGINT DEFAULT NULL COMMENT '陪玩师ID',
    `record_type` VARCHAR(50) DEFAULT NULL COMMENT '记录类型',
    `order_type` VARCHAR(50) DEFAULT NULL COMMENT '订单类型',
    `order_time` DATETIME DEFAULT NULL COMMENT '下单时间',
    `duration_hours` DECIMAL(10,1) DEFAULT NULL COMMENT '服务时长',
    `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
    `game_type` VARCHAR(100) DEFAULT NULL COMMENT '游戏类型',
    `companion_level` VARCHAR(50) DEFAULT NULL COMMENT '陪玩师等级',
    `time_slot` VARCHAR(50) DEFAULT NULL COMMENT '时段',
    `rating` INT DEFAULT NULL COMMENT '评分',
    `review_content` TEXT COMMENT '评价内容',
    `status` VARCHAR(30) DEFAULT 'COMPLETED' COMMENT '状态',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `service_item_id` BIGINT DEFAULT NULL COMMENT '服务项目ID',
    `game_config_id` BIGINT DEFAULT NULL COMMENT '游戏配置ID',
    `activity_package_id` BIGINT DEFAULT NULL COMMENT '活动套餐ID',
    `guarantee_result` VARCHAR(50) DEFAULT NULL COMMENT '保障结果',
    `content` LONGTEXT COMMENT '内容',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_cor_customer_id` (`customer_id`) USING BTREE,
    KEY `idx_cor_user_id` (`user_id`) USING BTREE,
    KEY `idx_cor_order_id` (`order_id`) USING BTREE,
    KEY `idx_cor_companion_id` (`companion_id`) USING BTREE,
    KEY `idx_cor_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户订单记录表';

-- 客户生命周期预警规则表
CREATE TABLE `customer_warning_rule` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `monitor_stage` VARCHAR(50) NOT NULL COMMENT '监控阶段: AT_RISK/CHURNED',
    `trigger_condition` VARCHAR(50) NOT NULL COMMENT '触发条件',
    `threshold_value` INT DEFAULT 0 COMMENT '条件阈值',
    `action_type` VARCHAR(50) NOT NULL COMMENT '处理动作',
    `action_params` VARCHAR(500) DEFAULT NULL COMMENT '动作参数JSON',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    KEY `idx_cwr_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户生命周期预警规则表';

-- 客服-客户关联表
CREATE TABLE `cs_user_customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `cs_user_id` BIGINT NOT NULL COMMENT '客服用户ID',
    `user_id` BIGINT NOT NULL COMMENT '客户用户ID',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '平台',
    `customer_name` VARCHAR(100) DEFAULT NULL COMMENT '客户名称',
    `assigned_at` DATETIME DEFAULT NULL COMMENT '分配时间',
    `assign_type` VARCHAR(50) DEFAULT NULL COMMENT '分配类型：MANUAL-手动，SYSTEM-系统',
    `assigned_by` BIGINT DEFAULT NULL COMMENT '分配操作人ID',
    `status` VARCHAR(50) DEFAULT 'ACTIVE' COMMENT '关联状态：ACTIVE-活跃，INACTIVE-非活跃',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_cs_user_customer_cs` (`cs_user_id`,`user_id`),
    KEY `idx_cs_user_customer_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服-客户关联表';

-- ==================== 俱乐部配置模块 ====================

-- 俱乐部配置表
CREATE TABLE `club_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_name` VARCHAR(200) DEFAULT NULL COMMENT '俱乐部名称',
    `club_logo` VARCHAR(500) DEFAULT NULL COMMENT '俱乐部Logo URL',
    `main_games` VARCHAR(500) DEFAULT NULL COMMENT '主营游戏，多个游戏用逗号分隔',
    `service_slogan` VARCHAR(500) DEFAULT NULL COMMENT '服务口号/标语',
    `welcome_message` TEXT COMMENT '欢迎语，新客户关注时自动发送',
    `contact_info` VARCHAR(500) DEFAULT NULL COMMENT '联系方式',
    `price_level_two` DECIMAL(10,2) DEFAULT NULL COMMENT '二品陪玩师价格（元/小时）',
    `price_level_one` DECIMAL(10,2) DEFAULT NULL COMMENT '一品陪玩师价格（元/小时）',
    `price_top` DECIMAL(10,2) DEFAULT NULL COMMENT '顶尖陪玩师价格（元/小时）',
    `price_star` DECIMAL(10,2) DEFAULT NULL COMMENT '明星陪玩师价格（元/小时）',
    `club_features` TEXT COMMENT '俱乐部特色',
    `custom_level_names` VARCHAR(500) DEFAULT NULL COMMENT '自定义等级名称',
    `service_promise` TEXT COMMENT '服务承诺',
    `refund_policy` TEXT COMMENT '退款政策',
    `member_discount` DECIMAL(10,2) DEFAULT NULL COMMENT '会员折扣',
    `recharge_bonus` VARCHAR(200) DEFAULT NULL COMMENT '充值优惠说明',
    `custom_welcome_template` TEXT COMMENT '自定义欢迎语模板',
    `ai_personality` VARCHAR(200) DEFAULT NULL COMMENT 'AI人格配置',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_club_config_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='俱乐部配置表';

-- 俱乐部等级价格关联表
CREATE TABLE `club_level_prices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT NOT NULL COMMENT '关联的俱乐部配置ID',
    `level_id` BIGINT NOT NULL COMMENT '关联的陪玩师等级ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '该等级在该俱乐部下的价格（元/小时）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_clp_club_config_id` (`club_config_id`) USING BTREE,
    KEY `idx_clp_level_id` (`level_id`) USING BTREE,
    KEY `idx_clp_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='俱乐部等级价格关联表';

-- 俱乐部订阅表
CREATE TABLE `club_subscription` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT NOT NULL COMMENT '俱乐部配置ID',
    `plan_id` BIGINT NOT NULL COMMENT '定价方案ID',
    `status` VARCHAR(16) NOT NULL DEFAULT 'TRIAL' COMMENT '订阅状态：TRIAL-试用中，ACTIVE-生效中，EXPIRED-已过期，CANCELLED-已取消',
    `start_at` DATETIME DEFAULT NULL COMMENT '订阅开始时间',
    `expire_at` DATETIME DEFAULT NULL COMMENT '订阅到期时间',
    `trial_end_at` DATETIME DEFAULT NULL COMMENT '试用到期时间',
    `auto_renew` TINYINT(1) DEFAULT '0' COMMENT '是否自动续费',
    `paid_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '实付金额',
    `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '支付方式',
    `payment_transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '支付流水号',
    `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_cs_club_config_id` (`club_config_id`),
    KEY `idx_cs_status` (`status`),
    KEY `idx_cs_expire_at` (`expire_at`),
    KEY `idx_cs_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='俱乐部订阅表';

-- ==================== 陪玩师管理模块 ====================

-- 陪玩师表
CREATE TABLE `companions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联系统用户ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '关联俱乐部ID',
    `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `wechat` VARCHAR(100) DEFAULT NULL COMMENT '微信号',
    `level_id` BIGINT DEFAULT NULL COMMENT '等级ID',
    `level_name` VARCHAR(100) DEFAULT NULL COMMENT '等级名称（冗余字段）',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `game_type` VARCHAR(100) DEFAULT NULL COMMENT '游戏类型',
    `description` LONGTEXT COMMENT '个人简介',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格（元/小时）',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `service_tags` LONGTEXT COMMENT '服务标签',
    `supported_games` LONGTEXT COMMENT '支持游戏列表',
    `kd_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT 'K/D比率',
    `rank_level` VARCHAR(50) DEFAULT NULL COMMENT '段位等级',
    `voice_sample_url` VARCHAR(500) DEFAULT NULL COMMENT '语音样本URL',
    `rating_avg` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
    `order_count` INT DEFAULT '0' COMMENT '订单数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_companions_level_id` (`level_id`) USING BTREE,
    KEY `idx_companions_enabled` (`enabled`) USING BTREE,
    KEY `idx_companions_game_type` (`game_type`) USING BTREE,
    KEY `idx_companions_enabled_game_type` (`enabled`,`game_type`) USING BTREE,
    KEY `idx_companions_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师表';

-- 陪玩师等级表
CREATE TABLE `companion_levels` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name` VARCHAR(100) NOT NULL COMMENT '等级名称，如"二品"、"一品"、"顶尖"',
    `level_code` VARCHAR(50) NOT NULL COMMENT '等级编码，如 LEVEL_TWO、LEVEL_ONE、TOP',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号，数值越小等级越高',
    `base_price` DECIMAL(10,2) DEFAULT NULL COMMENT '该等级基础价格（元/小时）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '等级描述',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_companion_levels_enabled` (`enabled`) USING BTREE,
    KEY `idx_companion_levels_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师等级表';

-- 陪玩师通知消息表
CREATE TABLE `companion_notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '陪玩师ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型：NEW_ORDER-新订单，STATUS_CHANGE-状态变更',
    `title` VARCHAR(256) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `is_read` INT NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_cn_companion_id` (`companion_id`),
    KEY `idx_cn_is_read` (`is_read`),
    KEY `idx_cn_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师通知消息表';

-- 陪玩师综合评分汇总表
CREATE TABLE `companion_rating_summary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '陪玩师ID',
    `total_reviews` INT DEFAULT '0' COMMENT '评价总数',
    `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分(1.00-5.00)',
    `rating1_count` INT DEFAULT '0' COMMENT '1星评价数',
    `rating2_count` INT DEFAULT '0' COMMENT '2星评价数',
    `rating3_count` INT DEFAULT '0' COMMENT '3星评价数',
    `rating4_count` INT DEFAULT '0' COMMENT '4星评价数',
    `rating5_count` INT DEFAULT '0' COMMENT '5星评价数',
    `positive_tags` TEXT COMMENT '正面评价标签(逗号分隔)',
    `negative_tags` TEXT COMMENT '负面评价标签(逗号分隔)',
    `last_review_at` DATETIME DEFAULT NULL COMMENT '最近评价时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crs_companion_id` (`companion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师综合评分汇总表';

-- 陪玩师排班表
CREATE TABLE `companion_schedules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '关联的陪玩师ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `time_slot` VARCHAR(50) DEFAULT NULL COMMENT '时段标识，如"上午"、"下午"、"晚上"',
    `start_time` TIME DEFAULT NULL COMMENT '开始时间',
    `end_time` TIME DEFAULT NULL COMMENT '结束时间',
    `status` VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE-可预约，BOOKED-已预约，UNAVAILABLE-不可用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_companion_schedules_companion_id` (`companion_id`) USING BTREE,
    KEY `idx_companion_schedules_status` (`status`) USING BTREE,
    KEY `idx_companion_schedules_companion_start` (`companion_id`,`start_time`) USING BTREE,
    KEY `idx_schedules_companion_date` (`companion_id`,`schedule_date`),
    KEY `idx_schedules_date_status` (`schedule_date`,`status`),
    KEY `idx_schedules_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师排班表';

-- 陪玩师结算表
CREATE TABLE `companion_settlement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '陪玩师ID',
    `settlement_period` VARCHAR(64) NOT NULL COMMENT '结算周期（如2026-05-01至2026-05-31）',
    `total_orders` INT DEFAULT '0' COMMENT '接单总数',
    `total_revenue` DECIMAL(12,2) DEFAULT '0.00' COMMENT '订单总收入',
    `platform_fee` DECIMAL(12,2) DEFAULT '0.00' COMMENT '平台分成金额',
    `companion_income` DECIMAL(12,2) DEFAULT '0.00' COMMENT '陪玩师实得金额',
    `deduction_amount` DECIMAL(12,2) DEFAULT '0.00' COMMENT '扣款项（违规罚款等）',
    `deduction_reason` VARCHAR(512) DEFAULT NULL COMMENT '扣款原因',
    `settlement_status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '结算状态：PENDING-待结算，PROCESSING-结算中，COMPLETED-已结算',
    `settled_at` DATETIME DEFAULT NULL COMMENT '实际结算时间',
    `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '收款方式',
    `payment_account` VARCHAR(128) DEFAULT NULL COMMENT '收款账号',
    `confirm_status` VARCHAR(16) DEFAULT 'UNCONFIRMED' COMMENT '陪玩师确认状态：UNCONFIRMED-未确认，CONFIRMED-已确认，DISPUTED-有异议',
    `dispute_content` TEXT COMMENT '申诉内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_cst_companion_id` (`companion_id`),
    KEY `idx_cst_status` (`settlement_status`),
    KEY `idx_cst_period` (`settlement_period`),
    KEY `idx_cst_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师结算记录表';

-- 陪玩师培训表
CREATE TABLE `companion_training` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '陪玩师ID',
    `course_name` VARCHAR(128) NOT NULL COMMENT '培训课程名称',
    `course_type` VARCHAR(32) NOT NULL COMMENT '培训类型：SERVICE_STANDARD-服务规范，SCRIPT_TEMPLATE-话术模板，COMPLIANCE-合规培训，GAME_SKILL-游戏技能',
    `course_content` TEXT COMMENT '培训内容（文本/Markdown）',
    `training_status` VARCHAR(16) DEFAULT 'NOT_STARTED' COMMENT '培训状态：NOT_STARTED-未开始，IN_PROGRESS-进行中，COMPLETED-已完成',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始学习时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成学习时间',
    `exam_score` INT DEFAULT NULL COMMENT '考核得分(0-100)',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '培训备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_ct_companion_id` (`companion_id`),
    KEY `idx_ct_status` (`training_status`),
    KEY `idx_ct_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师培训记录表';

-- ==================== 订单工单模块 ====================

-- 订单表
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '下单客户ID',
    `companion_id` BIGINT DEFAULT NULL COMMENT '陪玩师ID',
    `companion_name` VARCHAR(100) DEFAULT NULL COMMENT '陪玩师名称',
    `service_type` VARCHAR(100) DEFAULT NULL COMMENT '服务类型',
    `price_rule_id` BIGINT DEFAULT NULL COMMENT '价格规则ID',
    `order_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待确认，CONFIRMED-已确认，IN_PROGRESS-进行中，COMPLETED-已完成，PENDING_REVIEW-待评价，CANCELLED-已取消，REFUNDED-已退款，ABNORMAL-异常，ARCHIVED-已归档',
    `scheduled_start` DATETIME DEFAULT NULL COMMENT '预约开始时间',
    `scheduled_end` DATETIME DEFAULT NULL COMMENT '预约结束时间',
    `actual_start` DATETIME DEFAULT NULL COMMENT '实际开始时间',
    `actual_end` DATETIME DEFAULT NULL COMMENT '实际结束时间',
    `duration_minutes` INT DEFAULT NULL COMMENT '服务时长（分钟）',
    `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '订单总金额',
    `paid_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    `payment_status` VARCHAR(50) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付，PARTIAL-部分支付，PAID-已支付',
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式',
    `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '支付流水号',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `work_order_id` BIGINT DEFAULT NULL COMMENT '关联工单ID',
    `remark` VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_orders_order_no` (`order_no`) USING BTREE,
    KEY `idx_orders_user_id` (`user_id`) USING BTREE,
    KEY `idx_orders_companion_id` (`companion_id`) USING BTREE,
    KEY `idx_orders_status_created` (`order_status`,`created_at`) USING BTREE,
    KEY `idx_orders_transaction_id` (`transaction_id`),
    KEY `idx_orders_status_payment_created` (`order_status`,`payment_status`,`created_at`),
    KEY `idx_orders_user_id_created` (`user_id`,`created_at` DESC),
    KEY `idx_orders_companion_status` (`companion_id`,`order_status`),
    KEY `idx_orders_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 工单表
CREATE TABLE `work_orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `source_id` BIGINT DEFAULT NULL COMMENT '来源ID',
    `order_type` VARCHAR(50) DEFAULT NULL COMMENT '工单类型：CONSULT-咨询，BOOKING-预约，COMPLAINT-投诉，REFUND-退款，SERVICE_TRACK-服务追踪，OTHER-其他',
    `priority` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：NORMAL-普通，URGENT-紧急，CRITICAL-特急',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '平台',
    `user_id` BIGINT DEFAULT NULL COMMENT '客户ID',
    `customer_name` VARCHAR(100) DEFAULT NULL COMMENT '客户姓名',
    `customer_contact` VARCHAR(100) DEFAULT NULL COMMENT '客户联系方式',
    `customer_level` VARCHAR(50) DEFAULT NULL COMMENT '客户等级',
    `service_type` VARCHAR(100) DEFAULT NULL COMMENT '服务类型',
    `service_status` VARCHAR(50) DEFAULT NULL COMMENT '服务状态：PRE_SERVICE-服务前，IN_SERVICE-服务中，POST_SERVICE-服务后',
    `problem_detail` LONGTEXT COMMENT '问题描述',
    `problem_category` VARCHAR(100) DEFAULT NULL COMMENT '问题分类',
    `trigger_keyword` VARCHAR(500) DEFAULT NULL COMMENT '触发关键词',
    `context_summary` LONGTEXT COMMENT '上下文摘要',
    `assigned_cs_user_id` BIGINT DEFAULT NULL COMMENT '分配的客服ID',
    `assigned_cs_name` VARCHAR(100) DEFAULT NULL COMMENT '分配的客服名称',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handler_name` VARCHAR(100) DEFAULT NULL COMMENT '处理人名称',
    `handle_result` TEXT COMMENT '处理结果',
    `status` VARCHAR(50) DEFAULT 'NEW' COMMENT '工单状态：NEW-新建，PROCESSING-处理中，PENDING_CONFIRM-待确认，COMPLETED-已完成，CLOSED-已关闭，CANCELLED-已取消',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `escalation_level` INT DEFAULT '0' COMMENT '升级级别',
    `reminder_count` INT DEFAULT '0' COMMENT '提醒次数',
    `resolved_at` DATETIME DEFAULT NULL COMMENT '解决时间',
    `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
    `related_order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `related_companion_id` BIGINT DEFAULT NULL COMMENT '关联陪玩师ID',
    `satisfaction_score` INT DEFAULT NULL COMMENT '满意度评分',
    `satisfaction_remark` VARCHAR(500) DEFAULT NULL COMMENT '满意度备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_work_orders_user_id` (`user_id`) USING BTREE,
    KEY `idx_work_orders_assigned_cs_user_id` (`assigned_cs_user_id`) USING BTREE,
    KEY `idx_work_orders_status_priority` (`status`,`priority`) USING BTREE,
    KEY `idx_work_orders_order_type` (`order_type`) USING BTREE,
    KEY `idx_work_orders_platform` (`platform`) USING BTREE,
    KEY `idx_wo_status_deleted_ctime` (`status`,`deleted`,`created_at`),
    KEY `idx_work_orders_status_created` (`status`,`created_at`),
    KEY `idx_work_orders_cs_status` (`assigned_cs_user_id`,`status`),
    KEY `idx_work_orders_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- 工单记录表
CREATE TABLE `work_order_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `work_order_id` BIGINT NOT NULL COMMENT '工单ID',
    `record_type` VARCHAR(50) DEFAULT NULL COMMENT '记录类型：STATUS_CHANGE-状态变更，HANDLE_RECORD-处理记录，INTERNAL_NOTE-内部备注，SYSTEM_LOG-系统日志',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `operator_role` VARCHAR(50) DEFAULT NULL COMMENT '操作人角色',
    `content` TEXT COMMENT '内容',
    `old_status` VARCHAR(50) DEFAULT NULL COMMENT '旧状态',
    `new_status` VARCHAR(50) DEFAULT NULL COMMENT '新状态',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_wor_work_order_id` (`work_order_id`) USING BTREE,
    KEY `idx_wor_operator_id` (`operator_id`) USING BTREE,
    KEY `idx_wor_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单记录表';

-- 工单附件表
CREATE TABLE `work_order_attachments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `work_order_id` BIGINT NOT NULL COMMENT '关联工单ID',
    `record_id` BIGINT DEFAULT NULL COMMENT '关联记录ID',
    `file_name` VARCHAR(500) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(1000) NOT NULL COMMENT '文件路径',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型：IMAGE-图片，VIDEO-视频，DOCUMENT-文档，AUDIO-音频',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `uploaded_by` BIGINT DEFAULT NULL COMMENT '上传人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_work_order_attachments_work_order_id` (`work_order_id`) USING BTREE,
    KEY `idx_woa_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单附件表';

-- ==================== 游戏服务配置模块 ====================

-- 游戏配置表
CREATE TABLE `game_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '俱乐部配置ID',
    `game_name` VARCHAR(200) NOT NULL COMMENT '游戏名称',
    `game_code` VARCHAR(100) DEFAULT NULL COMMENT '游戏编码',
    `game_icon` VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    `game_desc` VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    `custom_settings` TEXT COMMENT '自定义设置',
    `game_type` VARCHAR(50) DEFAULT 'FPS' COMMENT '游戏类型',
    `base_hourly_price` DECIMAL(10,2) DEFAULT NULL COMMENT '基础时价',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_game_config_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏配置表';

-- 游戏知识库表
CREATE TABLE `game_knowledge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `game_id` BIGINT DEFAULT NULL COMMENT '游戏ID',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
    `title` VARCHAR(500) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `source` VARCHAR(200) DEFAULT NULL COMMENT '来源',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `keywords` VARCHAR(500) DEFAULT NULL COMMENT '关键词',
    `reliability` VARCHAR(50) DEFAULT NULL COMMENT '可靠性评级',
    `version` VARCHAR(50) DEFAULT NULL COMMENT '版本',
    `effective_from` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `effective_to` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `view_count` INT DEFAULT '0' COMMENT '查看次数',
    `helpful_count` INT DEFAULT '0' COMMENT '有帮助次数',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_gk_game_id` (`game_id`) USING BTREE,
    KEY `idx_gk_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏知识库表';

-- 服务项目表
CREATE TABLE `service_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '俱乐部配置ID',
    `game_config_id` BIGINT DEFAULT NULL COMMENT '游戏配置ID',
    `service_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `service_code` VARCHAR(100) DEFAULT NULL COMMENT '项目编码',
    `service_desc` VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    `service_icon` VARCHAR(500) DEFAULT NULL COMMENT '图标',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类：ACCOMPANY-陪玩，PACKAGE-套餐，TEACHING-教学，SOCIAL-社交',
    `base_price` DECIMAL(10,2) DEFAULT NULL COMMENT '基础价格',
    `price_unit` VARCHAR(50) DEFAULT 'HOUR' COMMENT '价格单位：HOUR-小时，NIGHT-包夜，ORDER-按单',
    `min_duration` DECIMAL(10,2) DEFAULT NULL COMMENT '最短时长',
    `guarantee_text` VARCHAR(1000) DEFAULT NULL COMMENT '保障说明',
    `refund_policy` VARCHAR(1000) DEFAULT NULL COMMENT '退款政策',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_si_club_config_id` (`club_config_id`) USING BTREE,
    KEY `idx_si_game_config_id` (`game_config_id`) USING BTREE,
    KEY `idx_si_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- 服务价格规则表
CREATE TABLE `service_price_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `service_item_id` BIGINT NOT NULL COMMENT '服务项目ID',
    `companion_level_id` BIGINT NOT NULL COMMENT '陪玩师等级ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `price_unit` VARCHAR(50) DEFAULT 'HOUR' COMMENT '价格单位',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_spr_service_item_id` (`service_item_id`) USING BTREE,
    KEY `idx_spr_companion_level_id` (`companion_level_id`) USING BTREE,
    KEY `idx_spr_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务价格规则表';

-- 服务追踪表
CREATE TABLE `service_tracks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '客户ID',
    `track_type` VARCHAR(50) DEFAULT NULL COMMENT '追踪类型',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
    `track_status` VARCHAR(50) DEFAULT NULL COMMENT '状态：CONSULTING-咨询中，BOOKED-已预约，SERVICING-服务中，SERVICE_DONE-服务完成，CONFIRMED-已确认',
    `current_step` VARCHAR(50) DEFAULT NULL COMMENT '当前步骤',
    `track_data` VARCHAR(4000) DEFAULT NULL COMMENT '追踪数据（JSON格式）',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `duration_seconds` INT DEFAULT NULL COMMENT '持续时长（秒）',
    `result` VARCHAR(200) DEFAULT NULL COMMENT '结果',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_st_user_id` (`user_id`) USING BTREE,
    KEY `idx_st_related_id` (`related_id`) USING BTREE,
    KEY `idx_st_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务追踪表';

-- ==================== AI消息模块 ====================

-- AI配置表
CREATE TABLE `ai_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) DEFAULT NULL COMMENT '配置类型，用于分组管理',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_ai_config_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- AI人格配置表
CREATE TABLE `ai_personality_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '俱乐部配置ID',
    `personality_style` VARCHAR(50) DEFAULT NULL COMMENT '人格风格',
    `industry_style` VARCHAR(50) DEFAULT NULL COMMENT '行业风格',
    `game_type` VARCHAR(100) DEFAULT NULL COMMENT '游戏类型',
    `is_default` TINYINT(1) DEFAULT '0' COMMENT '是否默认',
    `emotion_intelligence_level` VARCHAR(30) DEFAULT 'BASIC' COMMENT '情绪智能等级',
    `sentiment_sensitivity` INT DEFAULT '5' COMMENT '情绪敏感度',
    `proactive_comfort` TINYINT(1) DEFAULT '0' COMMENT '是否主动安抚',
    `address_format` VARCHAR(50) DEFAULT NULL COMMENT '称呼格式',
    `self_address` VARCHAR(50) DEFAULT NULL COMMENT '自称方式',
    `emoji_usage` INT DEFAULT '3' COMMENT '表情符号使用频率',
    `slang_usage` INT DEFAULT '2' COMMENT '网络用语使用频率',
    `formality_level` INT DEFAULT '4' COMMENT '正式程度',
    `greeting_style` TEXT COMMENT '问候风格',
    `max_reply_length` INT DEFAULT '500' COMMENT '最大回复长度',
    `use_game_terminology` TINYINT(1) DEFAULT '0' COMMENT '是否使用游戏术语',
    `conversion_style` VARCHAR(30) DEFAULT 'SOFT' COMMENT '转化风格',
    `conversion_rate` DECIMAL(10,2) DEFAULT '0' COMMENT '转化率',
    `satisfaction_score` DECIMAL(10,2) DEFAULT '0' COMMENT '满意度得分',
    `total_conversations` BIGINT DEFAULT '0' COMMENT '总对话数',
    `enabled` INT DEFAULT '1' COMMENT '是否启用',
    `priority` INT DEFAULT '0' COMMENT '优先级',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_pc_club_config_id` (`club_config_id`) USING BTREE,
    KEY `idx_pc_game_type` (`game_type`) USING BTREE,
    KEY `idx_pc_personality_style` (`personality_style`) USING BTREE,
    KEY `idx_pc_enabled_priority` (`enabled`,`priority`) USING BTREE,
    KEY `idx_pc_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI人格配置表';

-- 会话表
CREATE TABLE `conversation_sessions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '平台',
    `status` VARCHAR(50) DEFAULT 'ACTIVE' COMMENT '状态',
    `ai_model` VARCHAR(100) DEFAULT NULL COMMENT 'AI模型',
    `message_count` INT DEFAULT '0' COMMENT '消息总数',
    `ai_message_count` INT DEFAULT '0' COMMENT 'AI消息数',
    `human_message_count` INT DEFAULT '0' COMMENT '人工消息数',
    `first_message_at` DATETIME DEFAULT NULL COMMENT '首条消息时间',
    `last_message_at` DATETIME DEFAULT NULL COMMENT '末条消息时间',
    `resolved` INT DEFAULT '0' COMMENT '是否已解决',
    `context_summary` TEXT COMMENT '上下文摘要',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_conv_user_id` (`user_id`) USING BTREE,
    KEY `idx_conv_last_message` (`last_message_at`) USING BTREE,
    KEY `idx_conv_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 消息表（按月分区）
CREATE TABLE `messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT DEFAULT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `direction` VARCHAR(10) NOT NULL COMMENT '消息方向：in-接收（客户发送），out-发出（系统/客服发送）',
    `content` LONGTEXT COMMENT '消息内容',
    `content_type` VARCHAR(50) DEFAULT 'text' COMMENT '内容类型：text-文本，image-图片等',
    `is_ai` TINYINT(1) DEFAULT '0' COMMENT '是否AI回复',
    `ai_model` VARCHAR(100) DEFAULT NULL COMMENT 'AI模型名称',
    `ai_token_count` INT DEFAULT NULL COMMENT 'AI消耗Token数',
    `ai_response_time_ms` INT DEFAULT NULL COMMENT 'AI响应时间（毫秒）',
    `keyword_triggered` TINYINT(1) DEFAULT '0' COMMENT '是否触发关键词',
    `triggered_keyword` VARCHAR(500) DEFAULT NULL COMMENT '触发的关键词',
    `cs_user_id` BIGINT DEFAULT NULL COMMENT '客服用户ID',
    `emotion_tag` VARCHAR(50) DEFAULT NULL COMMENT '情绪标签',
    `intent_tag` VARCHAR(50) DEFAULT NULL COMMENT '意图标签',
    `read_status` VARCHAR(20) DEFAULT 'unread' COMMENT '阅读状态',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`,`created_at`),
    KEY `idx_messages_created_at` (`created_at`),
    KEY `idx_messages_user_id_created` (`user_id`,`created_at` DESC),
    KEY `idx_messages_session_id` (`session_id`) USING BTREE,
    KEY `idx_messages_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表'
PARTITION BY RANGE (TO_DAYS(`created_at`))
(
    PARTITION p202510 VALUES LESS THAN (739921) ENGINE = InnoDB,
    PARTITION p202511 VALUES LESS THAN (739951) ENGINE = InnoDB,
    PARTITION p202512 VALUES LESS THAN (739982) ENGINE = InnoDB,
    PARTITION p202601 VALUES LESS THAN (740013) ENGINE = InnoDB,
    PARTITION p202602 VALUES LESS THAN (740041) ENGINE = InnoDB,
    PARTITION p202603 VALUES LESS THAN (740072) ENGINE = InnoDB,
    PARTITION p202604 VALUES LESS THAN (740102) ENGINE = InnoDB,
    PARTITION p202605 VALUES LESS THAN (740133) ENGINE = InnoDB,
    PARTITION p202606 VALUES LESS THAN (740163) ENGINE = InnoDB,
    PARTITION p202607 VALUES LESS THAN (740194) ENGINE = InnoDB,
    PARTITION p202608 VALUES LESS THAN (740225) ENGINE = InnoDB,
    PARTITION p202609 VALUES LESS THAN (740255) ENGINE = InnoDB,
    PARTITION p202610 VALUES LESS THAN (740286) ENGINE = InnoDB,
    PARTITION p202611 VALUES LESS THAN (740316) ENGINE = InnoDB,
    PARTITION p202612 VALUES LESS THAN (740347) ENGINE = InnoDB,
    PARTITION p_future VALUES LESS THAN MAXVALUE ENGINE = InnoDB
);

-- 消息归档表
CREATE TABLE `messages_archive` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息原始ID',
    `session_id` BIGINT DEFAULT NULL COMMENT '会话ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `direction` VARCHAR(10) DEFAULT NULL COMMENT '消息方向',
    `content` LONGTEXT COMMENT '消息内容',
    `content_type` VARCHAR(20) DEFAULT NULL COMMENT '内容类型',
    `is_ai` TINYINT(1) DEFAULT NULL COMMENT '是否AI回复',
    `ai_model` VARCHAR(50) DEFAULT NULL COMMENT 'AI模型',
    `ai_token_count` INT DEFAULT NULL COMMENT 'Token消耗',
    `ai_response_time_ms` INT DEFAULT NULL COMMENT '响应时间',
    `keyword_triggered` TINYINT(1) DEFAULT NULL COMMENT '是否触发关键词',
    `triggered_keyword` VARCHAR(100) DEFAULT NULL COMMENT '触发的关键词',
    `cs_user_id` BIGINT DEFAULT NULL COMMENT '客服用户ID',
    `emotion_tag` VARCHAR(50) DEFAULT NULL COMMENT '情绪标签',
    `intent_tag` VARCHAR(50) DEFAULT NULL COMMENT '意图标签',
    `read_status` VARCHAR(20) DEFAULT NULL COMMENT '阅读状态',
    `archived_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    `created_at` DATETIME NOT NULL COMMENT '原始创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '原始更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`,`archived_at`),
    KEY `idx_archive_user_id` (`user_id`),
    KEY `idx_archive_session_id` (`session_id`),
    KEY `idx_archive_created_at` (`created_at`),
    KEY `idx_archive_archived_at` (`archived_at`),
    KEY `idx_archive_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息归档表';

-- 待处理消息表
CREATE TABLE `pending_messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '客户ID',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '平台',
    `content` LONGTEXT COMMENT '消息内容',
    `content_type` VARCHAR(50) DEFAULT 'text' COMMENT '内容类型',
    `pending_reason` VARCHAR(200) DEFAULT NULL COMMENT '待处理原因',
    `priority` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：NORMAL-普通，URGENT-紧急，CRITICAL-特急',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    `status` VARCHAR(30) DEFAULT 'pending' COMMENT '处理状态',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `escalation_level` INT DEFAULT '0' COMMENT '升级级别',
    `assigned_cs_user_id` BIGINT DEFAULT NULL COMMENT '分配的客服ID',
    `reminder_count` INT DEFAULT '0' COMMENT '提醒次数',
    `keyword` VARCHAR(200) DEFAULT NULL COMMENT '触发关键词',
    `handled_by` BIGINT DEFAULT NULL COMMENT '处理人用户ID',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `remark` TEXT COMMENT '处理备注',
    `context_summary` TEXT COMMENT '上下文摘要',
    `auto_reply_keyword` VARCHAR(200) DEFAULT NULL COMMENT '自动回复关键词',
    `matched_intent` VARCHAR(100) DEFAULT NULL COMMENT '匹配的意图',
    `intent_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '意图置信度',
    `auto_reply_template` TEXT COMMENT '自动回复模板',
    `auto_reply_at` DATETIME DEFAULT NULL COMMENT '自动回复时间',
    `auto_reply_used` TINYINT DEFAULT '0' COMMENT '是否使用了自动回复',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_pm_status` (`status`),
    KEY `idx_pm_status_deadline` (`status`,`deadline`),
    KEY `idx_pm_status_deleted_deadline` (`status`,`deleted`,`deadline`),
    KEY `idx_pm_user_id` (`user_id`) USING BTREE,
    KEY `idx_pm_assigned_cs` (`assigned_cs_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待处理消息表';

-- 自动回复规则表
CREATE TABLE `replies` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trigger_type` VARCHAR(50) NOT NULL COMMENT '触发类型：keyword-关键词触发，welcome-新用户关注触发，default-默认回复',
    `trigger_key` VARCHAR(500) NOT NULL COMMENT '触发键，关键词触发时为具体关键词，欢迎触发时为"welcome"',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `enabled` TINYINT(1) DEFAULT '1' COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_replies_enabled` (`enabled`) USING BTREE,
    KEY `idx_replies_trigger_key` (`trigger_key`(191)) USING BTREE,
    KEY `idx_replies_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动回复规则表';

-- 关键词触发表
CREATE TABLE `keywords` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(500) NOT NULL COMMENT '关键词内容',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '关键词分类：TRANSFER-转人工，COMPLAINT-投诉，ORDER-下单，EMERGENCY-紧急',
    `match_type` VARCHAR(50) DEFAULT 'EXACT' COMMENT '匹配方式：EXACT-精确，FUZZY-模糊，REGEX-正则',
    `action_type` VARCHAR(50) DEFAULT NULL COMMENT '触发动作：REPLY-自动回复，TRANSFER-转人工，TAG-标记，ESCALATE-升级',
    `reply_id` BIGINT DEFAULT NULL COMMENT '关联的自动回复ID',
    `priority` INT DEFAULT '0' COMMENT '优先级，数值越大优先级越高',
    `enabled` TINYINT(1) DEFAULT '1' COMMENT '是否启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_keywords_reply_id` (`reply_id`) USING BTREE,
    KEY `idx_keywords_enabled` (`enabled`) USING BTREE,
    KEY `idx_keywords_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词触发表';

-- FAQ知识库表
CREATE TABLE `faq_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '问题分类，如"服务流程"、"价格说明"',
    `question` TEXT NOT NULL COMMENT '问题内容',
    `answer` TEXT NOT NULL COMMENT '答案内容',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号，数值越小越靠前',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_faq_items_category` (`category`) USING BTREE,
    KEY `idx_faq_items_enabled` (`enabled`) USING BTREE,
    KEY `idx_faq_items_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ知识库表';

-- ==================== 营销活动模块 ====================

-- 活动套餐表
CREATE TABLE `activity_package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '俱乐部配置ID',
    `game_config_id` BIGINT DEFAULT NULL COMMENT '游戏配置ID',
    `title` VARCHAR(500) NOT NULL COMMENT '套餐标题',
    `description` TEXT COMMENT '描述',
    `activity_type` VARCHAR(50) DEFAULT NULL COMMENT '活动类型',
    `service_item_ids` VARCHAR(1000) DEFAULT NULL COMMENT '包含的服务项目ID列表',
    `package_price` DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `start_time` DATETIME DEFAULT NULL COMMENT '活动开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '活动结束时间',
    `banner_url` VARCHAR(500) DEFAULT NULL COMMENT '横幅图片URL',
    `terms` TEXT COMMENT '条款说明',
    `sort_order` INT DEFAULT '0' COMMENT '排序序号',
    `enabled` INT NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_activity_package_club_config_id` (`club_config_id`) USING BTREE,
    KEY `idx_activity_package_enabled` (`enabled`) USING BTREE,
    KEY `idx_activity_package_game_config_id` (`game_config_id`) USING BTREE,
    KEY `idx_activity_package_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动套餐表';

-- 营销活动表
CREATE TABLE `campaign` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT DEFAULT NULL COMMENT '俱乐部配置ID',
    `campaign_name` VARCHAR(128) NOT NULL COMMENT '活动名称',
    `campaign_type` VARCHAR(16) NOT NULL COMMENT '活动类型：TRIAL-试用推广，REFERRAL-裂变拉新，HOLIDAY-节日营销，RECALL-复购唤醒，OTHER-其他',
    `description` TEXT COMMENT '活动描述',
    `start_at` DATETIME DEFAULT NULL COMMENT '活动开始时间',
    `end_at` DATETIME DEFAULT NULL COMMENT '活动结束时间',
    `target_new_users` INT DEFAULT '0' COMMENT '目标拉新人数',
    `actual_new_users` INT DEFAULT '0' COMMENT '实际拉新人数',
    `budget` DECIMAL(10,2) DEFAULT '0.00' COMMENT '活动预算（元）',
    `actual_cost` DECIMAL(10,2) DEFAULT '0.00' COMMENT '实际花费（元）',
    `reward_rules` TEXT COMMENT '奖励方案描述',
    `status` VARCHAR(16) DEFAULT 'DRAFT' COMMENT '活动状态：DRAFT-草稿，ACTIVE-进行中，PAUSED-已暂停，ENDED-已结束，CANCELLED-已取消',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_cam_club_config_id` (`club_config_id`),
    KEY `idx_cam_status` (`status`),
    KEY `idx_cam_start_at` (`start_at`),
    KEY `idx_cam_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';

-- 裂变推荐记录表
CREATE TABLE `referral_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `campaign_id` BIGINT DEFAULT NULL COMMENT '关联营销活动ID',
    `referrer_user_id` BIGINT NOT NULL COMMENT '推荐人用户ID（老客户）',
    `referee_user_id` BIGINT DEFAULT NULL COMMENT '被推荐人用户ID（新客户）',
    `referral_code` VARCHAR(32) DEFAULT NULL COMMENT '推荐码',
    `referral_time` DATETIME NOT NULL COMMENT '推荐时间',
    `conversion_status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '转化状态：PENDING-待注册，REGISTERED-已注册，TRIALING-试用中，SUBSCRIBED-已付费',
    `converted_at` DATETIME DEFAULT NULL COMMENT '转化时间（注册/付费时间）',
    `reward_type` VARCHAR(16) DEFAULT NULL COMMENT '推荐人奖励类型：MONTH_FREE-赠送月会员，CASH-现金奖励，POINTS-积分奖励',
    `reward_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '推荐人奖励金额（元）',
    `reward_status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '奖励发放状态：PENDING-待发放，ISSUED-已发放，CANCELLED-已取消',
    `reward_issued_at` DATETIME DEFAULT NULL COMMENT '奖励发放时间',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rr_referrer_id` (`referrer_user_id`),
    KEY `idx_rr_referee_id` (`referee_user_id`),
    KEY `idx_rr_status` (`conversion_status`),
    KEY `idx_rr_campaign_id` (`campaign_id`),
    KEY `idx_rr_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='裂变推荐记录表';

-- ==================== 财务与质量模块 ====================

-- 定价方案表
CREATE TABLE `pricing_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `plan_code` VARCHAR(32) NOT NULL COMMENT '方案编码：BASIC/PRO/ENTERPRISE',
    `plan_name` VARCHAR(64) NOT NULL COMMENT '方案名称：基础版/专业版/企业版',
    `monthly_price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '月费价格',
    `yearly_price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '年费价格（购买年费享折扣）',
    `per_call_price` DECIMAL(10,4) DEFAULT '0.0000' COMMENT '按次付费单价（元/次AI对话）',
    `daily_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '日会员价格（9.9元/日）',
    `max_companions` INT DEFAULT '5' COMMENT '陪玩师数量上限',
    `max_monthly_messages` INT DEFAULT '0' COMMENT '月消息量上限（0=无限制）',
    `max_personality_templates` INT DEFAULT '2' COMMENT 'AI人格模板数量上限',
    `emotion_intelligence_level` VARCHAR(16) DEFAULT 'BASIC' COMMENT '情绪智能等级：BASIC/ADVANCED/PREMIUM',
    `include_smart_dispatch` TINYINT(1) DEFAULT '0' COMMENT '是否包含智能派单',
    `include_full_quality_check` TINYINT(1) DEFAULT '0' COMMENT '是否包含全流程质检',
    `include_analytics` TINYINT(1) DEFAULT '0' COMMENT '是否包含数据分析',
    `include_brand_custom` TINYINT(1) DEFAULT '0' COMMENT '是否支持自定义品牌',
    `include_api_access` TINYINT(1) DEFAULT '0' COMMENT '是否支持API接入',
    `features` TEXT COMMENT '功能描述（Markdown格式）',
    `sort_order` INT DEFAULT '0' COMMENT '排序号',
    `status` TINYINT DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_pp_plan_code` (`plan_code`),
    KEY `idx_pp_status` (`status`),
    KEY `idx_pp_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定价方案表';

-- 营收日报表
CREATE TABLE `revenue_daily_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_config_id` BIGINT NOT NULL COMMENT '俱乐部配置ID',
    `report_date` DATE NOT NULL COMMENT '报表日期',
    `game_type` VARCHAR(64) DEFAULT NULL COMMENT '游戏类型（null=全游戏合计）',
    `total_orders` INT DEFAULT '0' COMMENT '订单总数',
    `completed_orders` INT DEFAULT '0' COMMENT '已完成订单数',
    `refund_orders` INT DEFAULT '0' COMMENT '退款订单数',
    `total_revenue` DECIMAL(14,2) DEFAULT '0.00' COMMENT '订单总收入',
    `platform_income` DECIMAL(14,2) DEFAULT '0.00' COMMENT '平台分成收入',
    `ai_conversations` INT DEFAULT '0' COMMENT 'AI会话总数',
    `ai_handle_rate` DECIMAL(5,2) DEFAULT '0.00' COMMENT 'AI处理率(%)',
    `avg_satisfaction` DECIMAL(3,2) DEFAULT '0.00' COMMENT '客户满意度均值',
    `new_customers` INT DEFAULT '0' COMMENT '新客户数',
    `repeat_customers` INT DEFAULT '0' COMMENT '老客户复购数',
    `active_companions` INT DEFAULT '0' COMMENT '活跃陪玩师数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rdr_club_config_id` (`club_config_id`),
    KEY `idx_rdr_report_date` (`report_date`),
    KEY `idx_rdr_game_type` (`game_type`),
    KEY `idx_rdr_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营收日报表';

-- 服务质量检测记录表
CREATE TABLE `quality_check_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `companion_id` BIGINT DEFAULT NULL COMMENT '关联陪玩师ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联客户ID',
    `check_time` DATETIME NOT NULL COMMENT '检测时间',
    `check_type` VARCHAR(16) NOT NULL COMMENT '检测类型：SERVICE-服务质量，CONTENT-内容合规，ATTITUDE-服务态度，SPEED-响应速度',
    `risk_level` VARCHAR(16) NOT NULL DEFAULT 'SAFE' COMMENT '风险等级：SAFE-安全，LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-严重违规',
    `score` INT DEFAULT '0' COMMENT '检测得分(1-100)',
    `violation_type` VARCHAR(32) DEFAULT NULL COMMENT '违规类型：SEXUAL-涉黄，GAMBLING-涉赌，CHEAT-外挂，ABUSE-辱骂，REPLACE-代打，OTHER-其他',
    `violation_summary` TEXT COMMENT '违规内容摘要',
    `evidence_url` VARCHAR(512) DEFAULT NULL COMMENT '证据截图/录音URL',
    `action_suggestion` VARCHAR(512) DEFAULT NULL COMMENT '处理建议',
    `handle_status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理，REVIEWED-已审核，RESOLVED-已处理，IGNORED-已忽略',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_remark` VARCHAR(512) DEFAULT NULL COMMENT '处理备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_qcr_order_id` (`order_id`),
    KEY `idx_qcr_companion_id` (`companion_id`),
    KEY `idx_qcr_check_time` (`check_time`),
    KEY `idx_qcr_risk_level` (`risk_level`),
    KEY `idx_qcr_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务质量检测记录表';

-- ==================== 平台配置模块 ====================

-- 平台配置表
CREATE TABLE `platform_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台标识：wechat-微信，wework-企微，kook-KOOK，yy-YY',
    `enabled` TINYINT(1) DEFAULT '1' COMMENT '是否启用该平台接入',
    `config` JSON DEFAULT NULL COMMENT '平台特定配置，JSON格式存储',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_platform_configs_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台配置表';

-- ============================================
-- 第四部分: 初始数据 - 系统权限（74项）
-- ============================================
INSERT IGNORE INTO `sys_permission` (`id`, `perm_code`, `perm_name`, `perm_group`, `action_type`, `description`, `sort_order`, `status`) VALUES
(1, 'permission:manage', '权限管理', 'permission', 'manage', '管理角色和权限分配', 1, 1),
(2, 'dashboard:view', '查看工作台', 'dashboard', 'view', '查看运营数据仪表盘', 2, 1),
(3, 'stats:view', '查看统计', 'stats', 'view', '查看统计数据', 3, 1),
(4, 'customer:view', '查看客户', 'customer', 'view', '查看客户名录', 4, 1),
(5, 'customer:edit', '编辑客户', 'customer', 'edit', '编辑客户信息', 5, 1),
(6, 'customer:assign', '分配客户', 'customer', 'manage', '将客户分配给客服', 6, 1),
(7, 'customer:export', '导出客户', 'customer', 'export', '导出客户数据', 7, 1),
(8, 'customer_profile:view', '查看客户画像', 'customer', 'view', '查看客户画像和消费记录', 8, 1),
(9, 'customer_profile:edit', '编辑客户画像', 'customer', 'edit', '编辑客户画像和消费记录', 9, 1),
(10, 'customer_satisfaction:view', '查看满意度', 'customer', 'view', '查看客户满意度评价', 10, 1),
(11, 'customer_satisfaction:edit', '管理满意度', 'customer', 'edit', '回复和管理满意度评价', 11, 1),
(12, 'customer_lifecycle:view', '查看生命周期', 'customer', 'view', '查看客户生命周期阶段', 12, 1),
(13, 'customer_lifecycle:edit', '管理生命周期', 'customer', 'edit', '管理客户生命周期阶段', 13, 1),
(14, 'companion:view', '查看陪玩师', 'companion', 'view', '查看陪玩师列表和详情', 14, 1),
(15, 'companion:edit', '编辑陪玩师', 'companion', 'edit', '新增/编辑陪玩师', 15, 1),
(16, 'companion:export', '导出陪玩师', 'companion', 'export', '导出陪玩师Excel', 16, 1),
(17, 'companion:import', '导入陪玩师', 'companion', 'import', '导入陪玩师Excel', 17, 1),
(18, 'companion:rating', '评分看板', 'companion', 'view', '查看陪玩师评分数据', 18, 1),
(19, 'companion_level:view', '查看等级', 'companion', 'view', '查看陪玩师等级', 19, 1),
(20, 'companion_level:edit', '编辑等级', 'companion', 'edit', '编辑陪玩师等级', 20, 1),
(21, 'companion_level:export', '导出等级', 'companion', 'export', '导出等级Excel', 21, 1),
(22, 'companion_level:import', '导入等级', 'companion', 'import', '导入等级Excel', 22, 1),
(23, 'companion_settlement:view', '查看结算', 'companion', 'view', '查看陪玩师结算', 23, 1),
(24, 'companion_settlement:edit', '编辑结算', 'companion', 'edit', '编辑陪玩师结算', 24, 1),
(25, 'companion_training:view', '查看培训', 'companion', 'view', '查看陪玩师培训', 25, 1),
(26, 'companion_training:edit', '编辑培训', 'companion', 'edit', '编辑陪玩师培训', 26, 1),
(27, 'schedule:view', '查看排班', 'schedule', 'view', '查看陪玩师排班', 27, 1),
(28, 'schedule:edit', '编辑排班', 'schedule', 'edit', '编辑陪玩师排班', 28, 1),
(29, 'schedule:export', '导出排班', 'schedule', 'export', '导出排班Excel', 29, 1),
(30, 'schedule:import', '导入排班', 'schedule', 'import', '导入排班Excel', 30, 1),
(31, 'order:view', '查看订单', 'order', 'view', '查看订单列表和详情', 31, 1),
(32, 'order:edit', '编辑订单', 'order', 'edit', '修改订单状态', 32, 1),
(33, 'order:review', '订单评价', 'order', 'edit', '提交订单评价', 33, 1),
(34, 'order_status_history:view', '订单历史', 'order', 'view', '查看订单状态变更历史', 34, 1),
(35, 'work_order:view', '查看工单', 'order', 'view', '查看客服工单', 35, 1),
(36, 'work_order:edit', '编辑工单', 'order', 'edit', '编辑客服工单', 36, 1),
(37, 'service_item:view', '查看服务项目', 'service', 'view', '查看服务项目', 37, 1),
(38, 'service_item:edit', '编辑服务项目', 'service', 'edit', '编辑服务项目', 38, 1),
(39, 'service_track:view', '服务追踪', 'service', 'view', '查看服务追踪记录', 39, 1),
(40, 'pricing_plan:view', '查看定价', 'service', 'view', '查看定价方案', 40, 1),
(41, 'pricing_plan:edit', '编辑定价', 'service', 'edit', '编辑定价方案', 41, 1),
(42, 'activity_package:view', '查看套餐', 'activity', 'view', '查看活动套餐', 42, 1),
(43, 'activity_package:edit', '编辑套餐', 'activity', 'edit', '编辑活动套餐', 43, 1),
(44, 'activity_package:export', '导出套餐', 'activity', 'export', '导出套餐Excel', 44, 1),
(45, 'activity_package:import', '导入套餐', 'activity', 'import', '导入套餐Excel', 45, 1),
(46, 'subscription:view', '查看订阅', 'subscription', 'view', '查看俱乐部订阅', 46, 1),
(47, 'subscription:edit', '编辑订阅', 'subscription', 'edit', '管理俱乐部订阅', 47, 1),
(48, 'campaign:view', '查看活动', 'campaign', 'view', '查看营销活动', 48, 1),
(49, 'campaign:edit', '编辑活动', 'campaign', 'edit', '管理营销活动', 49, 1),
(50, 'message:view', '查看消息', 'message', 'view', '查看消息记录', 50, 1),
(51, 'pending_message:view', '查看待办', 'message', 'view', '查看待办消息', 51, 1),
(52, 'pending_message:edit', '处理待办', 'message', 'edit', '认领和处理待办消息', 52, 1),
(53, 'pending_message:export', '导出待办', 'message', 'export', '导出待办消息', 53, 1),
(54, 'chat:view', '对话测试', 'message', 'view', '使用AI对话测试沙箱', 54, 1),
(55, 'referral:view', '查看推荐', 'referral', 'view', '查看裂变推荐记录', 55, 1),
(56, 'referral:edit', '编辑推荐', 'referral', 'edit', '管理裂变推荐', 56, 1),
(57, 'cs_assignment:view', '查看分配', 'assignment', 'view', '查看客服客户分配', 57, 1),
(58, 'cs_assignment:edit', '编辑分配', 'assignment', 'edit', '管理客服客户分配', 58, 1),
(59, 'platform:manage', '平台配置', 'config', 'manage', '管理外部平台接入参数', 59, 1),
(60, 'club_config:view', '查看俱乐部', 'config', 'view', '查看俱乐部品牌配置', 60, 1),
(61, 'club_config:edit', '编辑俱乐部', 'config', 'edit', '编辑俱乐部品牌配置', 61, 1),
(62, 'game_config:view', '查看游戏', 'config', 'view', '查看游戏配置', 62, 1),
(63, 'game_config:edit', '编辑游戏', 'config', 'edit', '编辑游戏配置', 63, 1),
(64, 'game_config:export', '导出游戏', 'config', 'export', '导出游戏配置', 64, 1),
(65, 'ai_config:view', '查看AI配置', 'config', 'view', '查看AI引擎参数', 65, 1),
(66, 'ai_config:edit', '编辑AI配置', 'config', 'edit', '编辑AI引擎参数', 66, 1),
(67, 'faq_item:view', '查看FAQ', 'config', 'view', '查看FAQ知识库', 67, 1),
(68, 'faq_item:edit', '编辑FAQ', 'config', 'edit', '编辑FAQ知识库', 68, 1),
(69, 'faq_item:import', '导入FAQ', 'config', 'import', '导入FAQ知识库', 69, 1),
(70, 'keyword:view', '查看关键词', 'config', 'view', '查看关键词规则', 70, 1),
(71, 'keyword:edit', '编辑关键词', 'config', 'edit', '编辑关键词规则', 71, 1),
(72, 'keyword:import', '导入关键词', 'config', 'import', '导入关键词Excel', 72, 1),
(73, 'reply:view', '查看话术', 'config', 'view', '查看回复话术模板', 73, 1),
(74, 'reply:edit', '编辑话术', 'config', 'edit', '编辑回复话术模板', 74, 1),
(75, 'reply:import', '导入话术', 'config', 'import', '导入话术Excel', 75, 1),
(76, 'sys_user:view', '查看用户', 'config', 'view', '查看系统用户', 76, 1),
(77, 'sys_user:edit', '编辑用户', 'config', 'edit', '管理系统用户', 77, 1),
(78, 'sys_user:audit', '审核用户', 'config', 'edit', '审核用户注册', 78, 1),
(79, 'sys_user:export', '导出用户', 'config', 'export', '导出用户Excel', 79, 1),
(80, 'quality_check:view', '查看质检', 'quality', 'view', '查看质检记录', 80, 1),
(81, 'quality_check:edit', '编辑质检', 'quality', 'edit', '编辑质检记录', 81, 1),
(82, 'revenue_report:view', '查看财报', 'revenue', 'view', '查看营收日报', 82, 1),
(83, 'revenue_report:edit', '编辑财报', 'revenue', 'edit', '编辑营收数据', 83, 1),
(84, 'cache:view', '查看缓存', 'cache', 'view', '查看缓存状态', 84, 1),
(85, 'cache:edit', '管理缓存', 'cache', 'edit', '清除和管理缓存', 85, 1),
(86, 'system:admin', '系统管理', 'system', 'manage', '最高权限，管理所有系统配置', 86, 1);

-- ============================================
-- 第五部分: 初始数据 - 系统角色
-- ============================================
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `is_system`, `status`, `sort_order`) VALUES
(1, 'SYS_ADMIN', '超级管理员', '拥有系统所有权限，可管理系统配置、用户、角色和权限', 1, 1, 1),
(2, 'CS_LEADER', '客服主管', '管理客服团队、查看所有数据和报表、管理陪玩师和订单', 1, 1, 2),
(3, 'CS_STAFF', '客服人员', '处理客户咨询、管理工单和消息、查看分配客户数据', 1, 1, 3),
(4, 'COMPANION', '陪玩师', '查看个人排班、订单和结算信息，参与培训', 1, 1, 4);

-- ============================================
-- 第六部分: 初始数据 - 角色-权限关联
-- SYS_ADMIN拥有全部86项权限
-- ============================================
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),
(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),
(1,31),(1,32),(1,33),(1,34),(1,35),(1,36),(1,37),(1,38),(1,39),(1,40),
(1,41),(1,42),(1,43),(1,44),(1,45),(1,46),(1,47),(1,48),(1,49),(1,50),
(1,51),(1,52),(1,53),(1,54),(1,55),(1,56),(1,57),(1,58),(1,59),(1,60),
(1,61),(1,62),(1,63),(1,64),(1,65),(1,66),(1,67),(1,68),(1,69),(1,70),
(1,71),(1,72),(1,73),(1,74),(1,75),(1,76),(1,77),(1,78),(1,79),(1,80),
(1,81),(1,82),(1,83),(1,84),(1,85),(1,86);

-- CS_LEADER: 除系统管理和权限管理外的所有权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(2,9),(2,10),
(2,11),(2,12),(2,13),(2,14),(2,15),(2,16),(2,17),(2,18),(2,19),(2,20),
(2,21),(2,22),(2,23),(2,24),(2,25),(2,26),(2,27),(2,28),(2,29),(2,30),
(2,31),(2,32),(2,33),(2,34),(2,35),(2,36),(2,37),(2,38),(2,39),(2,40),
(2,41),(2,42),(2,43),(2,44),(2,45),(2,46),(2,47),(2,48),(2,49),(2,50),
(2,51),(2,52),(2,53),(2,54),(2,55),(2,56),(2,57),(2,58),(2,59),(2,60),
(2,61),(2,62),(2,63),(2,64),(2,65),(2,66),(2,67),(2,68),(2,69),(2,70),
(2,71),(2,72),(2,73),(2,74),(2,75),(2,76),(2,77),(2,78),(2,79),(2,80),
(2,81),(2,82),(2,83),(2,84),(2,85);

-- CS_STAFF: 基本查看和操作权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(3,2),(3,3),
(3,4),(3,5),(3,8),(3,10),(3,12),
(3,14),(3,18),(3,19),
(3,31),(3,32),(3,33),(3,34),(3,35),(3,36),
(3,37),(3,39),(3,40),
(3,42),(3,46),(3,48),
(3,50),(3,51),(3,52),(3,54),
(3,55),(3,57),
(3,60),(3,62),(3,65),(3,67),(3,70),(3,73),
(3,76),(3,77),(3,80),
(3,82),(3,84);

-- COMPANION: 陪玩师个人相关权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(4,14),(4,18),(4,19),
(4,23),
(4,25),
(4,27),
(4,31),(4,34),
(4,37),(4,39),
(4,42),(4,46),
(4,50),(4,54),
(4,60),(4,62),(4,67),(4,73);

-- ============================================
-- 第七部分: 初始数据 - 系统管理员账号
-- ============================================
-- 密码: Admin@123456 (BCrypt加密，强度10)
-- 生成方式: Spring Security BCryptPasswordEncoder.encode("Admin@123456")
-- 如需更换密码，请在应用启动后使用系统的密码修改功能或以下Java代码生成新的哈希值:
--   new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("新密码")
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `created_by`, `two_factor_enabled`) VALUES
(1, 'admin', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '系统管理员', NULL, NULL, 'SYS_ADMIN', 'ACTIVE', NULL, 0);

-- ============================================
-- 第八部分: 初始数据 - 用户-角色关联
-- ============================================
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1);

-- ============================================
-- 第九部分: 初始数据 - 定价方案
-- ============================================
INSERT IGNORE INTO `pricing_plan` (`id`, `plan_code`, `plan_name`, `monthly_price`, `yearly_price`, `per_call_price`, `daily_price`, `max_companions`, `max_monthly_messages`, `max_personality_templates`, `emotion_intelligence_level`, `include_smart_dispatch`, `include_full_quality_check`, `include_analytics`, `include_brand_custom`, `include_api_access`, `features`, `sort_order`, `status`) VALUES
(1, 'BASIC', '基础版', 0.00, 0.00, 0.0010, 0.00, 5, 5000, 2, 'BASIC', 0, 0, 0, 0, 0, '- AI自动回复\n- 基础关键词触发\n- 5名陪玩师\n- 月消息量5000条', 1, 1),
(2, 'PRO', '专业版', 299.00, 2999.00, 0.0005, 9.90, 50, 50000, 10, 'ADVANCED', 1, 1, 1, 0, 0, '- 全部基础版功能\n- 高级情绪智能\n- 50名陪玩师\n- 智能派单\n- 质检功能\n- 数据分析\n- 10个AI人格模板', 2, 1),
(3, 'ENTERPRISE', '企业版', 999.00, 9999.00, 0.0002, 0.00, 200, 0, 50, 'PREMIUM', 1, 1, 1, 1, 1, '- 全部专业版功能\n- 顶级情绪智能\n- 无限陪玩师\n- 无限消息量\n- 自定义品牌\n- API接入\n- 50个AI人格模板\n- 专属技术支持', 3, 1);

-- ============================================
-- 第十部分: 初始数据 - 客户预警规则
-- ============================================
INSERT IGNORE INTO `customer_warning_rule` (`id`, `rule_name`, `monitor_stage`, `trigger_condition`, `threshold_value`, `action_type`, `action_params`, `enabled`, `priority`) VALUES
(1, '连续7天无活跃', 'AT_RISK', 'NO_ACTIVITY_DAYS', 7, 'NOTIFY_CS', '{"template":"customer_inactive_warning"}', 1, 10),
(2, '严重流失风险', 'CHURNED', 'NO_ACTIVITY_DAYS', 14, 'SEND_COUPON', '{"couponId":1001}', 1, 5),
(3, '收到负面评价', 'AT_RISK', 'NEGATIVE_FEEDBACK', 1, 'MARK_VIP', '{"priority":"high"}', 1, 8);

-- ============================================
-- 初始化完成
-- ============================================
SELECT '=================================================' AS '';
SELECT '    Delta AI Customer Service 数据库初始化完成!' AS '';
SELECT '=================================================' AS '';
SELECT '' AS '';
SELECT '系统管理员账号信息:' AS '';
SELECT '  用户名: admin' AS '';
SELECT '  密码: Admin@123456' AS '';
SELECT '  角色: SYS_ADMIN (超级管理员)' AS '';
SELECT '  权限: 全部86项系统权限' AS '';
SELECT '' AS '';
SELECT '初始化内容统计:' AS '';
SELECT '  数据库表: 45张' AS '';
SELECT '  系统权限: 86项' AS '';
SELECT '  系统角色: 4个' AS '';
SELECT '  角色-权限关联: SYS_ADMIN(86项), CS_LEADER(85项), CS_STAFF(39项), COMPANION(17项)' AS '';
SELECT '  定价方案: 3个 (基础版/专业版/企业版)' AS '';
SELECT '  客户预警规则: 3条' AS '';
SELECT '  系统管理员: 1个 (admin)' AS '';
SELECT '=================================================' AS '';