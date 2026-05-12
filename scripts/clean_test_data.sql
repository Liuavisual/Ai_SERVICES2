-- ============================================
-- 测试数据安全清除脚本
-- 项目: Delta AI Customer Service (三角洲行动陪玩俱乐部AI客服系统)
-- 数据库: delta_ai_customer_service
-- 版本: v1.0
-- 日期: 2026-05-11
-- 作者: 刘建国
-- 
-- 说明:
--   本脚本用于安全清除数据库中所有测试数据，保留完整表结构。
--   按照外键依赖顺序从子表到父表逐一清空，确保不违反参照完整性。
--   执行前请务必备份数据！
-- 
-- 执行方式:
--   mysql -u root -p delta_ai_customer_service < clean_test_data.sql
-- ============================================

-- 禁用外键检查，避免 TRUNCATE 报错
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第1批: 关联明细表（最底层子表，无其他表依赖）
-- ============================================
TRUNCATE TABLE `messages_archive`;
TRUNCATE TABLE `work_order_attachments`;
TRUNCATE TABLE `work_order_records`;
TRUNCATE TABLE `sys_role_permission`;
TRUNCATE TABLE `sys_user_role`;
TRUNCATE TABLE `operation_logs`;
TRUNCATE TABLE `cs_user_customer`;
TRUNCATE TABLE `club_level_prices`;
TRUNCATE TABLE `service_price_rule`;

-- ============================================
-- 第2批: 业务记录表（依赖底层明细表）
-- ============================================
TRUNCATE TABLE `messages`;
TRUNCATE TABLE `conversation_sessions`;
TRUNCATE TABLE `pending_messages`;
TRUNCATE TABLE `customer_order_record`;
TRUNCATE TABLE `customer_satisfaction`;
TRUNCATE TABLE `customer_warning_rule`;
TRUNCATE TABLE `quality_check_record`;
TRUNCATE TABLE `referral_record`;
TRUNCATE TABLE `companion_schedules`;
TRUNCATE TABLE `companion_training`;
TRUNCATE TABLE `companion_settlement`;
TRUNCATE TABLE `companion_notifications`;
TRUNCATE TABLE `companion_rating_summary`;
TRUNCATE TABLE `service_tracks`;
TRUNCATE TABLE `revenue_daily_report`;

-- ============================================
-- 第3批: 业务主数据表（依赖于业务记录）
-- ============================================
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `work_orders`;
TRUNCATE TABLE `campaign`;
TRUNCATE TABLE `activity_package`;

-- ============================================
-- 第4批: 业务实体表
-- ============================================
TRUNCATE TABLE `companions`;
TRUNCATE TABLE `companion_levels`;
TRUNCATE TABLE `customer_profile`;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `club_subscription`;

-- ============================================
-- 第5批: 配置主数据表
-- ============================================
TRUNCATE TABLE `club_config`;
TRUNCATE TABLE `game_config`;
TRUNCATE TABLE `game_knowledge`;
TRUNCATE TABLE `service_item`;
TRUNCATE TABLE `faq_items`;
TRUNCATE TABLE `keywords`;
TRUNCATE TABLE `replies`;
TRUNCATE TABLE `ai_personality_config`;
TRUNCATE TABLE `ai_config`;
TRUNCATE TABLE `platform_configs`;
TRUNCATE TABLE `pricing_plan`;

-- ============================================
-- 第6批: 系统管理基础表（最后清空）
-- ============================================
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `sys_permission`;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 执行完成后验证
-- ============================================
SELECT '========== 数据清除完成 ==========' AS result;
SELECT COUNT(*) AS `activity_package` FROM `activity_package`;
SELECT COUNT(*) AS `ai_config` FROM `ai_config`;
SELECT COUNT(*) AS `ai_personality_config` FROM `ai_personality_config`;
SELECT COUNT(*) AS `campaign` FROM `campaign`;
SELECT COUNT(*) AS `club_config` FROM `club_config`;
SELECT COUNT(*) AS `club_level_prices` FROM `club_level_prices`;
SELECT COUNT(*) AS `club_subscription` FROM `club_subscription`;
SELECT COUNT(*) AS `companion_levels` FROM `companion_levels`;
SELECT COUNT(*) AS `companion_notifications` FROM `companion_notifications`;
SELECT COUNT(*) AS `companion_rating_summary` FROM `companion_rating_summary`;
SELECT COUNT(*) AS `companion_schedules` FROM `companion_schedules`;
SELECT COUNT(*) AS `companion_settlement` FROM `companion_settlement`;
SELECT COUNT(*) AS `companion_training` FROM `companion_training`;
SELECT COUNT(*) AS `companions` FROM `companions`;
SELECT COUNT(*) AS `conversation_sessions` FROM `conversation_sessions`;
SELECT COUNT(*) AS `cs_user_customer` FROM `cs_user_customer`;
SELECT COUNT(*) AS `customer_order_record` FROM `customer_order_record`;
SELECT COUNT(*) AS `customer_profile` FROM `customer_profile`;
SELECT COUNT(*) AS `customer_satisfaction` FROM `customer_satisfaction`;
SELECT COUNT(*) AS `customer_warning_rule` FROM `customer_warning_rule`;
SELECT COUNT(*) AS `faq_items` FROM `faq_items`;
SELECT COUNT(*) AS `game_config` FROM `game_config`;
SELECT COUNT(*) AS `game_knowledge` FROM `game_knowledge`;
SELECT COUNT(*) AS `keywords` FROM `keywords`;
SELECT COUNT(*) AS `messages` FROM `messages`;
SELECT COUNT(*) AS `messages_archive` FROM `messages_archive`;
SELECT COUNT(*) AS `operation_logs` FROM `operation_logs`;
SELECT COUNT(*) AS `orders` FROM `orders`;
SELECT COUNT(*) AS `pending_messages` FROM `pending_messages`;
SELECT COUNT(*) AS `platform_configs` FROM `platform_configs`;
SELECT COUNT(*) AS `pricing_plan` FROM `pricing_plan`;
SELECT COUNT(*) AS `quality_check_record` FROM `quality_check_record`;
SELECT COUNT(*) AS `referral_record` FROM `referral_record`;
SELECT COUNT(*) AS `replies` FROM `replies`;
SELECT COUNT(*) AS `revenue_daily_report` FROM `revenue_daily_report`;
SELECT COUNT(*) AS `service_item` FROM `service_item`;
SELECT COUNT(*) AS `service_price_rule` FROM `service_price_rule`;
SELECT COUNT(*) AS `service_tracks` FROM `service_tracks`;
SELECT COUNT(*) AS `sys_permission` FROM `sys_permission`;
SELECT COUNT(*) AS `sys_role` FROM `sys_role`;
SELECT COUNT(*) AS `sys_role_permission` FROM `sys_role_permission`;
SELECT COUNT(*) AS `sys_user` FROM `sys_user`;
SELECT COUNT(*) AS `sys_user_role` FROM `sys_user_role`;
SELECT COUNT(*) AS `users` FROM `users`;
SELECT COUNT(*) AS `work_order_attachments` FROM `work_order_attachments`;
SELECT COUNT(*) AS `work_order_records` FROM `work_order_records`;
SELECT COUNT(*) AS `work_orders` FROM `work_orders`;