-- =====================================================
-- Flyway V1.1: 索引优化 & 结构同步
-- 创建人: 刘建国
-- 创建时间: 2026-05-12
-- 描述:
--   1. orders 表查询索引优化
--   2. 为后续 Flyway 迁移建立统一版本管理规范
-- =====================================================

-- =====================================================
-- 1. orders 表查询索引优化
-- =====================================================

-- 订单状态+逻辑删除联合索引，用于按状态筛选订单列表
-- 覆盖 OrderTimeoutTask、OrderServiceImpl 等高频查询
CREATE INDEX IF NOT EXISTS `idx_orders_status_deleted`
    ON `orders` (`order_status`, `deleted`);

-- 订单编号唯一索引，用于订单编号精确查找
CREATE INDEX IF NOT EXISTS `idx_orders_order_no`
    ON `orders` (`order_no`);

-- 用户ID+订单状态联合索引，用于查询用户特定状态的订单
-- 覆盖 CompanionServiceImpl.delete 中的活跃订单检查
CREATE INDEX IF NOT EXISTS `idx_orders_user_status`
    ON `orders` (`user_id`, `order_status`);

-- 支付状态索引，用于 OrderTimeoutTask 超时扫描
CREATE INDEX IF NOT EXISTS `idx_orders_payment_status`
    ON `orders` (`payment_status`);

-- 创建时间+状态索引，用于超时订单分页扫描
CREATE INDEX IF NOT EXISTS `idx_orders_created_status`
    ON `orders` (`created_at`, `order_status`);

-- =====================================================
-- 2. work_orders 表查询索引优化
-- =====================================================

-- 工单状态+升级级别索引，用于 WorkOrderEscalationTask
CREATE INDEX IF NOT EXISTS `idx_work_orders_status_escalation`
    ON `work_orders` (`status`, `escalation_level`, `deleted`);

-- 分配客服+状态索引，用于客服人员查看自己负责的工单
CREATE INDEX IF NOT EXISTS `idx_work_orders_assigned_cs`
    ON `work_orders` (`assigned_cs_user_id`, `status`);

-- =====================================================
-- 3. Flyway 版本管理规范说明
-- =====================================================
-- 后续所有数据库变更统一通过 Flyway 迁移文件管理:
--   命名规范: V{major}.{minor}__{description}.sql
--   示例: V1.2__add_customer_tags.sql
--   禁止直接修改 V1.0__init_schema.sql（基线文件）
--   所有 DDL 变更必须创建新的版本文件
--   每个版本文件独立可重复执行（使用 IF NOT EXISTS / IF EXISTS）