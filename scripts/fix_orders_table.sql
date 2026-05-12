-- ============================================
-- orders 表缺失列修复
-- 项目: Delta AI Customer Service
-- 数据库: delta_ai_customer_service
-- 日期: 2026-05-12
-- 作者: 刘建国
-- 
-- 问题:
--   Order.java 实体包含 gameType、timeSource、cancelReason、scheduleId 字段，
--   但 orders 表缺少对应的 game_type、time_source、cancel_reason、schedule_id 列。
--   这会导致 MyBatis-Plus INSERT/SELECT 时出现 "Unknown column" 错误。
-- 
-- 执行方式: 在 Navicat 打开 delta_ai_customer_service 数据库后执行本文件
-- ============================================

USE `delta_ai_customer_service`;

ALTER TABLE `orders`
    ADD COLUMN `game_type` VARCHAR(100) DEFAULT NULL COMMENT '游戏类型' AFTER `service_type`,
    ADD COLUMN `time_source` VARCHAR(50) DEFAULT NULL COMMENT '时间选择方式：SYSTEM-系统推荐，CUSTOM-客户自定义' AFTER `remark`,
    ADD COLUMN `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消/拒单原因' AFTER `time_source`,
    ADD COLUMN `schedule_id` BIGINT DEFAULT NULL COMMENT '关联排班记录ID' AFTER `cancel_reason`;

-- 添加索引
ALTER TABLE `orders`
    ADD KEY `idx_orders_game_type` (`game_type`) USING BTREE,
    ADD KEY `idx_orders_schedule_id` (`schedule_id`) USING BTREE;

-- 验证
SELECT '=================================================' AS '';
SELECT '    orders 表列修复完成!' AS '';
SELECT '=================================================' AS '';
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'delta_ai_customer_service'
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME IN ('game_type', 'time_source', 'cancel_reason', 'schedule_id')
ORDER BY ORDINAL_POSITION;