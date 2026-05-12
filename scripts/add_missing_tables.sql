-- ============================================
-- 缺失表 DDL 脚本
-- 项目: Delta AI Customer Service (三角洲行动陪玩俱乐部AI客服系统)
-- 数据库: delta_ai_customer_service
-- 日期: 2026-05-12
-- 作者: 刘建国
-- 
-- 说明:
--   本脚本用于补充当前运行数据库中缺失的表，
--   可直接在 Navicat 中执行，仅新建表，不修改已有表。
-- 
-- 执行方式:
--   1. 在 Navicat 打开 delta_ai_customer_service 数据库
--   2. 打开本文件并执行
-- ============================================

USE `delta_ai_customer_service`;

-- ============================================
-- 1. 订单状态变更历史表
-- 对应实体: OrderStatusHistory.java
-- 使用场景: 订单创建/接单/拒单/取消时记录状态变更历史
-- ============================================
CREATE TABLE IF NOT EXISTS `order_status_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `from_status` VARCHAR(50) DEFAULT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(50) NOT NULL COMMENT '变更后状态',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    `operator_role` VARCHAR(50) DEFAULT NULL COMMENT '操作人角色',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_osh_order_id` (`order_id`),
    KEY `idx_osh_operator_id` (`operator_id`),
    KEY `idx_osh_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更历史表';

-- ============================================
-- 2. 陪玩师-游戏关联表
-- 对应实体: CompanionGame.java
-- 使用场景: 陪玩师可服务多款游戏，记录每款游戏的熟练度和段位
-- ============================================
CREATE TABLE IF NOT EXISTS `companion_game` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `companion_id` BIGINT NOT NULL COMMENT '陪玩师ID',
    `game_code` VARCHAR(100) NOT NULL COMMENT '游戏编码（如delta_force、league_of_legends）',
    `proficiency` INT DEFAULT NULL COMMENT '该游戏的熟练度等级(1-5)',
    `rank_level` VARCHAR(50) DEFAULT NULL COMMENT '该游戏的段位排名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_cg_companion_game` (`companion_id`,`game_code`),
    KEY `idx_cg_companion_id` (`companion_id`),
    KEY `idx_cg_game_code` (`game_code`),
    KEY `idx_cg_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩师-游戏关联表';

-- ============================================
-- 3. 系统操作审计日志表
-- 对应实体: SysOperationLog.java
-- 使用场景: 记录管理员/客服人员的敏感操作，用于审计追溯
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模块',
    `action` VARCHAR(100) DEFAULT NULL COMMENT '操作类型',
    `target_type` VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id` BIGINT DEFAULT NULL COMMENT '操作对象ID',
    `content` TEXT COMMENT '操作内容描述',
    `before_data` TEXT COMMENT '变更前数据(JSON)',
    `after_data` TEXT COMMENT '变更后数据(JSON)',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `operate_time` DATETIME DEFAULT NULL COMMENT '操作时间',
    `status` INT DEFAULT '1' COMMENT '操作状态：1-成功，0-失败',
    `error_msg` VARCHAR(2000) DEFAULT NULL COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_sol_operator` (`operator_id`),
    KEY `idx_sol_module` (`module`),
    KEY `idx_sol_operate_time` (`operate_time`),
    KEY `idx_sol_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作审计日志表';

-- ============================================
-- 4. 工单SLA追踪表
-- 对应实体: WorkOrderSla.java
-- 使用场景: 工单SLA超时预警和追踪
-- ============================================
CREATE TABLE IF NOT EXISTS `work_order_sla` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `work_order_id` BIGINT NOT NULL COMMENT '关联工单ID',
    `priority_level` VARCHAR(20) NOT NULL COMMENT '优先级：URGENT(15分钟) / HIGH(30分钟) / NORMAL(60分钟) / LOW(120分钟)',
    `deadline_time` DATETIME NOT NULL COMMENT 'SLA截止时间',
    `warn_time` DATETIME DEFAULT NULL COMMENT '预警时间（截止前N分钟预警）',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-等待中 / WARNED-已预警 / BREACHED-已超时 / COMPLETED-已完成',
    `alert_sent` INT DEFAULT '0' COMMENT '是否已发送告警通知：0-未发送，1-已发送',
    `breach_minutes` INT DEFAULT NULL COMMENT '超时时长（分钟，实际处理时间 - 截止时间）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_wsla_work_order_id` (`work_order_id`),
    KEY `idx_wsla_status` (`status`),
    KEY `idx_wsla_deadline` (`deadline_time`),
    KEY `idx_wsla_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单SLA追踪表';

-- ============================================
-- 5. 快捷回复使用统计表
-- 对应实体: ReplyUsageStat.java
-- 使用场景: 快捷回复使用频次统计
-- ============================================
CREATE TABLE IF NOT EXISTS `reply_usage_stat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reply_id` BIGINT NOT NULL COMMENT '快捷回复ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `use_count` INT NOT NULL DEFAULT '0' COMMENT '使用次数',
    `conversion_count` INT NOT NULL DEFAULT '0' COMMENT '转化次数（使用后用户下单）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_rus_reply_date` (`reply_id`,`stat_date`),
    KEY `idx_rus_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷回复使用统计表';

-- ============================================
-- 6. 活动礼包核销记录表
-- 对应实体: ActivityPackageUsage.java
-- 使用场景: 活动礼包领取/核销记录
-- ============================================
CREATE TABLE IF NOT EXISTS `activity_package_usage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `package_id` BIGINT NOT NULL COMMENT '活动礼包ID',
    `user_id` BIGINT NOT NULL COMMENT '核销用户ID',
    `claim_time` DATETIME NOT NULL COMMENT '领取时间',
    `is_converted` INT NOT NULL DEFAULT '0' COMMENT '是否已核销：0-未核销，1-已核销',
    `converted_time` DATETIME DEFAULT NULL COMMENT '核销时间',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_apu_package_id` (`package_id`),
    KEY `idx_apu_user_id` (`user_id`),
    KEY `idx_apu_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动礼包核销记录表';

-- ============================================
-- 执行完成验证
-- ============================================
SELECT '=================================================' AS '';
SELECT '    缺失表补充完成!' AS '';
SELECT '=================================================' AS '';
SELECT '' AS '';
SELECT '已新建以下6张表:' AS '';
SELECT '  1. order_status_history  (订单状态变更历史)' AS '';
SELECT '  2. companion_game        (陪玩师-游戏关联)' AS '';
SELECT '  3. sys_operation_log      (系统操作审计日志)' AS '';
SELECT '  4. work_order_sla         (工单SLA追踪)' AS '';
SELECT '  5. reply_usage_stat       (快捷回复使用统计)' AS '';
SELECT '  6. activity_package_usage (活动礼包核销记录)' AS '';
SELECT '' AS '';
SELECT '验证表是否存在:' AS '';
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'delta_ai_customer_service'
  AND TABLE_NAME IN ('order_status_history','companion_game','sys_operation_log','work_order_sla','reply_usage_stat','activity_package_usage')
ORDER BY TABLE_NAME;
SELECT '=================================================' AS '';