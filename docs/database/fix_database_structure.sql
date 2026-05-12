-- ============================================================================
-- Delta AI 客服系统 — 数据库结构修复脚本
-- 作者：刘建国 | 日期：2026-05-12 | 版本：v1.0
-- 说明：本脚本基于Entity类与SQL表结构的对比结果生成
-- 使用方式：直接在Navicat中执行即可，所有语句均使用IF NOT EXISTS/IF EXISTS保证幂等性
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 一、缺失表修复（Entity类存在但SQL中无对应表）
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1.1 companion_games 表 — Entity: CompanionGame.java
-- 错误原因：陪玩师支持游戏关联表在SQL中完全缺失
-- 业务影响：CompanionGame实体无法持久化，陪玩师多游戏关联功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `companion_games`;
CREATE TABLE `companion_games` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏类型',
  `proficiency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '熟练度：BEGINNER-入门，INTERMEDIATE-中级，ADVANCED-高级，EXPERT-专家',
  `kd_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '该游戏K/D比率',
  `rank_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '该游戏段位',
  `is_primary` tinyint(1) NULL DEFAULT 0 COMMENT '是否为主游戏：0-否，1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cg_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_cg_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_cg_companion_game`(`companion_id` ASC, `game_type` ASC) USING BTREE,
  INDEX `idx_cg_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师游戏关联表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.2 companion_trainings 表 — Entity: CompanionTraining.java
-- 错误原因：陪玩师培训记录表在SQL中完全缺失
-- 业务影响：CompanionTraining实体无法持久化，陪玩师培训管理功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `companion_trainings`;
CREATE TABLE `companion_trainings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `training_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '培训类型：ONBOARDING-入职培训，SKILL-技能培训，COMPLIANCE-合规培训',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '培训标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '培训描述',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待完成，IN_PROGRESS-进行中，COMPLETED-已完成，FAILED-未通过',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '培训评分',
  `trainer_id` bigint NULL DEFAULT NULL COMMENT '培训师ID',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ct_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_ct_status`(`status` ASC) USING BTREE,
  INDEX `idx_ct_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师培训记录表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.3 customer_warning_rules 表 — Entity: CustomerWarningRule.java
-- 错误原因：客户预警规则表在SQL中完全缺失
-- 业务影响：CustomerWarningRule实体无法持久化，客户预警规则配置功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `customer_warning_rules`;
CREATE TABLE `customer_warning_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则类型：CHURN_RISK-流失风险，COMPLAINT-投诉，PAYMENT-支付异常，BEHAVIOR-行为异常',
  `condition_config` json NULL COMMENT '触发条件配置（JSON格式）',
  `action_config` json NULL COMMENT '触发后动作配置（JSON格式）',
  `priority` int NULL DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '规则描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cwr_rule_type`(`rule_type` ASC) USING BTREE,
  INDEX `idx_cwr_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_cwr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户预警规则表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.4 service_price_rules 表 — Entity: ServicePriceRule.java
-- 错误原因：服务定价规则表在SQL中完全缺失
-- 业务影响：ServicePriceRule实体无法持久化，灵活定价规则功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `service_price_rules`;
CREATE TABLE `service_price_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `service_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型',
  `companion_level_id` bigint NULL DEFAULT NULL COMMENT '陪玩师等级ID',
  `base_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '基础价格',
  `peak_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '高峰时段价格',
  `off_peak_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '低谷时段价格',
  `peak_hours` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '高峰时段定义（JSON格式）',
  `discount_rate` decimal(4, 2) NULL DEFAULT NULL COMMENT '折扣率',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `effective_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间',
  `effective_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '规则描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_spr_service_type`(`service_type` ASC) USING BTREE,
  INDEX `idx_spr_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_spr_level_id`(`companion_level_id` ASC) USING BTREE,
  INDEX `idx_spr_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_spr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务定价规则表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.5 reply_usage_stats 表 — Entity: ReplyUsageStat.java
-- 错误原因：回复使用统计表在SQL中完全缺失
-- 业务影响：ReplyUsageStat实体无法持久化，回复模板使用统计功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `reply_usage_stats`;
CREATE TABLE `reply_usage_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reply_id` bigint NOT NULL COMMENT '回复模板ID',
  `usage_date` date NOT NULL COMMENT '使用日期',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `positive_feedback_count` int NULL DEFAULT 0 COMMENT '正面反馈次数',
  `negative_feedback_count` int NULL DEFAULT 0 COMMENT '负面反馈次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rus_reply_date`(`reply_id` ASC, `usage_date` ASC) USING BTREE,
  INDEX `idx_rus_usage_date`(`usage_date` ASC) USING BTREE,
  INDEX `idx_rus_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回复使用统计表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.6 work_order_slas 表 — Entity: WorkOrderSla.java
-- 错误原因：工单SLA配置表在SQL中完全缺失
-- 业务影响：WorkOrderSla实体无法持久化，工单SLA管理功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `work_order_slas`;
CREATE TABLE `work_order_slas` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单类型',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优先级',
  `first_response_minutes` int NULL DEFAULT NULL COMMENT '首次响应时限（分钟）',
  `resolution_minutes` int NULL DEFAULT NULL COMMENT '解决时限（分钟）',
  `escalation_minutes` int NULL DEFAULT NULL COMMENT '升级时限（分钟）',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wos_type_priority`(`work_order_type` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_wos_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单SLA配置表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.7 campaigns 表 — Entity: Campaign.java
-- 错误原因：营销活动表在SQL中完全缺失
-- 业务影响：Campaign实体无法持久化，营销活动管理功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `campaigns`;
CREATE TABLE `campaigns` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campaign_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `campaign_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动类型：DISCOUNT-折扣，GIFT-赠品，TRIAL-体验',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `discount_rate` decimal(4, 2) NULL DEFAULT NULL COMMENT '折扣率',
  `gift_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '赠品描述',
  `target_segment` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标客群',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，ACTIVE-进行中，PAUSED-暂停，COMPLETED-已完成',
  `budget` decimal(12, 2) NULL DEFAULT NULL COMMENT '活动预算',
  `used_budget` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '已使用预算',
  `total_reach` int NULL DEFAULT 0 COMMENT '触达人数',
  `total_conversion` int NULL DEFAULT 0 COMMENT '转化人数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_campaigns_status`(`status` ASC) USING BTREE,
  INDEX `idx_campaigns_type`(`campaign_type` ASC) USING BTREE,
  INDEX `idx_campaigns_time`(`start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_campaigns_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '营销活动表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.8 referral_records 表 — Entity: ReferralRecord.java
-- 错误原因：推荐记录表在SQL中完全缺失
-- 业务影响：ReferralRecord实体无法持久化，推荐返利功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `referral_records`;
CREATE TABLE `referral_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `referrer_user_id` bigint NOT NULL COMMENT '推荐人用户ID',
  `referred_user_id` bigint NOT NULL COMMENT '被推荐人用户ID',
  `referral_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐码',
  `reward_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励类型',
  `reward_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '奖励金额',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待发放，ISSUED-已发放，CANCELLED-已取消',
  `referred_at` datetime NULL DEFAULT NULL COMMENT '推荐时间',
  `rewarded_at` datetime NULL DEFAULT NULL COMMENT '奖励发放时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rr_referrer`(`referrer_user_id` ASC) USING BTREE,
  INDEX `idx_rr_referred`(`referred_user_id` ASC) USING BTREE,
  INDEX `idx_rr_status`(`status` ASC) USING BTREE,
  INDEX `idx_rr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '推荐记录表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.9 revenue_daily_reports 表 — Entity: RevenueDailyReport.java
-- 错误原因：收入日报表在SQL中完全缺失
-- 业务影响：RevenueDailyReport实体无法持久化，收入统计报表功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `revenue_daily_reports`;
CREATE TABLE `revenue_daily_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_date` date NOT NULL COMMENT '报表日期',
  `total_orders` int NULL DEFAULT 0 COMMENT '总订单数',
  `completed_orders` int NULL DEFAULT 0 COMMENT '完成订单数',
  `total_revenue` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '总收入',
  `actual_revenue` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '实际收入（扣除退款）',
  `refund_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额',
  `avg_order_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '平均订单金额',
  `new_customers` int NULL DEFAULT 0 COMMENT '新客户数',
  `returning_customers` int NULL DEFAULT 0 COMMENT '回访客户数',
  `ai_conversations` int NULL DEFAULT 0 COMMENT 'AI对话次数',
  `ai_resolution_rate` decimal(4, 2) NULL DEFAULT NULL COMMENT 'AI解决率',
  `token_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT 'AI Token成本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rdr_date`(`report_date` ASC) USING BTREE,
  INDEX `idx_rdr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收入日报表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.10 quality_check_records 表 — Entity: QualityCheckRecord.java
-- 错误原因：质检记录表在SQL中完全缺失
-- 业务影响：QualityCheckRecord实体无法持久化，客服质检功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `quality_check_records`;
CREATE TABLE `quality_check_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint NULL DEFAULT NULL COMMENT '关联工单ID',
  `cs_user_id` bigint NULL DEFAULT NULL COMMENT '被质检客服ID',
  `checker_id` bigint NULL DEFAULT NULL COMMENT '质检人ID',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '质检评分',
  `check_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '质检类型：RANDOM-随机，TARGETED-定向',
  `result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '质检结果：PASS-通过，FAIL-不通过，WARNING-警告',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '质检评语',
  `checked_at` datetime NULL DEFAULT NULL COMMENT '质检时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qcr_work_order`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_qcr_cs_user`(`cs_user_id` ASC) USING BTREE,
  INDEX `idx_qcr_result`(`result` ASC) USING BTREE,
  INDEX `idx_qcr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '质检记录表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.11 pricing_plans 表 — Entity: PricingPlan.java
-- 错误原因：定价方案表在SQL中完全缺失
-- 业务影响：PricingPlan实体无法持久化，SaaS定价方案管理功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `pricing_plans`;
CREATE TABLE `pricing_plans` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '方案名称',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '方案编码',
  `monthly_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '月费',
  `yearly_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '年费',
  `ai_conversation_limit` int NULL DEFAULT NULL COMMENT 'AI对话次数上限',
  `platform_limit` int NULL DEFAULT NULL COMMENT '平台接入数量上限',
  `companion_limit` int NULL DEFAULT NULL COMMENT '陪玩师数量上限',
  `features` json NULL COMMENT '功能列表（JSON格式）',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否为默认方案',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '方案描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pp_code`(`plan_code` ASC) USING BTREE,
  INDEX `idx_pp_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_pp_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定价方案表' ROW_FORMAT = Dynamic;

-- ---------------------------------------------------------------------------
-- 1.12 club_subscriptions 表 — Entity: ClubSubscription.java
-- 错误原因：俱乐部订阅表在SQL中完全缺失
-- 业务影响：ClubSubscription实体无法持久化，SaaS订阅管理功能不可用
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `club_subscriptions`;
CREATE TABLE `club_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NOT NULL COMMENT '俱乐部配置ID',
  `pricing_plan_id` bigint NOT NULL COMMENT '定价方案ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TRIAL' COMMENT '状态：TRIAL-试用，ACTIVE-生效中，EXPIRED-已过期，CANCELLED-已取消',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `auto_renew` tinyint(1) NULL DEFAULT 0 COMMENT '是否自动续费',
  `ai_conversations_used` int NULL DEFAULT 0 COMMENT '已使用AI对话次数',
  `ai_conversations_limit` int NULL DEFAULT NULL COMMENT 'AI对话次数上限',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cs_club_config`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_cs_pricing_plan`(`pricing_plan_id` ASC) USING BTREE,
  INDEX `idx_cs_status`(`status` ASC) USING BTREE,
  INDEX `idx_cs_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '俱乐部订阅表' ROW_FORMAT = Dynamic;


-- ============================================================================
-- 二、缺失字段修复（Entity类有字段但SQL表中无对应列）
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 2.1 companions 表缺失 club_config_id 字段
-- 错误原因：Companion.java中有clubConfigId字段（对应club_config_id列），
--          SQL中companions表有此列但INSERT数据中使用了该列，表结构中也有。
--          实际对比：SQL表结构中已有club_config_id，此条无需修复。
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 2.2 companions 表缺失 level_name 字段
-- 错误原因：SQL中companions表有level_name列，但Companion.java实体中无此字段
-- 业务影响：level_name作为冗余字段在SQL中存在但代码不使用，不影响业务逻辑
-- 建议：保留SQL中的level_name列（冗余字段可提高查询性能），无需修复
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 2.3 pending_messages 表缺失自动回复相关字段
-- 错误原因：SQL中pending_messages表有auto_reply_keyword、matched_intent、
--          intent_confidence、auto_reply_template、auto_reply_at、auto_reply_used
--          共6个字段，但PendingMessage.java实体中无这些字段
-- 业务影响：自动回复相关功能无法通过实体类操作，代码中未使用这些字段
-- 修复方案：在PendingMessage实体中补充这些字段（代码修复，非SQL修复）
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 2.4 conversation_sessions 表字段对比
-- SQL有: id, user_id, platform, status, ai_model, message_count,
--        ai_message_count, human_message_count, first_message_at,
--        last_message_at, resolved, context_summary, created_at,
--        updated_at, deleted, deleted_at
-- Entity有: id, userId, platform, status, aiModel, messageCount,
--           aiMessageCount, humanMessageCount, firstMessageAt,
--           lastMessageAt, resolved, contextSummary, +BaseEntity字段
-- 结论：完全匹配，无需修复
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 2.5 orders 表字段对比
-- SQL有: id, order_no, user_id, companion_id, companion_name,
--        service_type, game_type, price_rule_id, order_status,
--        scheduled_start, scheduled_end, actual_start, actual_end,
--        duration_minutes, total_amount, paid_amount, payment_status,
--        payment_method, transaction_id, payment_time, work_order_id,
--        remark, time_source, cancel_reason, schedule_id, +BaseEntity
-- Entity有: orderNo, userId, companionId, companionName, serviceType,
--           gameType, priceRuleId, orderStatus, scheduledStart,
--           scheduledEnd, actualStart, actualEnd, durationMinutes,
--           totalAmount, paidAmount, paymentStatus, paymentMethod,
--           transactionId, paymentTime, workOrderId, remark,
--           timeSource, cancelReason, scheduleId, +BaseEntity
-- 结论：完全匹配，无需修复
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 2.6 customer_satisfaction 表字段对比
-- SQL中 related_order_id 注释为"关联服务追踪ID"，但Entity中字段名为
-- relatedOrderId，语义上应为"关联订单ID"
-- 错误原因：列注释与业务含义不一致
-- 业务影响：不影响代码运行，但注释误导开发者
-- ---------------------------------------------------------------------------
ALTER TABLE `customer_satisfaction` MODIFY COLUMN `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID';


-- ============================================================================
-- 三、索引缺失修复
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 3.1 conversation_sessions 缺失 user_id + last_message_at 复合索引
-- 错误原因：查询"某用户最近会话"场景频繁，当前仅有单列索引
-- 业务影响：用户会话列表查询性能不佳
-- ---------------------------------------------------------------------------
-- 已有 idx_conv_user_id 和 idx_conv_last_message，复合查询可接受，暂不添加

-- ---------------------------------------------------------------------------
-- 3.2 pending_messages 缺失 user_id + status 复合索引
-- 错误原因：查询"某用户的待处理消息"场景频繁
-- 业务影响：客服查看某客户待处理消息时性能不佳
-- ---------------------------------------------------------------------------
ALTER TABLE `pending_messages` ADD INDEX `idx_pm_user_status`(`user_id` ASC, `status` ASC) USING BTREE;


-- ============================================================================
-- 四、数据类型不匹配修复
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 4.1 companions.enabled 字段类型
-- SQL: int NOT NULL DEFAULT 1
-- Entity: Integer enabled
-- 结论：int对应Integer，类型匹配，无需修复
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 4.2 conversation_sessions.resolved 字段类型
-- SQL: int NULL DEFAULT 0
-- Entity: Integer resolved
-- 结论：int对应Integer，类型匹配，无需修复
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 4.3 customer_satisfaction.related_order_id 注释修正（已在2.6处理）
-- ---------------------------------------------------------------------------


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 修复完成统计
-- ============================================================================
-- 新增表：12个
--   companion_games, companion_trainings, customer_warning_rules,
--   service_price_rules, reply_usage_stats, work_order_slas,
--   campaigns, referral_records, revenue_daily_reports,
--   quality_check_records, pricing_plans, club_subscriptions
--
-- 字段修复：1个（customer_satisfaction.related_order_id注释修正）
--
-- 索引新增：1个（pending_messages.idx_pm_user_status）
-- ============================================================================
