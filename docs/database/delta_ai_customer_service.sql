/*
 Navicat Premium Dump SQL

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 90600 (9.6.0-commercial)
 Source Host           : localhost:3306
 Source Schema         : delta_ai_customer_service

 Target Server Type    : MySQL
 Target Server Version : 90600 (9.6.0-commercial)
 File Encoding         : 65001

 Date: 12/05/2026 13:40:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_package
-- ----------------------------
DROP TABLE IF EXISTS `activity_package`;
CREATE TABLE `activity_package`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '俱乐部配置ID',
  `game_config_id` bigint NULL DEFAULT NULL COMMENT '游戏配置ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动类型',
  `service_item_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '包含的服务项目ID列表',
  `package_price` decimal(10, 2) NOT NULL COMMENT '套餐价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `banner_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '横幅图片URL',
  `terms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '条款说明',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_package_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_activity_package_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_activity_package_game_config_id`(`game_config_id` ASC) USING BTREE,
  INDEX `idx_activity_package_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动套餐表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_package
-- ----------------------------
INSERT INTO `activity_package` VALUES (1, 1, 1, '新客尝鲜套餐', '专为新客户打造的体验套餐，含2小时顶尖陪玩+基础教学指导', 'TRIAL', '2,3', 800.00, 1300.00, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 'https://cdn.delta.com/banners/new_user.png', '仅限新注册用户使用，每人限购一次', 1, 1, '2026-04-25 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `activity_package` VALUES (2, 1, 1, '五一大促套餐', '五一劳动节特惠，3小时明星陪玩套餐直降200元', 'HOLIDAY', '2', 2800.00, 3000.00, '2026-05-01 00:00:00', '2026-05-07 23:59:59', 'https://cdn.delta.com/banners/labor_day.png', '活动期间下单享优惠，不与会员折扣叠加', 2, 1, '2026-04-28 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `activity_package` VALUES (3, 1, 1, '双排上分包', '与好友一起享受陪玩服务，双人套餐享8折优惠', 'OTHER', '1', 450.00, 600.00, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 'https://cdn.delta.com/banners/duo.png', '需由两位客户同时下单方可使用', 3, 1, '2026-03-25 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `activity_package` VALUES (4, 2, 4, '深夜狂欢套餐', '夜猫子专属！3小时深夜服务+免费饮品零食配送', 'OTHER', '5', 600.00, 750.00, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 'https://cdn.delta.com/banners/night_party.png', '仅限22:00-06:00时段使用', 1, 1, '2026-04-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `activity_package` VALUES (5, 3, 5, '零基础成长套餐', '10小时系统教学套餐，从入门到精通', 'EDUCATION', '6', 1000.00, 1200.00, '2026-03-01 00:00:00', '2026-12-31 23:59:59', 'https://cdn.delta.com/banners/growth.png', '含10节系统课程，可分次使用', 1, 1, '2026-02-20 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for activity_package_usage
-- ----------------------------
DROP TABLE IF EXISTS `activity_package_usage`;
CREATE TABLE `activity_package_usage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `package_id` bigint NOT NULL COMMENT '活动礼包ID',
  `user_id` bigint NOT NULL COMMENT '核销用户ID',
  `claim_time` datetime NOT NULL COMMENT '领取时间',
  `is_converted` int NOT NULL DEFAULT 0 COMMENT '是否已核销：0-未核销，1-已核销',
  `converted_time` datetime NULL DEFAULT NULL COMMENT '核销时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_apu_package_id`(`package_id` ASC) USING BTREE,
  INDEX `idx_apu_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apu_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动礼包核销记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_package_usage
-- ----------------------------

-- ----------------------------
-- Table structure for ai_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_config`;
CREATE TABLE `ai_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置类型，用于分组管理',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_config_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_config
-- ----------------------------
INSERT INTO `ai_config` VALUES (1, 'ai.model.default', 'GPT-4o', 'MODEL', '默认AI模型', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (2, 'ai.model.temperature', '0.8', 'MODEL', 'AI回复温度参数（0-1），越高越有创意', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (3, 'ai.model.max_tokens', '2048', 'MODEL', '单次最大Token数', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (4, 'ai.rate.limit_per_minute', '100', 'RATE', '每分钟请求限制次数', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (5, 'ai.transfer.keyword', '投诉,退款,人工,客服', 'TRANSFER', '触发转人工的关键词', '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (6, 'ai.welcome.enabled', 'true', 'FEATURE', '是否启用AI欢迎消息', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (7, 'ai.sentiment.enabled', 'true', 'FEATURE', '是否启用情感分析', '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_config` VALUES (8, 'ai.context.memory_size', '20', 'CONTEXT', 'AI对话上下文记忆条数', '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for ai_personality_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_personality_config`;
CREATE TABLE `ai_personality_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '俱乐部配置ID',
  `personality_style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '人格风格',
  `industry_style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '行业风格',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认',
  `emotion_intelligence_level` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'BASIC' COMMENT '情绪智能等级',
  `sentiment_sensitivity` int NULL DEFAULT 5 COMMENT '情绪敏感度',
  `proactive_comfort` tinyint(1) NULL DEFAULT 0 COMMENT '是否主动安抚',
  `address_format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '称呼格式',
  `self_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自称方式',
  `emoji_usage` int NULL DEFAULT 3 COMMENT '表情符号使用频率',
  `slang_usage` int NULL DEFAULT 2 COMMENT '网络用语使用频率',
  `formality_level` int NULL DEFAULT 4 COMMENT '正式程度',
  `greeting_style` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '问候风格',
  `max_reply_length` int NULL DEFAULT 500 COMMENT '最大回复长度',
  `use_game_terminology` tinyint(1) NULL DEFAULT 0 COMMENT '是否使用游戏术语',
  `conversion_style` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SOFT' COMMENT '转化风格',
  `conversion_rate` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '转化率',
  `satisfaction_score` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '满意度得分',
  `total_conversations` bigint NULL DEFAULT 0 COMMENT '总对话数',
  `enabled` int NULL DEFAULT 1 COMMENT '是否启用',
  `priority` int NULL DEFAULT 0 COMMENT '优先级',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pc_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_pc_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_pc_personality_style`(`personality_style` ASC) USING BTREE,
  INDEX `idx_pc_enabled_priority`(`enabled` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_pc_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI人格配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_personality_config
-- ----------------------------
INSERT INTO `ai_personality_config` VALUES (1, 1, 'FRIENDLY', 'GAMING', 'FPS', 1, 'ADVANCED', 7, 1, '大佬', '小助手', 4, 3, 3, '嘿大佬，今天想玩什么游戏呀？', 500, 1, 'SOFT', 0.00, 4.80, 5000, 1, 10, NULL, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_personality_config` VALUES (2, 2, 'CHEERFUL', 'GAMING', 'FPS', 1, 'BASIC', 5, 1, '夜猫子', '小夜', 5, 4, 2, '哈喽夜猫子～深夜档已就位！', 400, 1, 'SOFT', 0.00, 4.50, 3000, 1, 8, NULL, '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_personality_config` VALUES (3, 3, 'GENTLE', 'EDUCATION', 'FPS', 1, 'ADVANCED', 8, 1, '同学', '老师', 2, 1, 5, '同学你好，准备好开始今天的学习了吗？', 600, 0, 'SOFT', 0.00, 4.60, 1500, 1, 6, NULL, '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `ai_personality_config` VALUES (4, 1, 'ENTHUSIASTIC', 'GAMING', 'FPS', 0, 'PREMIUM', 9, 1, '战神', '你的战友', 5, 5, 2, '战神归来！今天我们要征服哪张地图？', 500, 1, 'AGGRESSIVE', 0.00, 4.90, 3500, 1, 5, NULL, '2026-03-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for campaign
-- ----------------------------
DROP TABLE IF EXISTS `campaign`;
CREATE TABLE `campaign`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '俱乐部配置ID',
  `campaign_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `campaign_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型：TRIAL-试用推广，REFERRAL-裂变拉新，HOLIDAY-节日营销，RECALL-复购唤醒，OTHER-其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `start_at` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_at` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `target_new_users` int NULL DEFAULT 0 COMMENT '目标拉新人数',
  `actual_new_users` int NULL DEFAULT 0 COMMENT '实际拉新人数',
  `budget` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '活动预算（元）',
  `actual_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实际花费（元）',
  `reward_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '奖励方案描述',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '活动状态：DRAFT-草稿，ACTIVE-进行中，PAUSED-已暂停，ENDED-已结束，CANCELLED-已取消',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cam_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_cam_status`(`status` ASC) USING BTREE,
  INDEX `idx_cam_start_at`(`start_at` ASC) USING BTREE,
  INDEX `idx_cam_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '营销活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of campaign
-- ----------------------------
INSERT INTO `campaign` VALUES (1, 1, '星河行动-好友裂变', 'REFERRAL', '推荐好友注册下单，双方各得100元代金券，推荐人额外获得1个月VIP会员', '2026-05-01 00:00:00', '2026-06-30 23:59:59', 500, 85, 50000.00, 8500.00, '成功推荐1人：双方各100元代金券\n成功推荐5人：额外获1个月VIP\n成功推荐10人：额外获3个月VIP', 'ACTIVE', NULL, '2026-04-20 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `campaign` VALUES (2, 2, '深夜特惠季', 'RECALL', '针对30天未活跃的夜猫子客户，定向推送专属优惠券和限时折扣', '2026-05-10 00:00:00', '2026-06-10 23:59:59', 200, 35, 10000.00, 2800.00, '回归客户首单5折\n连续下单3单享7折', 'ACTIVE', NULL, '2026-05-05 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `campaign` VALUES (3, 1, '五一大促', 'HOLIDAY', '五一劳动节全场陪玩服务9折，套餐最高立减200元', '2026-05-01 00:00:00', '2026-05-07 23:59:59', 300, 220, 30000.00, 25000.00, '全场9折\n套餐最高立减200元\n会员充值加赠20%', 'ENDED', NULL, '2026-04-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for club_config
-- ----------------------------
DROP TABLE IF EXISTS `club_config`;
CREATE TABLE `club_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '俱乐部名称',
  `club_logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '俱乐部Logo URL',
  `main_games` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主营游戏，多个游戏用逗号分隔',
  `service_slogan` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务口号/标语',
  `welcome_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '欢迎语，新客户关注时自动发送',
  `contact_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `price_level_two` decimal(10, 2) NULL DEFAULT NULL COMMENT '二品陪玩师价格（元/小时）',
  `price_level_one` decimal(10, 2) NULL DEFAULT NULL COMMENT '一品陪玩师价格（元/小时）',
  `price_top` decimal(10, 2) NULL DEFAULT NULL COMMENT '顶尖陪玩师价格（元/小时）',
  `price_star` decimal(10, 2) NULL DEFAULT NULL COMMENT '明星陪玩师价格（元/小时）',
  `club_features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '俱乐部特色',
  `custom_level_names` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自定义等级名称',
  `service_promise` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务承诺',
  `refund_policy` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '退款政策',
  `member_discount` decimal(10, 2) NULL DEFAULT NULL COMMENT '会员折扣',
  `recharge_bonus` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '充值优惠说明',
  `custom_welcome_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自定义欢迎语模板',
  `ai_personality` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI人格配置',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_club_config_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '俱乐部配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_config
-- ----------------------------
INSERT INTO `club_config` VALUES (1, '三角洲精英俱乐部', 'https://cdn.delta.com/clubs/elite_logo.png', '三角洲行动,CSGO,APEX', '专业陪玩，带你起飞！', '欢迎加入三角洲精英俱乐部！我们提供最专业的陪玩服务，助你成为游戏中的王者！', '客服电话: 400-888-0001', 150.00, 300.00, 500.00, 1000.00, '拥有三角洲行动TOP100排名选手,专业教练团队,7x24小时服务', '{\"LEVEL_TWO\":\"精英\", \"LEVEL_ONE\":\"大师\", \"TOP\":\"王者\", \"STAR\":\"传奇\"}', '不满意全额退款，迟到双倍赔偿', '服务开始前可全额退款，服务开始后按比例退款', 0.90, '充值1000送100，充值5000送800', '亲爱的{昵称}，欢迎加入三角洲精英俱乐部！', '专业热情', '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_config` VALUES (2, '夜猫子陪玩社', 'https://cdn.delta.com/clubs/night_logo.png', '三角洲行动,绝地求生,战地', '深夜不孤单，陪你战到天亮', '欢迎来到夜猫子陪玩社！深夜档专属陪玩，让你不再孤独上分！', '客服电话: 400-888-0002', 100.00, 200.00, 400.00, 800.00, '深夜专属服务(22:00-06:00),高颜值陪玩师团队,语音/视频陪玩', '{\"LEVEL_TWO\":\"夜莺\", \"LEVEL_ONE\":\"夜鹰\", \"TOP\":\"夜神\", \"STAR\":\"夜皇\"}', '深夜服务保障，不满意免费重来', '服务开始前可全额退款', 0.85, '首单8折，推荐好友双方各得100元代金券', '嘿{昵称}，夜猫子们都在等你哦！', '活泼亲切', '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_config` VALUES (3, '新手成长学院', 'https://cdn.delta.com/clubs/academy_logo.png', '三角洲行动,使命召唤,彩虹六号', '从菜鸟到大神，我们一路相伴', '欢迎加入新手成长学院！零基础也不用怕，我们的专业教练带你从零开始，快速成长！', '客服电话: 400-888-0003', 80.00, 200.00, 350.00, 600.00, '新手专属教学体系,技能评估报告,成长路线规划,一对一指导', '{\"LEVEL_TWO\":\"助教\", \"LEVEL_ONE\":\"讲师\", \"TOP\":\"教授\", \"STAR\":\"院士\"}', '学不会免费重教，包教包会', '课程开始前可全额退款', 0.80, '学生认证享8折，首次注册送免费体验课', '{昵称}同学，准备好开启你的成长之旅了吗？', '耐心温和', '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for club_level_prices
-- ----------------------------
DROP TABLE IF EXISTS `club_level_prices`;
CREATE TABLE `club_level_prices`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NOT NULL COMMENT '关联的俱乐部配置ID',
  `level_id` bigint NOT NULL COMMENT '关联的陪玩师等级ID',
  `price` decimal(10, 2) NOT NULL COMMENT '该等级在该俱乐部下的价格（元/小时）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_clp_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_clp_level_id`(`level_id` ASC) USING BTREE,
  INDEX `idx_clp_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '俱乐部等级价格关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_level_prices
-- ----------------------------
INSERT INTO `club_level_prices` VALUES (1, 1, 1, 150.00, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (2, 1, 2, 300.00, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (3, 1, 3, 500.00, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (4, 1, 4, 1000.00, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (5, 2, 1, 100.00, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (6, 2, 2, 200.00, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (7, 2, 3, 400.00, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (8, 2, 4, 800.00, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (9, 3, 1, 80.00, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (10, 3, 2, 200.00, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (11, 3, 3, 350.00, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_level_prices` VALUES (12, 3, 4, 600.00, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for club_subscription
-- ----------------------------
DROP TABLE IF EXISTS `club_subscription`;
CREATE TABLE `club_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NOT NULL COMMENT '俱乐部配置ID',
  `plan_id` bigint NOT NULL COMMENT '定价方案ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TRIAL' COMMENT '订阅状态：TRIAL-试用中，ACTIVE-生效中，EXPIRED-已过期，CANCELLED-已取消',
  `start_at` datetime NULL DEFAULT NULL COMMENT '订阅开始时间',
  `expire_at` datetime NULL DEFAULT NULL COMMENT '订阅到期时间',
  `trial_end_at` datetime NULL DEFAULT NULL COMMENT '试用到期时间',
  `auto_renew` tinyint(1) NULL DEFAULT 0 COMMENT '是否自动续费',
  `paid_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实付金额',
  `payment_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `payment_transaction_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付流水号',
  `paid_at` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cs_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_cs_status`(`status` ASC) USING BTREE,
  INDEX `idx_cs_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `idx_cs_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '俱乐部订阅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_subscription
-- ----------------------------
INSERT INTO `club_subscription` VALUES (1, 1, 3, 'ACTIVE', '2026-01-01 00:00:00', '2027-01-01 00:00:00', NULL, 1, 9999.00, 'ALIPAY', 'TXN_ENT_001_20260101', '2026-01-01 00:00:00', '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_subscription` VALUES (2, 2, 2, 'ACTIVE', '2026-02-01 00:00:00', '2027-02-01 00:00:00', NULL, 1, 2999.00, 'WECHAT', 'TXN_PRO_002_20260201', '2026-02-01 00:00:00', '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `club_subscription` VALUES (3, 3, 1, 'TRIAL', '2026-05-01 00:00:00', NULL, '2026-06-01 00:00:00', 0, 0.00, NULL, NULL, NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for companion_game
-- ----------------------------
DROP TABLE IF EXISTS `companion_game`;
CREATE TABLE `companion_game`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `game_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏编码（如delta_force、league_of_legends）',
  `proficiency` int NULL DEFAULT NULL COMMENT '该游戏的熟练度等级(1-5)',
  `rank_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '该游戏的段位排名',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cg_companion_game`(`companion_id` ASC, `game_code` ASC) USING BTREE,
  INDEX `idx_cg_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_cg_game_code`(`game_code` ASC) USING BTREE,
  INDEX `idx_cg_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师-游戏关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_game
-- ----------------------------

-- ----------------------------
-- Table structure for companion_levels
-- ----------------------------
DROP TABLE IF EXISTS `companion_levels`;
CREATE TABLE `companion_levels`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级名称，如\"二品\"、\"一品\"、\"顶尖\"',
  `level_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级编码，如 LEVEL_TWO、LEVEL_ONE、TOP',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号，数值越小等级越高',
  `base_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '该等级基础价格（元/小时）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级描述',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_companion_levels_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_companion_levels_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_levels
-- ----------------------------
INSERT INTO `companion_levels` VALUES (1, '二品', 'LEVEL_TWO', 4, 150.00, '初级陪玩师，具备基础游戏技能和服务能力', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_levels` VALUES (2, '一品', 'LEVEL_ONE', 3, 300.00, '中级陪玩师，游戏技能熟练，服务经验丰富', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_levels` VALUES (3, '顶尖', 'TOP', 2, 500.00, '高级陪玩师，游戏水平顶尖，具备教学能力', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_levels` VALUES (4, '明星', 'STAR', 1, 1000.00, '明星陪玩师，全国顶尖水平，拥有大量粉丝', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for companion_notifications
-- ----------------------------
DROP TABLE IF EXISTS `companion_notifications`;
CREATE TABLE `companion_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：NEW_ORDER-新订单，STATUS_CHANGE-状态变更',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容',
  `is_read` int NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cn_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_cn_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_cn_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师通知消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_notifications
-- ----------------------------
INSERT INTO `companion_notifications` VALUES (1, 1, 1, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260501-001，客户：三角洲战神，时间：5月1日 18:00-20:00，金额：1000元', 1, '2026-05-01 15:30:00');
INSERT INTO `companion_notifications` VALUES (2, 1, 11, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260512-011，客户：三角洲战神，时间：5月12日 20:00-22:00，金额：1000元', 0, '2026-05-12 10:05:00');
INSERT INTO `companion_notifications` VALUES (3, 3, 2, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260420-002，客户：吃鸡达人小王，时间：4月20日 19:00-20:30，金额：450元', 1, '2026-04-20 15:30:00');
INSERT INTO `companion_notifications` VALUES (4, 5, 3, 'STATUS_CHANGE', '订单退款通知', '您的订单 ORD-20260315-003 已被客户投诉并退款，扣款200元，请查看详情', 1, '2026-03-16 15:00:00');
INSERT INTO `companion_notifications` VALUES (5, 6, 7, 'STATUS_CHANGE', '订单退款通知', '您的订单 ORD-20260324-007 已被客户投诉并退款，请查看详情。系统已对您进行停权处理。', 1, '2026-03-25 12:00:00');
INSERT INTO `companion_notifications` VALUES (6, 2, 4, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260415-004，客户：老兵不死，时间：4月15日 20:00-22:00，金额：1000元', 1, '2026-04-15 16:30:00');
INSERT INTO `companion_notifications` VALUES (7, 4, 5, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260410-005，客户：K神降临，时间：4月10日 15:00-16:30，金额：450元', 1, '2026-04-10 13:00:00');
INSERT INTO `companion_notifications` VALUES (8, 1, 6, 'NEW_ORDER', '新订单通知', '您有一个新的教学订单 ORD-20260320-006，客户：枪王之王，时间：3月20日 18:00-20:00，金额：1000元', 1, '2026-03-20 15:05:00');
INSERT INTO `companion_notifications` VALUES (9, 2, 8, 'NEW_ORDER', '新订单通知', '您有一个新的教学订单 ORD-20260425-008，客户：战术大师Leo，时间：4月25日 10:00-11:30，金额：450元', 1, '2026-04-25 08:30:00');
INSERT INTO `companion_notifications` VALUES (10, 3, 9, 'NEW_ORDER', '新订单通知', '您有一个新的套餐订单 ORD-20260502-009，客户：守点老王，时间：5月2日 10:00-13:00，金额：1500元', 1, '2026-05-02 08:05:00');
INSERT INTO `companion_notifications` VALUES (11, 4, 10, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260505-010，客户：测试用户A，时间：5月5日 08:00-10:00，金额：600元', 1, '2026-05-05 06:05:00');
INSERT INTO `companion_notifications` VALUES (12, 1, 19, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单 ORD-20260506-019，客户：枪王之王，时间：5月6日 15:00-17:30，金额：1250元', 1, '2026-05-06 12:30:00');
INSERT INTO `companion_notifications` VALUES (13, 5, 15, 'STATUS_CHANGE', '异常订单通知', '您的订单 ORD-20260501-015 被标记为异常订单，客户中途退出，请关注后续处理', 0, '2026-05-01 23:00:00');
INSERT INTO `companion_notifications` VALUES (14, 6, NULL, 'STATUS_CHANGE', '停权通知', '由于多次客户投诉，您的账号已被暂时停权。请完成合规培训后联系管理员恢复权限。', 0, '2026-03-25 12:05:00');
INSERT INTO `companion_notifications` VALUES (15, 5, NULL, 'STATUS_CHANGE', '警告通知', '因服务迟到，您收到一次警告处分，扣除当月绩效50元。累计3次警告将影响等级评定。', 1, '2026-03-16 15:05:00');
INSERT INTO `companion_notifications` VALUES (19, 9, 24, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单待处理，订单号：ORD20260512717000272', 0, '2026-05-12 11:48:52');
INSERT INTO `companion_notifications` VALUES (20, 9, 25, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单待处理，订单号：ORD20260512332600766', 0, '2026-05-12 11:50:34');
INSERT INTO `companion_notifications` VALUES (21, 12, 26, 'NEW_ORDER', '新订单通知', '您有一个新的陪玩订单待处理，订单号：ORD20260512380500974', 0, '2026-05-12 11:50:34');

-- ----------------------------
-- Table structure for companion_rating_summary
-- ----------------------------
DROP TABLE IF EXISTS `companion_rating_summary`;
CREATE TABLE `companion_rating_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `total_reviews` int NULL DEFAULT 0 COMMENT '评价总数',
  `avg_rating` decimal(3, 2) NULL DEFAULT NULL COMMENT '平均评分(1.00-5.00)',
  `rating1_count` int NULL DEFAULT 0 COMMENT '1星评价数',
  `rating2_count` int NULL DEFAULT 0 COMMENT '2星评价数',
  `rating3_count` int NULL DEFAULT 0 COMMENT '3星评价数',
  `rating4_count` int NULL DEFAULT 0 COMMENT '4星评价数',
  `rating5_count` int NULL DEFAULT 0 COMMENT '5星评价数',
  `positive_tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '正面评价标签(逗号分隔)',
  `negative_tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '负面评价标签(逗号分隔)',
  `last_review_at` datetime NULL DEFAULT NULL COMMENT '最近评价时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_crs_companion_id`(`companion_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师综合评分汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_rating_summary
-- ----------------------------
INSERT INTO `companion_rating_summary` VALUES (1, 1, 3, 4.67, 0, 0, 0, 1, 2, '技术好,教学,推荐', '', '2026-05-08 20:00:00', '2026-01-01 00:00:00', '2026-05-08 20:00:00');
INSERT INTO `companion_rating_summary` VALUES (2, 2, 3, 4.33, 0, 0, 0, 2, 1, '声甜,推荐', '价格', '2026-05-09 14:00:00', '2026-01-01 00:00:00', '2026-05-09 14:00:00');
INSERT INTO `companion_rating_summary` VALUES (3, 3, 2, 5.00, 0, 0, 0, 0, 2, '划算,推荐,套餐', '', '2026-05-02 14:00:00', '2026-01-15 00:00:00', '2026-05-02 14:00:00');
INSERT INTO `companion_rating_summary` VALUES (4, 4, 2, 4.50, 0, 0, 0, 1, 1, '技术好,信赖', '', '2026-05-05 10:00:00', '2026-01-15 00:00:00', '2026-05-05 10:00:00');
INSERT INTO `companion_rating_summary` VALUES (5, 5, 1, 2.00, 0, 1, 0, 0, 0, '', '迟到,技术差', '2026-03-15 17:00:00', '2026-02-01 00:00:00', '2026-03-15 17:00:00');
INSERT INTO `companion_rating_summary` VALUES (6, 6, 1, 1.00, 1, 0, 0, 0, 0, '', '态度差,退款,投诉', '2026-03-24 23:00:00', '2026-02-15 00:00:00', '2026-03-24 23:00:00');
INSERT INTO `companion_rating_summary` VALUES (7, 7, 1, 4.00, 0, 0, 0, 1, 0, '聊得开心', '', '2026-05-04 16:00:00', '2026-03-01 00:00:00', '2026-05-04 16:00:00');
INSERT INTO `companion_rating_summary` VALUES (8, 8, 0, 0.00, 0, 0, 0, 0, 0, '', '', NULL, '2026-03-15 00:00:00', '2026-03-15 00:00:00');
INSERT INTO `companion_rating_summary` VALUES (9, 9, 0, 0.00, 0, 0, 0, 0, 0, '', '', NULL, '2026-04-01 00:00:00', '2026-04-01 00:00:00');
INSERT INTO `companion_rating_summary` VALUES (10, 10, 0, 0.00, 0, 0, 0, 0, 0, '', '', NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00');
INSERT INTO `companion_rating_summary` VALUES (11, 11, 0, 0.00, 0, 0, 0, 0, 0, '', '', NULL, '2026-03-01 00:00:00', '2026-03-01 00:00:00');
INSERT INTO `companion_rating_summary` VALUES (12, 12, 0, 0.00, 0, 0, 0, 0, 0, '', '', NULL, '2026-04-01 00:00:00', '2026-04-01 00:00:00');

-- ----------------------------
-- Table structure for companion_schedules
-- ----------------------------
DROP TABLE IF EXISTS `companion_schedules`;
CREATE TABLE `companion_schedules`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '关联的陪玩师ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时段标识，如\"上午\"、\"下午\"、\"晚上\"',
  `start_time` time NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE-可预约，BOOKED-已预约，UNAVAILABLE-不可用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_companion_schedules_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_companion_schedules_status`(`status` ASC) USING BTREE,
  INDEX `idx_companion_schedules_companion_start`(`companion_id` ASC, `start_time` ASC) USING BTREE,
  INDEX `idx_schedules_companion_date`(`companion_id` ASC, `schedule_date` ASC) USING BTREE,
  INDEX `idx_schedules_date_status`(`schedule_date` ASC, `status` ASC) USING BTREE,
  INDEX `idx_schedules_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_schedules
-- ----------------------------
INSERT INTO `companion_schedules` VALUES (1, 1, '2026-05-12', '晚上', '18:00:00', '22:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (2, 1, '2026-05-13', '晚上', '18:00:00', '22:00:00', 'BOOKED', '已预约', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (3, 2, '2026-05-12', '晚上', '19:00:00', '23:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (4, 2, '2026-05-13', '下午', '14:00:00', '18:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (5, 3, '2026-05-12', '上午', '09:00:00', '12:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (6, 3, '2026-05-13', '下午', '14:00:00', '18:00:00', 'BOOKED', '已预约', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (7, 4, '2026-05-12', '下午', '14:00:00', '18:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (8, 4, '2026-05-13', '晚上', '18:00:00', '22:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (9, 5, '2026-05-12', '上午', '09:00:00', '12:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (10, 5, '2026-05-13', '上午', '09:00:00', '12:00:00', 'UNAVAILABLE', '请假', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (11, 6, '2026-05-12', '晚上', '20:00:00', '24:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (12, 6, '2026-05-13', '晚上', '20:00:00', '24:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (13, 7, '2026-05-12', '下午', '14:00:00', '18:00:00', 'BOOKED', '已预约', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (14, 7, '2026-05-13', '晚上', '18:00:00', '22:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (15, 8, '2026-05-12', '晚上', '19:00:00', '23:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (16, 8, '2026-05-13', '下午', '14:00:00', '18:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (17, 9, '2026-05-12', '上午', '09:00:00', '12:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (18, 9, '2026-05-13', '上午', '09:00:00', '12:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (19, 10, '2026-05-12', '晚上', '18:00:00', '22:00:00', 'BOOKED', '已预约', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (20, 10, '2026-05-13', '晚上', '18:00:00', '22:00:00', 'BOOKED', '已预约', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (21, 11, '2026-05-12', '下午', '14:00:00', '18:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (22, 11, '2026-05-13', '下午', '14:00:00', '18:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (23, 12, '2026-05-12', '晚上', '20:00:00', '24:00:00', 'BOOKED', '直播档', '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_schedules` VALUES (24, 12, '2026-05-13', '晚上', '20:00:00', '24:00:00', 'AVAILABLE', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for companion_settlement
-- ----------------------------
DROP TABLE IF EXISTS `companion_settlement`;
CREATE TABLE `companion_settlement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `settlement_period` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结算周期（如2026-05-01至2026-05-31）',
  `total_orders` int NULL DEFAULT 0 COMMENT '接单总数',
  `total_revenue` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '订单总收入',
  `platform_fee` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '平台分成金额',
  `companion_income` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '陪玩师实得金额',
  `deduction_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '扣款项（违规罚款等）',
  `deduction_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扣款原因',
  `settlement_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '结算状态：PENDING-待结算，PROCESSING-结算中，COMPLETED-已结算',
  `settled_at` datetime NULL DEFAULT NULL COMMENT '实际结算时间',
  `payment_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收款方式',
  `payment_account` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收款账号',
  `confirm_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNCONFIRMED' COMMENT '陪玩师确认状态：UNCONFIRMED-未确认，CONFIRMED-已确认，DISPUTED-有异议',
  `dispute_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申诉内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cst_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_cst_status`(`settlement_status` ASC) USING BTREE,
  INDEX `idx_cst_period`(`settlement_period` ASC) USING BTREE,
  INDEX `idx_cst_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师结算记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_settlement
-- ----------------------------
INSERT INTO `companion_settlement` VALUES (1, 1, '2026-04', 30, 15000.00, 3000.00, 12000.00, 0.00, NULL, 'COMPLETED', '2026-05-01 10:00:00', 'BANK', '6222001234567890', 'CONFIRMED', NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_settlement` VALUES (2, 2, '2026-04', 45, 22500.00, 4500.00, 18000.00, 200.00, '客户投诉扣款', 'COMPLETED', '2026-05-01 10:00:00', 'ALIPAY', 'meimei@delta.com', 'CONFIRMED', NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_settlement` VALUES (3, 4, '2026-04', 25, 7500.00, 1500.00, 6000.00, 0.00, NULL, 'COMPLETED', '2026-05-01 10:00:00', 'WECHAT', 'xiaolong_Delta', 'CONFIRMED', NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_settlement` VALUES (4, 10, '2026-04', 60, 60000.00, 12000.00, 48000.00, 0.00, NULL, 'COMPLETED', '2026-05-01 10:00:00', 'BANK', '6222009876543210', 'CONFIRMED', NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_settlement` VALUES (5, 1, '2026-05', 10, 5000.00, 1000.00, 4000.00, 0.00, NULL, 'PROCESSING', NULL, NULL, NULL, 'UNCONFIRMED', NULL, '2026-05-10 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_settlement` VALUES (6, 5, '2026-04', 12, 1800.00, 360.00, 1440.00, 100.00, '迟到扣款', 'COMPLETED', '2026-05-01 10:00:00', 'ALIPAY', 'xingxing@delta.com', 'DISPUTED', NULL, '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for companion_training
-- ----------------------------
DROP TABLE IF EXISTS `companion_training`;
CREATE TABLE `companion_training`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `companion_id` bigint NOT NULL COMMENT '陪玩师ID',
  `course_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '培训课程名称',
  `course_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '培训类型：SERVICE_STANDARD-服务规范，SCRIPT_TEMPLATE-话术模板，COMPLIANCE-合规培训，GAME_SKILL-游戏技能',
  `course_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '培训内容（文本/Markdown）',
  `training_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NOT_STARTED' COMMENT '培训状态：NOT_STARTED-未开始，IN_PROGRESS-进行中，COMPLETED-已完成',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始学习时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成学习时间',
  `exam_score` int NULL DEFAULT NULL COMMENT '考核得分(0-100)',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '培训备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ct_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_ct_status`(`training_status` ASC) USING BTREE,
  INDEX `idx_ct_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师培训记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companion_training
-- ----------------------------
INSERT INTO `companion_training` VALUES (1, 5, '新入职服务规范培训', 'SERVICE_STANDARD', '# 服务规范\n1. 准时上线\n2. 礼貌用语\n3. 不催单\n4. 保护客户隐私', 'COMPLETED', '2026-02-01 10:00:00', '2026-02-03 16:00:00', 92, '表现优秀', '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_training` VALUES (2, 5, '陪玩话术模板', 'SCRIPT_TEMPLATE', '# 标准话术\n## 开场白\n您好，我是您的陪玩师星星，很高兴为您服务！\n## 结束语\n感谢您的惠顾，期待下次再见！', 'COMPLETED', '2026-02-05 10:00:00', '2026-02-07 15:00:00', 85, '通过', '2026-02-05 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_training` VALUES (3, 6, '合规培训', 'COMPLIANCE', '# 合规要求\n- 禁止代打\n- 禁止使用外挂\n- 禁止私下交易', 'IN_PROGRESS', '2026-05-01 09:00:00', NULL, NULL, '进行中', '2026-05-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_training` VALUES (4, 9, '游戏技能提升', 'GAME_SKILL', '# 英雄联盟进阶技巧\n1. 补刀技巧\n2. 走位练习\n3. 团战意识', 'COMPLETED', '2026-04-05 10:00:00', '2026-04-10 18:00:00', 88, '技能提升明显', '2026-04-05 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_training` VALUES (5, 1, '高级教练培训', 'SERVICE_STANDARD', '# 教练能力提升\n1. 教学心理学\n2. 个性化教学方案\n3. 学员跟踪评估', 'COMPLETED', '2026-03-01 09:00:00', '2026-03-05 17:00:00', 96, '优秀教练', '2026-03-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companion_training` VALUES (6, 12, '内容安全培训', 'COMPLIANCE', '# 直播内容规范\n- 着装要求\n- 敏感词过滤\n- 版权意识', 'NOT_STARTED', NULL, NULL, NULL, '待开始', '2026-05-05 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for companions
-- ----------------------------
DROP TABLE IF EXISTS `companions`;
CREATE TABLE `companions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联系统用户ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '关联俱乐部ID',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `wechat` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信号',
  `level_id` bigint NULL DEFAULT NULL COMMENT '等级ID',
  `level_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级名称（冗余字段）',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型',
  `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '个人简介',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格（元/小时）',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `service_tags` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务标签',
  `supported_games` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '支持游戏列表',
  `kd_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT 'K/D比率',
  `rank_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '段位等级',
  `voice_sample_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '语音样本URL',
  `rating_avg` decimal(3, 2) NULL DEFAULT NULL COMMENT '平均评分',
  `order_count` int NULL DEFAULT 0 COMMENT '订单数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_companions_level_id`(`level_id` ASC) USING BTREE,
  INDEX `idx_companions_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_companions_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_companions_enabled_game_type`(`enabled` ASC, `game_type` ASC) USING BTREE,
  INDEX `idx_companions_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '陪玩师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of companions
-- ----------------------------
INSERT INTO `companions` VALUES (1, 6, 1, '陈浩', '浩神带你飞', '13900002001', 'haoshen_Delta', 3, '顶尖', 'https://cdn.delta.com/companions/hao.png', 'FPS', '三角洲行动前职业选手，KD 5.2，曾获全国大赛冠军。擅长教学，耐心细致。', 500.00, 1, '技术流,教学型,职业选手', '三角洲行动,CSGO,APEX', 5.20, '传奇', 'https://cdn.delta.com/voice/hao.mp3', 4.90, 156, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (2, NULL, 1, '林小美', '美美陪玩', '13900002002', 'meimei_Delta', 3, '顶尖', 'https://cdn.delta.com/companions/meimei.png', 'FPS', '女性陪玩师TOP3，声甜技术好，擅长双排带飞。', 500.00, 1, '声甜,女陪,双排', '三角洲行动,绝地求生', 3.80, '钻石', 'https://cdn.delta.com/voice/meimei.mp3', 4.80, 210, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (3, NULL, 1, '王大力', '大力出奇迹', '13900002003', 'dali_Delta', 3, '顶尖', 'https://cdn.delta.com/companions/dali.png', 'FPS', '前职业教练，精通战术指导和团队配合，适合新手教学。', 500.00, 1, '教练,战术,教学', '三角洲行动,彩虹六号', 3.50, '大师', NULL, 4.60, 89, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (4, NULL, 1, '赵小龙', '小龙大魔王', '13900002004', 'xiaolong_Delta', 2, '一品', 'https://cdn.delta.com/companions/xiaolong.png', 'FPS', '年轻实力派，反应快枪法准，适合高端局陪玩。', 300.00, 1, '技术好,年轻,高分段', '三角洲行动,Valorant', 4.50, '钻石', NULL, 4.50, 120, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (5, NULL, 3, '周星星', '星星点灯', '13900002005', 'xingxing_Delta', 1, '二品', 'https://cdn.delta.com/companions/xingxing.png', 'FPS', '新晋陪玩师，服务态度好，学习能力强。', 150.00, 1, '新人,态度好', '三角洲行动', 2.00, '铂金', NULL, 3.80, 35, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (6, NULL, 2, '吴天', '天哥娱乐', '13900002006', 'tiange_Delta', 1, '二品', 'https://cdn.delta.com/companions/tiange.png', 'SOCIAL', '主打娱乐陪玩，话多有趣，让你开心上分。', 150.00, 0, '娱乐,话痨,有趣', '三角洲行动', 1.50, '黄金', NULL, 3.20, 50, '2026-02-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (7, NULL, 2, '郑小帅', '帅哥陪玩', '13900002007', 'shuaige_Delta', 2, '一品', 'https://cdn.delta.com/companions/shuaige.png', 'SOCIAL', '颜值在线，声线好听，擅长社交互动和语音陪聊。', 300.00, 1, '颜值,社交,语音', '三角洲行动', 2.50, '铂金', 'https://cdn.delta.com/voice/shuaige.mp3', 4.20, 65, '2026-03-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (8, NULL, 1, '黄小仙', '小仙姐姐', '13900002008', 'xiaoxian_Delta', 2, '一品', 'https://cdn.delta.com/companions/xiaoxian.png', 'FPS', '高人气女陪玩，游戏和聊天两不误，亲和力强。', 300.00, 1, '女陪,人气,亲和', '三角洲行动,守望先锋', 2.80, '钻石', 'https://cdn.delta.com/voice/xiaoxian.mp3', 4.50, 180, '2026-03-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (9, NULL, 3, '刘技术', '技术刘老师', '13900002009', 'jsliu_Delta', 1, '二品', 'https://cdn.delta.com/companions/jsliu.png', 'MOBA', 'MOBA游戏达人，擅长英雄联盟和王者荣耀，能做操作教学。', 150.00, 1, 'MOBA,教学,耐心', '三角洲行动,英雄联盟', NULL, '钻石', NULL, 4.00, 28, '2026-04-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (10, NULL, 1, '杨明星', '明星杨杨', '13900002010', 'yangyang_Delta', 4, '明星', 'https://cdn.delta.com/companions/yangyang.png', 'FPS', '知名游戏主播，百万粉丝，全国排名前三，明星陪玩师。', 1000.00, 1, '主播,明星,全游戏', '三角洲行动,CSGO,APEX,Valorant', 6.50, '传奇', 'https://cdn.delta.com/voice/yangyang.mp3', 4.95, 320, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (11, NULL, 1, '马教练', '马教练教学', '13900002011', 'majiaolian_Delta', 4, '明星', 'https://cdn.delta.com/companions/majiaolian.png', 'FPS', '国家级电竞教练，培养过多位职业选手，教学水平一流。', 1000.00, 1, '国家级教练,职业培训', '三角洲行动,CSGO', 4.00, '传奇', NULL, 4.90, 95, '2026-03-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `companions` VALUES (12, NULL, 1, '陈独秀', '独秀同志', '13900002012', 'duxiu_Delta', 4, '明星', 'https://cdn.delta.com/companions/duxiu.png', 'SOCIAL', '顶流游戏主播，颜值与技术并存，每次陪玩都是一场直播秀。', 1000.00, 1, '顶流,直播,全能', '三角洲行动,APEX,守望先锋', 5.00, '传奇', 'https://cdn.delta.com/voice/duxiu.mp3', 4.85, 400, '2026-04-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for conversation_sessions
-- ----------------------------
DROP TABLE IF EXISTS `conversation_sessions`;
CREATE TABLE `conversation_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平台',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `ai_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI模型',
  `message_count` int NULL DEFAULT 0 COMMENT '消息总数',
  `ai_message_count` int NULL DEFAULT 0 COMMENT 'AI消息数',
  `human_message_count` int NULL DEFAULT 0 COMMENT '人工消息数',
  `first_message_at` datetime NULL DEFAULT NULL COMMENT '首条消息时间',
  `last_message_at` datetime NULL DEFAULT NULL COMMENT '末条消息时间',
  `resolved` int NULL DEFAULT 0 COMMENT '是否已解决',
  `context_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '上下文摘要',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conv_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_conv_last_message`(`last_message_at` ASC) USING BTREE,
  INDEX `idx_conv_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation_sessions
-- ----------------------------
INSERT INTO `conversation_sessions` VALUES (1, 1, 'wechat', 'ACTIVE', 'GPT-4o', 8, 5, 3, '2026-05-01 15:00:00', '2026-05-01 17:30:00', 1, '客户咨询陪玩预约，AI完成匹配并转人工确认', '2026-05-01 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (2, 2, 'wechat', 'ACTIVE', 'GPT-4o', 6, 4, 2, '2026-04-20 14:00:00', '2026-04-20 18:30:00', 1, '客户咨询陪玩价格，AI解答后转人工下单', '2026-04-20 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (3, 3, 'wechat', 'RESOLVED', 'GPT-4o', 4, 2, 2, '2026-03-15 12:00:00', '2026-03-15 16:30:00', 1, '客户投诉陪玩师迟到，AI初步收集信息后转人工处理', '2026-03-15 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (4, 5, 'wechat', 'ACTIVE', 'Claude-3.5', 5, 3, 2, '2026-04-15 16:00:00', '2026-04-15 19:00:00', 1, 'VIP客户预约高端陪玩服务', '2026-04-15 16:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (5, 6, 'kook', 'ACTIVE', 'GPT-4o', 7, 5, 2, '2026-04-10 12:00:00', '2026-04-10 14:30:00', 1, 'KOOK平台客户咨询陪玩服务', '2026-04-10 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (6, 7, 'kook', 'ACTIVE', 'GPT-4o', 5, 3, 2, '2026-03-20 15:00:00', '2026-03-20 17:00:00', 1, 'KOOK客户预约教学服务', '2026-03-20 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (7, 9, 'kook', 'RESOLVED', 'Claude-3.5', 6, 3, 3, '2026-03-24 19:00:00', '2026-03-24 22:30:00', 1, '客户投诉陪玩师服务态度', '2026-03-24 19:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (8, 10, 'kook', 'ACTIVE', 'GPT-4o', 4, 2, 2, '2026-04-25 08:00:00', '2026-04-25 09:30:00', 1, 'KOOK客户预约教学服务', '2026-04-25 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (9, 14, 'yy', 'ACTIVE', 'GPT-4o', 5, 3, 2, '2026-05-02 08:00:00', '2026-05-02 09:30:00', 1, 'YY客户咨询套餐服务', '2026-05-02 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `conversation_sessions` VALUES (10, 16, 'test', 'ACTIVE', 'Claude-3.5', 10, 7, 3, '2026-05-05 06:00:00', '2026-05-05 07:30:00', 1, '测试平台VIP客户多次服务咨询', '2026-05-05 06:00:00', '2026-05-12 02:04:10', 0, NULL);

-- ----------------------------
-- Table structure for cs_user_customer
-- ----------------------------
DROP TABLE IF EXISTS `cs_user_customer`;
CREATE TABLE `cs_user_customer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cs_user_id` bigint NOT NULL COMMENT '客服用户ID',
  `user_id` bigint NOT NULL COMMENT '客户用户ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平台',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
  `assigned_at` datetime NULL DEFAULT NULL COMMENT '分配时间',
  `assign_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分配类型：MANUAL-手动，SYSTEM-系统',
  `assigned_by` bigint NULL DEFAULT NULL COMMENT '分配操作人ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '关联状态：ACTIVE-活跃，INACTIVE-非活跃',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cs_user_customer_cs`(`cs_user_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_cs_user_customer_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客服-客户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cs_user_customer
-- ----------------------------
INSERT INTO `cs_user_customer` VALUES (1, 3, 1, 'wechat', '三角洲战神', '2026-01-15 10:00:00', 'MANUAL', 2, 'ACTIVE', '2026-01-15 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (2, 3, 2, 'wechat', '吃鸡达人小王', '2026-01-20 14:00:00', 'SYSTEM', NULL, 'ACTIVE', '2026-01-20 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (3, 4, 3, 'wechat', '狙击手阿强', '2026-02-05 09:00:00', 'MANUAL', 2, 'ACTIVE', '2026-02-05 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (4, 3, 5, 'wechat', '老兵不死', '2026-01-25 11:00:00', 'MANUAL', 2, 'ACTIVE', '2026-01-25 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (5, 4, 6, 'kook', 'K神降临', '2026-02-15 15:00:00', 'SYSTEM', NULL, 'ACTIVE', '2026-02-15 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (6, 3, 7, 'kook', '枪王之王', '2026-02-25 10:00:00', 'MANUAL', 2, 'ACTIVE', '2026-02-25 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (7, 4, 9, 'kook', '夜猫子玩家', '2026-03-25 20:00:00', 'SYSTEM', NULL, 'ACTIVE', '2026-03-25 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (8, 3, 10, 'kook', '战术大师Leo', '2026-04-05 08:00:00', 'MANUAL', 2, 'ACTIVE', '2026-04-05 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (9, 3, 11, 'yy', 'YY一哥', '2026-03-05 14:00:00', 'SYSTEM', NULL, 'ACTIVE', '2026-03-05 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (10, 4, 12, 'yy', '颜值区扛把子', '2026-04-10 19:00:00', 'MANUAL', 2, 'ACTIVE', '2026-04-10 19:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (11, 3, 14, 'yy', '守点老王', '2026-04-20 10:00:00', 'SYSTEM', NULL, 'ACTIVE', '2026-04-20 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (12, 4, 16, 'test', '测试用户A', '2026-01-10 09:00:00', 'MANUAL', 2, 'ACTIVE', '2026-01-10 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (13, 3, 17, 'test', '测试用户B', '2026-01-12 10:00:00', 'MANUAL', 2, 'ACTIVE', '2026-01-12 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `cs_user_customer` VALUES (14, 3, 20, 'test', '测试流失用户', '2026-01-05 08:00:00', 'SYSTEM', NULL, 'INACTIVE', '2026-01-05 08:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for customer_order_record
-- ----------------------------
DROP TABLE IF EXISTS `customer_order_record`;
CREATE TABLE `customer_order_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `companion_id` bigint NULL DEFAULT NULL COMMENT '陪玩师ID',
  `record_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记录类型',
  `order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单类型',
  `order_time` datetime NULL DEFAULT NULL COMMENT '下单时间',
  `duration_hours` decimal(10, 1) NULL DEFAULT NULL COMMENT '服务时长',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型',
  `companion_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '陪玩师等级',
  `time_slot` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时段',
  `rating` int NULL DEFAULT NULL COMMENT '评分',
  `review_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'COMPLETED' COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `service_item_id` bigint NULL DEFAULT NULL COMMENT '服务项目ID',
  `game_config_id` bigint NULL DEFAULT NULL COMMENT '游戏配置ID',
  `activity_package_id` bigint NULL DEFAULT NULL COMMENT '活动套餐ID',
  `guarantee_result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '保障结果',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cor_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_cor_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_cor_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_cor_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_cor_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户订单记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_order_record
-- ----------------------------
INSERT INTO `customer_order_record` VALUES (1, 1, 1, 1, 1, 'ORDER', 'ACCOMPANY', '2026-05-01 18:00:00', 2.0, 600.00, '三角洲行动', 'TOP', '晚上', 5, '非常满意', 'COMPLETED', '回头客', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-01 18:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (2, 2, 2, 2, 3, 'ORDER', 'ACCOMPANY', '2026-04-20 19:00:00', 1.5, 450.00, '三角洲行动', 'LEVEL_ONE', '晚上', 4, '不错', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-20 19:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (3, 3, 3, 3, 5, 'ORDER', 'TEACHING', '2026-03-15 15:00:00', 1.0, 200.00, '三角洲行动', 'LEVEL_TWO', '下午', 2, '不满意', 'COMPLETED', '迟到', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-15 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (4, 5, 5, 4, 2, 'ORDER', 'ACCOMPANY', '2026-04-15 20:00:00', 2.0, 600.00, '三角洲行动', 'TOP', '晚上', 5, '超级棒', 'COMPLETED', '老客', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-15 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (5, 6, 6, 5, 4, 'ORDER', 'ACCOMPANY', '2026-04-10 15:00:00', 1.5, 450.00, '三角洲行动', 'LEVEL_ONE', '下午', 4, '还行', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-10 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (6, 7, 7, 6, 1, 'ORDER', 'TEACHING', '2026-03-20 18:00:00', 2.0, 800.00, '三角洲行动', 'TOP', '晚上', 5, '学到很多', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-20 18:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (7, 9, 9, 7, 6, 'ORDER', 'ACCOMPANY', '2026-03-24 21:00:00', 1.0, 150.00, '三角洲行动', 'LEVEL_TWO', '晚上', 1, '极差', 'REFUNDED', '已退款', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-24 21:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (8, 10, 10, 8, 2, 'ORDER', 'TEACHING', '2026-04-25 10:00:00', 1.5, 500.00, '三角洲行动', 'LEVEL_ONE', '上午', 4, '有收获', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-25 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (9, 14, 14, 9, 3, 'ORDER', 'PACKAGE', '2026-05-02 10:00:00', 3.0, 1000.00, '三角洲行动', 'TOP', '上午', 5, '超值', 'COMPLETED', '套餐', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-02 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (10, 16, 16, 10, 4, 'ORDER', 'ACCOMPANY', '2026-05-05 08:00:00', 2.0, 600.00, '三角洲行动', 'LEVEL_ONE', '上午', 5, '完美', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-05 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (11, 1, 1, 11, 2, 'ORDER', 'ACCOMPANY', '2026-05-08 20:00:00', 1.0, 300.00, '三角洲行动', 'TOP', '晚上', 5, '一如既往好', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-08 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (12, 7, 7, 12, 1, 'ORDER', 'ACCOMPANY', '2026-05-06 15:00:00', 2.5, 750.00, '三角洲行动', 'TOP', '下午', 4, '不错不错', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-06 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (13, 11, 11, 13, 7, 'ORDER', 'SOCIAL', '2026-05-04 14:00:00', 1.5, 450.00, '三角洲行动', 'LEVEL_ONE', '下午', 4, '聊得开心', 'COMPLETED', '社交', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-04 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (14, 15, 15, 14, NULL, 'ORDER', 'ACCOMPANY', '2026-05-01 22:00:00', 1.0, 100.00, '三角洲行动', 'LEVEL_TWO', '晚上', 2, '一般', 'ABNORMAL', '异常订单', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-01 22:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (15, 17, 17, 15, 2, 'ORDER', 'ACCOMPANY', '2026-05-09 14:00:00', 2.0, 600.00, '三角洲行动', 'LEVEL_ONE', '下午', 5, '很好', 'COMPLETED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-09 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_order_record` VALUES (16, 1, 1, 24, 9, 'ORDER', 'ACCOMPANY_PLAY', '2026-05-14 20:00:00', 2.0, 300.00, 'MOBA', '钻石', '20:00-22:00', NULL, NULL, 'PENDING', 'API楠岃瘉娴嬭瘯璁㈠崟', NULL, NULL, NULL, NULL, '订单编号: ORD20260512717000272', NULL, NULL, '2026-05-12 11:48:52', '2026-05-12 11:48:52', 0, NULL);
INSERT INTO `customer_order_record` VALUES (17, 1, 1, 25, 9, 'ORDER', 'ACCOMPANY_PLAY', '2026-05-15 20:00:00', 2.0, 300.00, 'MOBA', '钻石', '20:00-22:00', NULL, NULL, 'PENDING', '鍏ㄩ潰娴嬭瘯璁㈠崟1', NULL, NULL, NULL, NULL, '订单编号: ORD20260512332600766', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);
INSERT INTO `customer_order_record` VALUES (18, 16, 16, 26, 12, 'ORDER', 'GAME_TUTORING', '2026-05-16 14:00:00', 2.0, 2000.00, 'SOCIAL', '传奇', '14:00-16:00', NULL, NULL, 'PENDING', '鍏ㄩ潰娴嬭瘯璁㈠崟2', NULL, NULL, NULL, NULL, '订单编号: ORD20260512380500974', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);

-- ----------------------------
-- Table structure for customer_profile
-- ----------------------------
DROP TABLE IF EXISTS `customer_profile`;
CREATE TABLE `customer_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rfm_recency_score` int NULL DEFAULT NULL COMMENT 'RFM最近消费得分',
  `rfm_frequency_score` int NULL DEFAULT NULL COMMENT 'RFM消费频率得分',
  `rfm_monetary_score` int NULL DEFAULT NULL COMMENT 'RFM消费金额得分',
  `rfm_total_score` int NULL DEFAULT NULL COMMENT 'RFM综合得分',
  `rfm_segment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'RFM分群标签：CHAMPION-冠军，LOYAL-忠实，POTENTIAL-潜力，NEW-新客，AT_RISK-风险，HIBERNATE-休眠，LOST-流失',
  `total_orders` int NULL DEFAULT 0 COMMENT '总订单数',
  `total_spent` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '总消费金额',
  `spending_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消费水平（HIGH/MEDIUM/LOW）',
  `avg_order_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '平均订单金额',
  `max_order_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '最大订单金额',
  `spending_trend` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消费趋势：INCREASING-增长，STABLE-稳定，DECREASING-下降',
  `repurchase_rate` decimal(4, 2) NULL DEFAULT NULL COMMENT '复购率',
  `estimated_ltv` decimal(12, 2) NULL DEFAULT NULL COMMENT '预估客户终身价值',
  `avg_service_duration` decimal(10, 2) NULL DEFAULT NULL COMMENT '平均服务时长',
  `last_order_at` datetime NULL DEFAULT NULL COMMENT '最后下单时间',
  `favorite_companion_id` bigint NULL DEFAULT NULL COMMENT '最喜欢的陪玩师ID',
  `favorite_game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最喜欢的游戏类型',
  `game_preferences` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '游戏偏好',
  `preferred_time_slot` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '偏好时段',
  `preferred_companion_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '偏好陪玩师等级',
  `preferred_order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '偏好订单类型',
  `companion_diversity` int NULL DEFAULT 0 COMMENT '陪玩师多样性',
  `first_contact_at` datetime NULL DEFAULT NULL COMMENT '首次接触时间',
  `last_active_at` datetime NULL DEFAULT NULL COMMENT '最后活跃时间',
  `active_days` int NULL DEFAULT 0 COMMENT '活跃天数',
  `total_messages` int NULL DEFAULT 0 COMMENT '总消息数',
  `ai_interaction_count` int NULL DEFAULT 0 COMMENT 'AI交互次数',
  `manual_interaction_count` int NULL DEFAULT 0 COMMENT '人工交互次数',
  `ai_ratio` decimal(4, 2) NULL DEFAULT NULL COMMENT 'AI交互占比',
  `human_handoff_count` int NULL DEFAULT 0 COMMENT '转人工次数',
  `top_handoff_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最常见转人工原因',
  `emotion_trigger_count` int NULL DEFAULT 0 COMMENT '情绪触发次数',
  `order_intent_count` int NULL DEFAULT 0 COMMENT '下单意向次数',
  `satisfaction_score` decimal(3, 2) NULL DEFAULT NULL COMMENT '满意度评分',
  `satisfaction_trend` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '满意度趋势',
  `complaint_count` int NULL DEFAULT 0 COMMENT '投诉次数',
  `refund_count` int NULL DEFAULT 0 COMMENT '退款次数',
  `avg_rating` decimal(3, 2) NULL DEFAULT NULL COMMENT '平均评分',
  `lifecycle_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生命周期阶段：NEW-新客，ACTIVE-活跃，LOYAL-忠实，AT_RISK-流失风险，CHURNED-已流失',
  `member_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '会员等级：NORMAL-普通，BRONZE-铜牌，SILVER-银牌，GOLD-金牌，PLATINUM-白金，DIAMOND-钻石',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '风险等级：LOW-低，MEDIUM-中，HIGH-高',
  `churn_risk_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '流失风险评分',
  `primary_need_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主要需求类型',
  `need_tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求标签',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `assigned_cs_user_id` bigint NULL DEFAULT NULL COMMENT '分配的客服用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_profile_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_customer_profile_stage_active`(`lifecycle_stage` ASC, `last_active_at` ASC) USING BTREE,
  INDEX `idx_customer_profile_rfm_risk`(`rfm_segment` ASC, `risk_level` ASC) USING BTREE,
  INDEX `idx_customer_profile_member_level`(`member_level` ASC) USING BTREE,
  INDEX `idx_customer_profile_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户画像表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_profile
-- ----------------------------
INSERT INTO `customer_profile` VALUES (1, 1, 5, 2, 3, 10, 'POTENTIAL', 2, 900.00, 'HIGH', 450.00, 600.00, 'INCREASING', 1.00, 2700.00, 1.50, '2026-05-08 20:00:00', 1, '三角洲行动', '三角洲行动,CSGO,彩虹六号', '晚上', 'TOP', 'ACCOMPANY', 2, '2026-01-10 10:00:00', '2026-05-10 18:00:00', 122, 350, 280, 70, 0.80, 12, '复杂问题咨询', 2, 15, 5.00, 'STABLE', 0, 0, 5.00, 'ACTIVE', 'BRONZE', 'LOW', 0.00, 'EMOTIONAL', '情感陪伴,上分指导', 'VIP,高消费,忠实', NULL, 3, '2026-01-10 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (2, 2, 4, 4, 4, 12, 'LOYAL', 15, 6750.00, 'MEDIUM', 450.00, 1200.00, 'STABLE', 0.60, 25000.00, 2.00, '2026-05-08 20:00:00', 3, 'FPS', '三角洲行动,绝地求生', '晚上', 'LEVEL_ONE', 'ACCOMPANY', 3, '2026-01-15 14:00:00', '2026-05-08 20:00:00', 95, 280, 220, 60, 0.78, 8, '价格咨询', 1, 8, 4.50, 'STABLE', 1, 0, 4.50, 'LOYAL', 'GOLD', 'LOW', 10.00, '陪玩服务', '陪玩,竞技', '忠实客户', NULL, 3, '2026-01-15 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (3, 3, 2, 5, 2, 9, 'POTENTIAL', 20, 4000.00, 'LOW', 200.00, 500.00, 'DECREASING', 0.40, 8000.00, 1.50, '2026-05-05 15:00:00', 5, 'FPS', '使命召唤,三角洲行动', '下午', 'LEVEL_TWO', 'TEACHING', 4, '2026-02-01 09:00:00', '2026-05-05 15:00:00', 80, 200, 120, 80, 0.60, 15, '人工服务需求', 3, 5, 3.50, 'DECREASING', 2, 1, 3.50, 'AT_RISK', 'SILVER', 'MEDIUM', 55.00, '教学需求', '教学,新手引导', '潜力客户,有流失风险', NULL, 4, '2026-02-01 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (4, 4, 3, 2, 1, 6, 'NEW', 3, 900.00, 'LOW', 300.00, 400.00, 'STABLE', 0.20, 3000.00, 1.00, '2026-05-01 12:00:00', NULL, 'FPS', '三角洲行动', '上午', 'LEVEL_TWO', 'ACCOMPANY', 1, '2026-03-10 16:00:00', '2026-05-01 12:00:00', 45, 80, 70, 10, 0.88, 2, '初次咨询', 0, 2, 4.00, 'INCREASING', 0, 0, 4.00, 'NEW', 'NORMAL', 'LOW', 15.00, '初次体验', '新手', '新客', NULL, NULL, '2026-03-10 16:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (5, 5, 5, 5, 4, 14, 'CHAMPION', 22, 11000.00, 'HIGH', 500.00, 1800.00, 'STABLE', 0.70, 45000.00, 2.50, '2026-05-09 19:00:00', 2, 'FPS', '三角洲行动,CSGO', '晚上', 'TOP', 'ACCOMPANY', 4, '2026-01-20 11:00:00', '2026-05-09 19:00:00', 110, 300, 250, 50, 0.83, 10, '投诉处理', 1, 12, 4.60, 'STABLE', 1, 0, 4.60, 'LOYAL', 'DIAMOND', 'LOW', 3.00, '陪玩服务', '陪玩,竞技,高端', '钻石会员', NULL, 3, '2026-01-20 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (6, 6, 3, 3, 3, 9, 'LOYAL', 10, 4500.00, 'MEDIUM', 450.00, 1000.00, 'STABLE', 0.50, 15000.00, 2.00, '2026-05-07 16:00:00', 4, 'FPS', '三角洲行动,Valorant', '下午', 'LEVEL_ONE', 'ACCOMPANY', 3, '2026-02-10 15:00:00', '2026-05-07 16:00:00', 75, 180, 150, 30, 0.83, 5, '技术问题', 0, 6, 4.20, 'STABLE', 0, 0, 4.20, 'ACTIVE', 'SILVER', 'LOW', 12.00, '陪玩服务', '陪玩,社交', '活跃客户', NULL, 4, '2026-02-10 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (7, 7, 4, 3, 3, 10, 'LOYAL', 12, 4800.00, 'MEDIUM', 400.00, 900.00, 'INCREASING', 0.55, 20000.00, 2.00, '2026-05-06 18:00:00', 1, 'FPS', '三角洲行动,绝地求生', '晚上', 'LEVEL_ONE', 'ACCOMPANY', 3, '2026-02-20 10:00:00', '2026-05-06 18:00:00', 70, 160, 130, 30, 0.81, 6, '预约需求', 1, 7, 4.30, 'INCREASING', 0, 0, 4.30, 'ACTIVE', 'GOLD', 'LOW', 8.00, '陪玩服务', '陪玩,竞技', '增长型客户', NULL, 3, '2026-02-20 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (8, 8, 2, 2, 1, 5, 'NEW', 2, 600.00, 'LOW', 300.00, 350.00, 'STABLE', 0.10, 2000.00, 1.00, '2026-04-20 14:00:00', NULL, 'MOBA', '三角洲行动', '上午', 'LEVEL_TWO', 'ACCOMPANY', 1, '2026-03-15 12:00:00', '2026-04-20 14:00:00', 30, 50, 40, 10, 0.80, 3, '服务咨询', 0, 1, 3.80, 'STABLE', 0, 0, 3.80, 'NEW', 'NORMAL', 'LOW', 20.00, '初次体验', '新手', '新客', NULL, NULL, '2026-03-15 12:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (9, 9, 1, 4, 3, 8, 'AT_RISK', 14, 4200.00, 'MEDIUM', 300.00, 800.00, 'DECREASING', 0.45, 10000.00, 1.50, '2026-04-01 22:00:00', 6, 'FPS', '三角洲行动,战地', '晚上', 'LEVEL_TWO', 'ACCOMPANY', 2, '2026-03-20 20:00:00', '2026-04-01 22:00:00', 50, 120, 90, 30, 0.75, 8, '夜间服务问题', 2, 4, 3.20, 'DECREASING', 1, 1, 3.20, 'AT_RISK', 'BRONZE', 'HIGH', 75.00, '陪玩服务', '夜猫子,社交', '流失风险高', NULL, 4, '2026-03-20 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (10, 10, 3, 1, 2, 6, 'POTENTIAL', 5, 2000.00, 'MEDIUM', 400.00, 600.00, 'STABLE', 0.30, 8000.00, 2.00, '2026-05-03 10:00:00', 2, 'MOBA', '三角洲行动,王者荣耀', '上午', 'LEVEL_ONE', 'TEACHING', 2, '2026-04-01 08:00:00', '2026-05-03 10:00:00', 40, 100, 50, 50, 0.50, 10, '教学指导需求', 1, 3, 3.80, 'STABLE', 0, 0, 3.80, 'ACTIVE', 'NORMAL', 'MEDIUM', 35.00, '教学需求', '教学,战术', '教学型客户', NULL, 3, '2026-04-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (11, 11, 4, 2, 1, 7, 'POTENTIAL', 4, 1600.00, 'MEDIUM', 400.00, 500.00, 'INCREASING', 0.25, 6000.00, 1.50, '2026-05-04 16:00:00', 7, 'SOCIAL', '三角洲行动', '下午', 'LEVEL_ONE', 'SOCIAL', 2, '2026-03-01 14:00:00', '2026-05-04 16:00:00', 55, 90, 70, 20, 0.78, 4, '社交需求', 0, 2, 4.00, 'STABLE', 0, 0, 4.00, 'ACTIVE', 'BRONZE', 'LOW', 18.00, '社交需求', '社交,聊天', '社交型客户', NULL, 3, '2026-03-01 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (12, 12, 2, 2, 3, 7, 'POTENTIAL', 8, 3600.00, 'MEDIUM', 450.00, 800.00, 'STABLE', 0.35, 12000.00, 2.00, '2026-05-05 21:00:00', 8, 'FPS', '三角洲行动,守望先锋', '晚上', 'LEVEL_ONE', 'ACCOMPANY', 2, '2026-04-05 19:00:00', '2026-05-05 21:00:00', 35, 80, 60, 20, 0.75, 5, '颜值区互动', 1, 4, 4.10, 'STABLE', 0, 0, 4.10, 'ACTIVE', 'SILVER', 'LOW', 15.00, '陪玩服务', '陪玩,颜值', '颜值区客户', NULL, 4, '2026-04-05 19:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (13, 13, 1, 1, 1, 3, 'NEW', 1, 300.00, 'LOW', 300.00, 300.00, NULL, 0.05, 1000.00, 1.00, '2026-04-28 15:00:00', NULL, 'SOCIAL', '三角洲行动', '下午', 'LEVEL_TWO', 'ACCOMPANY', 1, '2026-04-10 13:00:00', '2026-04-28 15:00:00', 15, 20, 18, 2, 0.90, 1, '初次咨询', 0, 1, 4.00, NULL, 0, 0, 4.00, 'NEW', 'NORMAL', 'LOW', 10.00, '初次体验', '新手', '新客', NULL, NULL, '2026-04-10 13:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (14, 14, 4, 3, 4, 11, 'LOYAL', 16, 8000.00, 'HIGH', 500.00, 1000.00, 'INCREASING', 0.60, 30000.00, 2.50, '2026-05-08 12:00:00', 3, 'FPS', '三角洲行动,彩虹六号', '上午', 'TOP', 'PACKAGE', 4, '2026-04-15 10:00:00', '2026-05-08 12:00:00', 28, 60, 50, 10, 0.83, 3, '套餐咨询', 0, 5, 4.50, 'INCREASING', 0, 0, 4.50, 'ACTIVE', 'GOLD', 'LOW', 5.00, '套餐服务', '套餐,高端', '套餐型客户', NULL, 3, '2026-04-15 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (15, 15, 1, 5, 1, 7, 'AT_RISK', 25, 2500.00, 'LOW', 100.00, 200.00, 'DECREASING', 0.20, 3000.00, 0.50, '2026-05-01 23:00:00', NULL, 'FPS', '三角洲行动', '晚上', 'LEVEL_TWO', 'ACCOMPANY', 6, '2026-04-20 22:00:00', '2026-05-01 23:00:00', 25, 150, 80, 70, 0.53, 25, '各种问题', 5, 10, 2.50, 'DECREASING', 5, 3, 2.50, 'AT_RISK', 'NORMAL', 'HIGH', 85.00, '投诉类', '投诉,不满', '高风险客户', NULL, NULL, '2026-04-20 22:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (16, 16, 5, 1, 3, 9, 'NEW', 1, 600.00, 'HIGH', 600.00, 600.00, 'INCREASING', 0.50, 1800.00, 2.00, '2026-05-05 08:00:00', 4, '三角洲行动', '三角洲行动,CSGO,APEX', '上午', 'LEVEL_ONE', 'ACCOMPANY', 1, '2026-01-05 09:00:00', '2026-05-10 10:00:00', 127, 400, 320, 80, 0.80, 15, '高端需求', 1, 20, 5.00, 'STABLE', 0, 0, 5.00, 'ACTIVE', 'NORMAL', 'LOW', 0.00, 'SKILL', '技能提升', '顶级客户', NULL, 4, '2026-01-05 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (17, 17, 4, 4, 4, 12, 'LOYAL', 18, 9000.00, 'HIGH', 500.00, 1200.00, 'STABLE', 0.65, 35000.00, 2.00, '2026-05-09 15:00:00', 2, 'MOBA', '三角洲行动,英雄联盟', '下午', 'LEVEL_ONE', 'ACCOMPANY', 3, '2026-01-08 10:00:00', '2026-05-09 15:00:00', 100, 250, 200, 50, 0.80, 8, '技术咨询', 0, 10, 4.40, 'STABLE', 1, 0, 4.40, 'ACTIVE', 'PLATINUM', 'LOW', 6.00, '陪玩服务', '陪玩,MOBA', '活跃客户', NULL, 3, '2026-01-08 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (18, 18, 2, 1, 1, 4, 'NEW', 2, 400.00, 'LOW', 200.00, 250.00, NULL, 0.10, 1500.00, 1.00, '2026-04-10 10:00:00', NULL, 'SOCIAL', '三角洲行动', '上午', 'LEVEL_TWO', 'SOCIAL', 1, '2026-02-28 15:00:00', '2026-04-10 10:00:00', 42, 60, 40, 20, 0.67, 5, '平台咨询', 0, 1, 3.50, NULL, 0, 0, 3.50, 'NEW', 'NORMAL', 'MEDIUM', 30.00, '初次体验', '新手', '待培育', NULL, NULL, '2026-02-28 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (19, 19, 2, 1, 2, 5, 'POTENTIAL', 3, 1500.00, 'MEDIUM', 500.00, 600.00, 'INCREASING', 0.20, 6000.00, 2.00, '2026-05-06 11:00:00', NULL, 'FPS', '三角洲行动', '上午', 'LEVEL_ONE', 'TEACHING', 1, '2026-04-25 11:00:00', '2026-05-06 11:00:00', 18, 40, 35, 5, 0.88, 2, '教学咨询', 0, 2, 4.20, 'INCREASING', 0, 0, 4.20, 'NEW', 'NORMAL', 'LOW', 12.00, '教学需求', '教学,竞技提升', '潜力新客', NULL, NULL, '2026-04-25 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_profile` VALUES (20, 20, 1, 1, 1, 3, 'LOST', 2, 400.00, 'LOW', 200.00, 250.00, 'DECREASING', 0.05, 500.00, 0.50, '2026-02-01 10:00:00', NULL, 'FPS', '三角洲行动', '上午', 'LEVEL_TWO', 'ACCOMPANY', 1, '2026-01-01 08:00:00', '2026-02-01 10:00:00', 30, 15, 12, 3, 0.80, 1, '服务结束', 0, 0, 3.00, 'DECREASING', 0, 0, 3.00, 'CHURNED', 'NORMAL', 'HIGH', 92.00, '无', '', '已流失', NULL, NULL, '2026-01-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for customer_satisfaction
-- ----------------------------
DROP TABLE IF EXISTS `customer_satisfaction`;
CREATE TABLE `customer_satisfaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '客户ID',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联服务追踪ID',
  `related_companion_id` bigint NULL DEFAULT NULL COMMENT '陪玩师ID',
  `satisfaction_score` int NULL DEFAULT NULL COMMENT '评分：1-5',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '反馈内容',
  `feedback_tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签（逗号分隔）',
  `is_anonymous` int NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `service_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复内容',
  `replied_by` bigint NULL DEFAULT NULL COMMENT '回复人ID',
  `replied_at` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cs_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_cs_order_id`(`related_order_id` ASC) USING BTREE,
  INDEX `idx_cs_companion_id`(`related_companion_id` ASC) USING BTREE,
  INDEX `idx_cs_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户满意度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_satisfaction
-- ----------------------------
INSERT INTO `customer_satisfaction` VALUES (1, 1, 1, 1, 5, '非常棒的服务！陪玩师技术一流，态度也很好，下次还来！', '技术好,态度好,准时', 0, 'ACCOMPANY', '感谢您的认可，我们会继续努力提供优质服务！', 3, '2026-05-02 10:00:00', '2026-05-01 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (2, 2, 2, 3, 4, '服务不错，就是价格稍微贵了点，希望能有更多优惠活动', '价格,建议,优惠', 0, 'ACCOMPANY', '感谢您的反馈，我们近期会有优惠活动推出，敬请关注', 3, '2026-04-21 10:00:00', '2026-04-20 22:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (3, 3, 3, 5, 2, '体验不太好，陪玩师迟到半小时，而且技术不如预期', '迟到,技术差,不满', 1, 'TEACHING', '非常抱歉给您带来不好的体验，我们已对该陪玩师进行警告处理，并为您申请补偿', 4, '2026-03-16 09:00:00', '2026-03-15 17:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (4, 5, 4, 2, 5, '每次都找这位陪玩师，从来没让我失望过，强烈推荐！', '推荐,技术好,老客户', 0, 'ACCOMPANY', '感谢您一直以来的支持，我们将继续提升服务质量！', 3, '2026-04-16 10:00:00', '2026-04-15 21:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (5, 6, 5, 4, 4, '整体满意，就是沟通上有些小问题，希望改进', '沟通,改进,满意', 0, 'ACCOMPANY', '感谢您的建议，我们会加强陪玩师的沟通培训', 4, '2026-04-11 10:00:00', '2026-04-10 22:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (6, 7, 6, 1, 5, '顶尖陪玩师就是不一样，实力碾压，学到了很多技巧', '技术好,教学,推荐', 0, 'TEACHING', '很高兴能帮到您，祝您游戏愉快！', 3, '2026-03-21 10:00:00', '2026-03-20 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (7, 9, 7, 6, 1, '太差了！陪玩师一直催单，态度恶劣，要求退款！', '态度差,退款,投诉', 0, 'ACCOMPANY', '非常抱歉给您带来极差的体验，我们已经对该陪玩师进行处理，订单已退款', 4, '2026-03-25 10:00:00', '2026-03-24 23:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (8, 10, 8, 2, 4, '教学内容不错，但感觉时间不太够用，希望能延长一下', '教学,时间,建议', 0, 'TEACHING', '感谢您的建议，我们会考虑调整教学时长方案', 3, '2026-04-26 10:00:00', '2026-04-25 12:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (9, 14, 9, 3, 5, '套餐很划算，性价比高，服务时长也很充足', '套餐,划算,推荐', 0, 'PACKAGE', '感谢您的认可，我们会推出更多优惠套餐！', 3, '2026-05-03 10:00:00', '2026-05-02 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `customer_satisfaction` VALUES (10, 16, 10, 4, 5, '老客户了，服务始终如一的好，值得信赖', '老客户,信赖,推荐', 0, 'ACCOMPANY', '感谢您长期的信任与支持，我们将继续努力！', 4, '2026-05-06 10:00:00', '2026-05-05 10:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for customer_warning_rule
-- ----------------------------
DROP TABLE IF EXISTS `customer_warning_rule`;
CREATE TABLE `customer_warning_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `monitor_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '监控阶段: AT_RISK/CHURNED',
  `trigger_condition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发条件',
  `threshold_value` int NULL DEFAULT 0 COMMENT '条件阈值',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '处理动作',
  `action_params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动作参数JSON',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `priority` int NULL DEFAULT 0 COMMENT '优先级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cwr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户生命周期预警规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_warning_rule
-- ----------------------------
INSERT INTO `customer_warning_rule` VALUES (1, '连续7天无活跃', 'AT_RISK', 'NO_ACTIVITY_DAYS', 7, 'NOTIFY_CS', '{\"template\":\"customer_inactive_warning\"}', 1, 10, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `customer_warning_rule` VALUES (2, '严重流失风险', 'CHURNED', 'NO_ACTIVITY_DAYS', 14, 'SEND_COUPON', '{\"couponId\":1001}', 1, 5, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `customer_warning_rule` VALUES (3, '收到负面评价', 'AT_RISK', 'NEGATIVE_FEEDBACK', 1, 'MARK_VIP', '{\"priority\":\"high\"}', 1, 8, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);

-- ----------------------------
-- Table structure for faq_items
-- ----------------------------
DROP TABLE IF EXISTS `faq_items`;
CREATE TABLE `faq_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '问题分类，如\"服务流程\"、\"价格说明\"',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '答案内容',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号，数值越小越靠前',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_faq_items_category`(`category` ASC) USING BTREE,
  INDEX `idx_faq_items_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_faq_items_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'FAQ知识库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of faq_items
-- ----------------------------
INSERT INTO `faq_items` VALUES (1, '服务流程', '如何预约陪玩服务？', '您可以通过以下方式预约：1.在聊天窗口直接告诉AI客服您的需求 2.AI会自动匹配适合的陪玩师 3.确认时间和价格后下单支付 4.陪玩师准时上线为您服务', 1, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (2, '价格说明', '陪玩师怎么收费？', '陪玩师按等级收费：二品150元/小时、一品300元/小时、顶尖500元/小时、明星1000元/小时。新用户首单享8折，会员更有多重优惠。', 2, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (3, '支付方式', '支持哪些支付方式？', '我们支持微信支付、支付宝、银行卡转账等多种支付方式。支付完成后系统会自动确认并安排服务。', 3, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (4, '退款政策', '如果服务不满意可以退款吗？', '当然可以！我们的退款政策：1.服务开始前可全额退款 2.服务中不满意可按比例退款 3.陪玩师迟到双倍赔偿 4.技术不达标免费重来', 4, 1, '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (5, '会员权益', '会员有什么特权？', '会员等级越高享受越多权益：青铜会员享9折+优先匹配、白银会员享8.5折+专属客服、黄金会员享8折+免费升等、铂金会员享7.5折+私人订制、钻石会员享7折+全部特权', 5, 1, '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (6, '技术支持', '如果遇到技术问题怎么办？', '您可以：1.直接联系在线客服 2.拨打客服电话400-888-0001 3.提交工单，我们会在2小时内响应处理', 6, 1, '2026-02-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (7, '账号管理', '如何修改个人信息？', '请登录系统后进入「个人中心」→「账号设置」即可修改昵称、头像、联系方式等信息。如需修改实名认证信息请联系客服。', 7, 1, '2026-03-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (8, '隐私安全', '个人数据安全吗？', '我们非常重视您的隐私安全！所有数据采用银行级加密存储，严格遵守数据保护法规，未经授权不会向第三方透露您的个人信息。', 8, 1, '2026-03-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (9, '平台支持', '支持哪些平台？', '目前我们支持微信、KOOK、YY等多个主流社交平台，您可以在以上平台直接使用我们的服务。更多平台正在接入中。', 9, 1, '2026-04-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `faq_items` VALUES (10, '合作咨询', '如何成为陪玩师？', '如果您有意向成为陪玩师，请发送简历和游戏战绩截图至 hr@delta.com，我们的HR团队会在3个工作日内与您联系。', 10, 1, '2026-04-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1.0', 'init schema', 'SQL', 'V1.0__init_schema.sql', -1819556898, 'init_production', '2026-05-12 13:26:46', 0, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '1.1', 'add indexes and sync', 'SQL', 'V1.1__add_indexes_and_sync.sql', 76578308, 'init_production', '2026-05-12 13:26:46', 0, 1);

-- ----------------------------
-- Table structure for game_config
-- ----------------------------
DROP TABLE IF EXISTS `game_config`;
CREATE TABLE `game_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '俱乐部配置ID',
  `game_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏名称',
  `game_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏编码',
  `game_icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标URL',
  `game_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `custom_settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自定义设置',
  `game_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'FPS' COMMENT '游戏类型',
  `base_hourly_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '基础时价',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_game_config_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游戏配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_config
-- ----------------------------
INSERT INTO `game_config` VALUES (1, 1, '三角洲行动', 'DELTA_FORCE', 'https://cdn.delta.com/games/delta_force.png', '战术射击游戏，主打团队配合和战术执行', NULL, 'FPS', 200.00, 1, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_config` VALUES (2, 1, 'CSGO', 'CSGO', 'https://cdn.delta.com/games/csgo.png', '经典FPS竞技游戏，全球最火爆的射击游戏之一', NULL, 'FPS', 180.00, 1, 2, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_config` VALUES (3, 1, 'APEX英雄', 'APEX', 'https://cdn.delta.com/games/apex.png', '战术竞技射击游戏，英雄技能与射击结合', NULL, 'FPS', 170.00, 1, 3, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_config` VALUES (4, 2, '绝地求生', 'PUBG', 'https://cdn.delta.com/games/pubg.png', '战术竞技射击游戏，100人大逃杀', NULL, 'FPS', 150.00, 1, 1, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_config` VALUES (5, 3, '使命召唤', 'COD', 'https://cdn.delta.com/games/cod.png', '经典FPS系列作品，战役和多人模式', NULL, 'FPS', 120.00, 1, 1, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_config` VALUES (6, 3, '彩虹六号', 'R6', 'https://cdn.delta.com/games/r6.png', '战术射击游戏，强调破坏和团队配合', NULL, 'FPS', 130.00, 1, 2, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for game_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `game_knowledge`;
CREATE TABLE `game_knowledge`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `game_id` bigint NULL DEFAULT NULL COMMENT '游戏ID',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关键词',
  `reliability` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可靠性评级',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版本',
  `effective_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间',
  `effective_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间',
  `view_count` int NULL DEFAULT 0 COMMENT '查看次数',
  `helpful_count` int NULL DEFAULT 0 COMMENT '有帮助次数',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gk_game_id`(`game_id` ASC) USING BTREE,
  INDEX `idx_gk_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游戏知识库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_knowledge
-- ----------------------------
INSERT INTO `game_knowledge` VALUES (1, 1, '新手入门', '三角洲行动新手快速上手指南', '三角洲行动是一款战术射击游戏，新手建议先完成训练模式...', '官方文档', '新手,入门,指南', '三角洲,新手,入门,教程', 'HIGH', 'v2.5', NULL, NULL, 1500, 320, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (2, 1, '武器配置', '三角洲行动最佳武器搭配方案', '当前版本推荐武器配置：M4A1+红点瞄准镜+扩容弹匣...', '玩家社区', '武器,配置,搭配', '武器,最佳,搭配,方案', 'MEDIUM', 'v2.5', NULL, NULL, 2300, 450, 1, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (3, 1, '战术策略', '三角洲行动高级战术指南', '地图控制、团队配合、信息收集等高级战术策略...', '职业选手', '战术,高级,策略', '战术,高级,策略,指南', 'HIGH', 'v2.5', NULL, NULL, 3200, 680, 1, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (4, 2, '武器配置', 'CSGO武器经济管理指南', '合理分配经济，选择最佳武器配置策略...', '职业教练', '经济,武器,管理', 'CSGO,经济,武器,管理', 'HIGH', 'latest', NULL, NULL, 1800, 390, 1, '2026-02-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (5, 4, '新手入门', '绝地求生零基础攻略', '从跳伞到吃鸡的全流程攻略...', '职业教练', '新手,攻略,教程', '绝地求生,零基础,攻略', 'MEDIUM', 'latest', NULL, NULL, 1200, 250, 1, '2026-03-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (6, 1, '角色技能', '三角洲行动全角色技能详解', '每个角色的技能特点、使用技巧和最佳搭配...', '官方文档', '角色,技能,详解', '角色,技能,详解,全角色', 'HIGH', 'v2.6', NULL, NULL, 1800, 400, 1, '2026-03-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (7, 5, '地图攻略', '使命召唤多地图战术详解', '各张地图的重点位置、掩体和战术路线...', '职业选手', '地图,攻略,战术', '使命召唤,地图,攻略', 'MEDIUM', 'latest', NULL, NULL, 950, 180, 1, '2026-04-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `game_knowledge` VALUES (8, 6, '团队配合', '彩虹六号团队配合战术', '小队配合战术、角色分工和沟通技巧...', '职业教练', '团队,配合,战术', '彩虹六号,团队,配合', 'HIGH', 'latest', NULL, NULL, 780, 160, 1, '2026-04-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for keywords
-- ----------------------------
DROP TABLE IF EXISTS `keywords`;
CREATE TABLE `keywords`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词内容',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关键词分类：TRANSFER-转人工，COMPLAINT-投诉，ORDER-下单，EMERGENCY-紧急',
  `match_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'EXACT' COMMENT '匹配方式：EXACT-精确，FUZZY-模糊，REGEX-正则',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发动作：REPLY-自动回复，TRANSFER-转人工，TAG-标记，ESCALATE-升级',
  `reply_id` bigint NULL DEFAULT NULL COMMENT '关联的自动回复ID',
  `priority` int NULL DEFAULT 0 COMMENT '优先级，数值越大优先级越高',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_keywords_reply_id`(`reply_id` ASC) USING BTREE,
  INDEX `idx_keywords_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_keywords_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关键词触发表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of keywords
-- ----------------------------
INSERT INTO `keywords` VALUES (1, '投诉', 'COMPLAINT', 'FUZZY', 'TRANSFER', NULL, 100, 1, '触发投诉处理流程', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (2, '退款', 'REFUND', 'EXACT', 'TRANSFER', NULL, 100, 1, '触发退款处理流程', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (3, '人工', 'TRANSFER', 'EXACT', 'TRANSFER', NULL, 90, 1, '客户主动要求转人工', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (4, '客服', 'TRANSFER', 'EXACT', 'TRANSFER', NULL, 90, 1, '客户主动要求转人工', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (5, '下单', 'ORDER', 'FUZZY', 'REPLY', 1, 80, 1, '识别下单意向', '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (6, '预约', 'ORDER', 'EXACT', 'REPLY', 2, 80, 1, '识别预约意向', '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (7, '价格', 'ORDER', 'EXACT', 'REPLY', 3, 70, 1, '价格咨询', '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (8, '多少钱', 'ORDER', 'FUZZY', 'REPLY', 3, 70, 1, '价格咨询', '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (9, '紧急', 'EMERGENCY', 'EXACT', 'ESCALATE', NULL, 95, 1, '紧急情况立即升级', '2026-02-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (10, '死妈', 'EMERGENCY', 'EXACT', 'ESCALATE', NULL, 100, 1, '敏感词监控', '2026-03-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (11, '开挂', 'COMPLAINT', 'EXACT', 'TAG', NULL, 60, 1, '标记为违规问题', '2026-03-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `keywords` VALUES (12, '代打', 'COMPLAINT', 'EXACT', 'TAG', NULL, 60, 1, '标记为违规问题', '2026-03-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `direction` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息方向：in-接收（客户发送），out-发出（系统/客服发送）',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `content_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '内容类型：text-文本，image-图片等',
  `is_ai` tinyint(1) NULL DEFAULT 0 COMMENT '是否AI回复',
  `ai_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI模型名称',
  `ai_token_count` int NULL DEFAULT NULL COMMENT 'AI消耗Token数',
  `ai_response_time_ms` int NULL DEFAULT NULL COMMENT 'AI响应时间（毫秒）',
  `keyword_triggered` tinyint(1) NULL DEFAULT 0 COMMENT '是否触发关键词',
  `triggered_keyword` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发的关键词',
  `cs_user_id` bigint NULL DEFAULT NULL COMMENT '客服用户ID',
  `emotion_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '情绪标签',
  `intent_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '意图标签',
  `read_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'unread' COMMENT '阅读状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`, `created_at`) USING BTREE,
  INDEX `idx_messages_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_messages_user_id_created`(`user_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_messages_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_messages_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic PARTITION BY RANGE (to_days(`created_at`))
PARTITIONS 16
(PARTITION `p202510` VALUES LESS THAN (739921) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202511` VALUES LESS THAN (739951) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202512` VALUES LESS THAN (739982) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202601` VALUES LESS THAN (740013) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202602` VALUES LESS THAN (740041) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202603` VALUES LESS THAN (740072) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202604` VALUES LESS THAN (740102) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202605` VALUES LESS THAN (740133) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202606` VALUES LESS THAN (740163) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202607` VALUES LESS THAN (740194) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202608` VALUES LESS THAN (740225) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202609` VALUES LESS THAN (740255) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202610` VALUES LESS THAN (740286) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202611` VALUES LESS THAN (740316) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p202612` VALUES LESS THAN (740347) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (17, 3, 3, 'in', '我要投诉！今晚的陪玩师迟到了半天，技术还不行！', 'text', 0, NULL, NULL, NULL, 1, '投诉', NULL, 'NEGATIVE', 'COMPLAINT', 'read', '2026-03-15 16:30:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (18, 3, 3, 'out', '非常抱歉给您带来了不好的体验！我已记录您的投诉内容，请稍等，我马上为您转接人工客服处理。', 'text', 1, 'GPT-4o', 58, 900, 1, '投诉', NULL, 'CONCERN', 'COMPLAINT_HANDLE', 'read', '2026-03-15 16:30:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (19, 3, 3, 'out', '您好，我是客服王五，已收到您的投诉。请问能详细描述一下情况吗？', 'text', 0, NULL, NULL, NULL, 0, NULL, 4, 'NEUTRAL', 'FOLLOW_UP', 'read', '2026-03-15 16:31:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (20, 3, 3, 'in', '约的下午3点，他3点15才上线，而且打得很菜，完全不值这个价！', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEGATIVE', 'COMPLAINT_DETAIL', 'read', '2026-03-15 16:32:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (34, 6, 7, 'in', '想学三角洲行动，有没有教学服务？', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'TEACHING_QUERY', 'read', '2026-03-20 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (35, 6, 7, 'out', '当然有！我们提供专业的游戏教学服务，涵盖基础操作到高级战术。顶尖教练500元/小时，一品教练300元/小时。', 'text', 1, 'GPT-4o', 68, 1100, 0, NULL, NULL, 'NEUTRAL', 'TEACHING_INFO', 'read', '2026-03-20 15:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (36, 6, 7, 'in', '直接上顶尖的吧，我要学就要学最好的', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'LEVEL_SELECT', 'read', '2026-03-20 15:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (37, 6, 7, 'out', '好的！为您推荐浩神带你飞，前职业选手，擅长教学。2小时教学1000元。已帮您预约3月20日18:00-20:00。', 'text', 1, 'GPT-4o', 72, 1200, 0, NULL, NULL, 'POSITIVE', 'BOOKING_CONFIRM', 'read', '2026-03-20 15:02:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (38, 6, 7, 'in', '已支付，期待！', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'PAYMENT', 'read', '2026-03-20 15:05:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (39, 7, 9, 'in', '气死我了！这什么陪玩师啊！催个不停！', 'text', 0, NULL, NULL, NULL, 1, '投诉', NULL, 'ANGRY', 'COMPLAINT', 'read', '2026-03-24 22:30:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (40, 7, 9, 'out', '非常抱歉让您遇到这种情况！我理解您的愤怒，这绝对不是我们应有的服务标准。立即为您转接高级客服处理。', 'text', 1, 'Claude-3.5', 65, 1000, 1, '投诉', NULL, 'APOLOGETIC', 'COMPLAINT_HANDLE', 'read', '2026-03-24 22:30:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (41, 7, 9, 'out', '您好，我是客服王五。非常重视您的投诉，请详细说明情况，我们会第一时间处理。', 'text', 0, NULL, NULL, NULL, 0, NULL, 4, 'CONCERN', 'FOLLOW_UP', 'read', '2026-03-24 22:31:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (42, 7, 9, 'in', '才打了不到一小时就一直催说时间快到了，态度特别差！', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'ANGRY', 'COMPLAINT_DETAIL', 'read', '2026-03-24 22:32:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (43, 7, 9, 'out', '我已了解全部情况，这确实是我们陪玩师的严重失职。我们决定：1.全额退款150元 2.对涉事陪玩师停权处理 3.补偿您一张免单券。您看可以吗？', 'text', 0, NULL, NULL, NULL, 0, NULL, 4, 'NEUTRAL', 'SOLUTION', 'read', '2026-03-24 22:35:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (44, 7, 9, 'in', '行吧，退款快点到账', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'ACCEPT', 'read', '2026-03-24 22:38:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (11, 2, 2, 'in', '我想了解一下陪玩的价格', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'PRICE_QUERY', 'read', '2026-04-20 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (12, 2, 2, 'out', '您好！我们的陪玩价格根据等级不同：二品150元/小时，一品300元/小时，顶尖500元/小时，明星1000元/小时。新用户首单还有8折优惠哦！', 'text', 1, 'GPT-4o', 80, 1000, 0, NULL, NULL, 'NEUTRAL', 'PRICE_INFO', 'read', '2026-04-20 14:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (13, 2, 2, 'in', '一品300可以接受，帮我预约一个吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'BOOKING', 'read', '2026-04-20 14:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (14, 2, 2, 'out', '好的，为您转接人工客服确认具体时间和陪玩师安排。', 'text', 1, 'GPT-4o', 35, 600, 0, NULL, NULL, 'NEUTRAL', 'TRANSFER', 'read', '2026-04-20 14:02:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (15, 2, 2, 'out', '您好，我是您的专属客服李四，请问您方便什么时间呢？', 'text', 0, NULL, NULL, NULL, 0, NULL, 3, 'NEUTRAL', 'GREETING', 'read', '2026-04-20 14:03:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (16, 2, 2, 'in', '今晚7点到8点半可以吗', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'TIME_SELECT', 'read', '2026-04-20 14:04:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (21, 4, 5, 'in', '帮我安排美美陪玩，今晚8点到10点', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'BOOKING', 'read', '2026-04-15 16:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (22, 4, 5, 'out', '晚上好！美美陪玩今晚8-10点正好有空，顶尖级别500元/小时，共1000元。您是钻石会员享9折，只需900元。', 'text', 1, 'Claude-3.5', 75, 1300, 0, NULL, NULL, 'POSITIVE', 'BOOKING_CONFIRM', 'read', '2026-04-15 16:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (23, 4, 5, 'in', '直接下单吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'CONFIRM', 'read', '2026-04-15 16:01:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (24, 4, 5, 'out', '好的，已为您下单。订单ORD-20260415-004已生成，美美陪玩将准时为您服务！', 'text', 1, 'Claude-3.5', 52, 800, 0, NULL, NULL, 'POSITIVE', 'ORDER_CREATED', 'read', '2026-04-15 16:01:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (25, 4, 5, 'in', '谢谢', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'THANKS', 'read', '2026-04-15 16:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (26, 5, 6, 'in', '嗨，KOOK平台能用吗？想约个陪玩', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'INQUIRY', 'read', '2026-04-10 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (27, 5, 6, 'out', '当然可以！KOOK平台完全支持，我们提供和微信端相同的高品质服务。请问您想预约什么游戏和时间呢？', 'text', 1, 'GPT-4o', 55, 950, 0, NULL, NULL, 'NEUTRAL', 'SERVICE_INFO', 'read', '2026-04-10 12:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (28, 5, 6, 'in', '三角洲行动，下午3点可以吗', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'BOOKING', 'read', '2026-04-10 12:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (29, 5, 6, 'out', '下午3点可以的，请问您偏好哪个等级的陪玩师？', 'text', 1, 'GPT-4o', 40, 800, 0, NULL, NULL, 'NEUTRAL', 'LEVEL_QUERY', 'read', '2026-04-10 12:02:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (30, 5, 6, 'in', '一品吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'LEVEL_SELECT', 'read', '2026-04-10 12:03:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (31, 5, 6, 'out', '一品陪玩师300元/小时。当前可预约的有小龙大魔王和帅哥陪玩。为您转接人工确认。', 'text', 1, 'GPT-4o', 60, 1000, 0, NULL, NULL, 'NEUTRAL', 'TRANSFER', 'read', '2026-04-10 12:03:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (32, 5, 6, 'out', '您好，我是客服王五。小龙大魔王下午3点有空，技术非常好，KD4.5钻石段位，为您安排他吧？', 'text', 0, NULL, NULL, NULL, 0, NULL, 4, 'NEUTRAL', 'RECOMMEND', 'read', '2026-04-10 12:04:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (33, 5, 6, 'in', '好的，下单吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'CONFIRM', 'read', '2026-04-10 12:05:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (45, 8, 10, 'in', '预约明天上午10点的教学服务', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'BOOKING', 'read', '2026-04-24 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (46, 8, 10, 'out', '您好！明天上午10点可预约的教学导师有美美陪玩（一品/300元/小时）。请问您需要多长时间？', 'text', 1, 'GPT-4o', 58, 900, 0, NULL, NULL, 'NEUTRAL', 'BOOKING_INFO', 'read', '2026-04-24 10:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (47, 8, 10, 'in', '1.5小时吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'DURATION', 'read', '2026-04-24 10:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (48, 8, 10, 'out', '已为您预约美美陪玩，4月25日10:00-11:30，费用450元。已下单，请完成支付。', 'text', 1, 'GPT-4o', 55, 850, 0, NULL, NULL, 'NEUTRAL', 'ORDER_CREATED', 'read', '2026-04-24 10:02:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (1, 1, 1, 'in', '你好，我想预约今晚的陪玩服务', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'BOOKING', 'read', '2026-05-01 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (2, 1, 1, 'out', '您好！欢迎咨询三角洲陪玩服务。请问您想预约什么时间段的呢？我们有以下时段可选：晚上18:00-20:00、20:00-22:00', 'text', 1, 'GPT-4o', 85, 1200, 0, NULL, NULL, 'NEUTRAL', 'QUERY', 'read', '2026-05-01 15:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (3, 1, 1, 'in', '晚上18:00-20:00吧，想找顶尖级别的陪玩师', 'text', 0, NULL, NULL, NULL, 1, '顶尖', NULL, 'POSITIVE', 'BOOKING', 'read', '2026-05-01 15:01:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (4, 1, 1, 'out', '好的，顶尖陪玩师每小时500元，2小时共1000元。当前可用的顶尖陪玩师有：浩神带你飞和大力出奇迹。请问您有偏好吗？', 'text', 1, 'GPT-4o', 92, 1500, 0, NULL, NULL, 'NEUTRAL', 'RECOMMEND', 'read', '2026-05-01 15:01:02', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (5, 1, 1, 'in', '选浩神吧，听说他很厉害', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'SELECT', 'read', '2026-05-01 15:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (6, 1, 1, 'out', '太棒了！已为您预约浩神带你飞，服务时间：5月1日18:00-20:00，费用1000元。请确认下单并完成支付。', 'text', 1, 'GPT-4o', 78, 1100, 0, NULL, NULL, 'POSITIVE', 'CONFIRM', 'read', '2026-05-01 15:02:02', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (7, 1, 1, 'in', '好的，我已支付，谢谢', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'PAYMENT', 'read', '2026-05-01 15:05:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (8, 1, 1, 'out', '支付成功！您的订单已确认。浩神带你飞将在18:00准时上线为您服务。如有任何问题可随时联系我们。祝您游戏愉快！', 'text', 1, 'GPT-4o', 65, 900, 0, NULL, NULL, 'POSITIVE', 'CONFIRM', 'read', '2026-05-01 15:05:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (9, 1, 1, 'out', '您好，您的陪玩服务即将开始，浩神带你飞已上线等候，请准备好开始游戏！', 'text', 1, 'GPT-4o', 48, 800, 0, NULL, NULL, 'NEUTRAL', 'REMINDER', 'read', '2026-05-01 17:55:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (10, 1, 1, 'in', '来了来了，准备进游戏！', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'RESPONSE', 'read', '2026-05-01 17:58:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (49, 9, 14, 'in', '你们那个套餐怎么算的？', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'PACKAGE_QUERY', 'read', '2026-05-02 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (50, 9, 14, 'out', '我们提供多种套餐！目前最受欢迎的3小时套餐1500元（含顶尖陪玩师），比按小时购买便宜300元。还有新客尝鲜套餐等。', 'text', 1, 'GPT-4o', 72, 1100, 0, NULL, NULL, 'NEUTRAL', 'PACKAGE_INFO', 'read', '2026-05-02 08:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (51, 9, 14, 'in', '3小时套餐不错，就这个吧', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'SELECT', 'read', '2026-05-02 08:02:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (52, 9, 14, 'out', '好的！已为您安排大力出奇迹，5月2日10:00-13:00，套餐价1500元，您是金牌会员享9折，实付1350元。', 'text', 1, 'GPT-4o', 65, 950, 0, NULL, NULL, 'POSITIVE', 'BOOKING_CONFIRM', 'read', '2026-05-02 08:02:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (53, 9, 14, 'in', '已付，谢谢', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'PAYMENT', 'read', '2026-05-02 08:05:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (54, 10, 16, 'in', '我是老客户了，想约小龙大魔王明天早上8点', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'BOOKING', 'read', '2026-05-05 06:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (55, 10, 16, 'out', '测试用户A您好！识别到您是钻石会员。小龙大魔王明天8:00-10:00有空，一品级别300元/小时，2小时共600元。', 'text', 1, 'Claude-3.5', 78, 1300, 0, NULL, NULL, 'NEUTRAL', 'BOOKING_INFO', 'read', '2026-05-05 06:00:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (56, 10, 16, 'in', '就他了，下单', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'CONFIRM', 'read', '2026-05-05 06:01:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (57, 10, 16, 'out', '订单已生成ORD-20260505-010，小龙大魔王明早8点准时为您服务。祝您游戏愉快！', 'text', 1, 'Claude-3.5', 50, 800, 0, NULL, NULL, 'POSITIVE', 'ORDER_CREATED', 'read', '2026-05-05 06:01:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (58, 10, 16, 'in', '最近有什么新活动吗？', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'NEUTRAL', 'PROMOTION_QUERY', 'read', '2026-05-05 06:03:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (59, 10, 16, 'out', '有的！目前正在进行五一大促活动，会员充值满1000送200，还有星河行动裂变活动推荐好友各得100元代金券。', 'text', 1, 'Claude-3.5', 68, 1000, 0, NULL, NULL, 'NEUTRAL', 'PROMOTION_INFO', 'read', '2026-05-05 06:03:01', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `messages` VALUES (60, 10, 16, 'in', '不错，回头参加一下', 'text', 0, NULL, NULL, NULL, 0, NULL, NULL, 'POSITIVE', 'INTEREST', 'read', '2026-05-05 06:04:00', '2026-05-12 02:04:10', 0, NULL);

-- ----------------------------
-- Table structure for messages_archive
-- ----------------------------
DROP TABLE IF EXISTS `messages_archive`;
CREATE TABLE `messages_archive`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息原始ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '会话ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `direction` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息方向',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容类型',
  `is_ai` tinyint(1) NULL DEFAULT NULL COMMENT '是否AI回复',
  `ai_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI模型',
  `ai_token_count` int NULL DEFAULT NULL COMMENT 'Token消耗',
  `ai_response_time_ms` int NULL DEFAULT NULL COMMENT '响应时间',
  `keyword_triggered` tinyint(1) NULL DEFAULT NULL COMMENT '是否触发关键词',
  `triggered_keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发的关键词',
  `cs_user_id` bigint NULL DEFAULT NULL COMMENT '客服用户ID',
  `emotion_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '情绪标签',
  `intent_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '意图标签',
  `read_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '阅读状态',
  `archived_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  `created_at` datetime NOT NULL COMMENT '原始创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '原始更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`, `archived_at`) USING BTREE,
  INDEX `idx_archive_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_archive_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_archive_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_archive_archived_at`(`archived_at` ASC) USING BTREE,
  INDEX `idx_archive_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息归档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages_archive
-- ----------------------------

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operation_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `operation_target` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作目标',
  `operation_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作内容',
  `operation_result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作结果',
  `affected_rows` int NULL DEFAULT NULL COMMENT '影响行数',
  `duration_ms` int NULL DEFAULT NULL COMMENT '耗时（毫秒）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `rollback_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回滚信息',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_logs_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------
INSERT INTO `operation_logs` VALUES (1, '2026-05-01 09:00:00', 'admin', 'LOGIN', '系统登录', '管理员登录系统', 'SUCCESS', 0, 120, NULL, NULL, NULL, '2026-05-01 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (2, '2026-05-01 09:30:00', 'admin', 'CREATE', '系统用户', '创建客服人员李四', 'SUCCESS', 1, 45, NULL, NULL, NULL, '2026-05-01 09:30:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (3, '2026-05-02 10:00:00', 'zhangsan', 'UPDATE', '客户信息', '修改客户VIP等级为金牌', 'SUCCESS', 1, 32, NULL, NULL, NULL, '2026-05-02 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (4, '2026-05-02 14:00:00', 'lisi', 'ASSIGN', '工单分配', '将工单WO-20260502-001分配给王五', 'SUCCESS', 1, 15, NULL, NULL, NULL, '2026-05-02 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (5, '2026-05-03 11:00:00', 'admin', 'CONFIG', 'AI配置', '修改AI回复温度参数为0.8', 'SUCCESS', 1, 28, NULL, NULL, NULL, '2026-05-03 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (6, '2026-05-03 16:00:00', 'wangwu', 'DELETE', '待处理消息', '删除已处理的待办消息', 'SUCCESS', 1, 10, NULL, NULL, NULL, '2026-05-03 16:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (7, '2026-05-04 09:00:00', 'zhangsan', 'EXPORT', '客户数据', '导出本月客户活跃度报表', 'SUCCESS', 150, 2300, NULL, NULL, NULL, '2026-05-04 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (8, '2026-05-04 15:00:00', 'admin', 'IMPORT', '陪玩师数据', '批量导入陪玩师信息', 'SUCCESS', 5, 1200, NULL, NULL, NULL, '2026-05-04 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (9, '2026-05-05 10:00:00', 'lisi', 'REPLY', '客户满意度', '回复客户差评并承诺改进', 'SUCCESS', 1, 180, NULL, NULL, NULL, '2026-05-05 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `operation_logs` VALUES (10, '2026-05-05 17:00:00', 'zhangsan', 'AUDIT', '质检记录', '审核服务质量检测结果', 'SUCCESS', 3, 600, NULL, NULL, NULL, '2026-05-05 17:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE `order_status_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `from_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更前状态',
  `to_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变更后状态',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operator_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人角色',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_osh_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_osh_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_osh_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单状态变更历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_history
-- ----------------------------
INSERT INTO `order_status_history` VALUES (4, 24, NULL, 'PENDING', 1, '系统', 'SYSTEM', '订单创建', '2026-05-12 11:48:52');
INSERT INTO `order_status_history` VALUES (5, 25, NULL, 'PENDING', 1, '系统', 'SYSTEM', '订单创建', '2026-05-12 11:50:34');
INSERT INTO `order_status_history` VALUES (6, 26, NULL, 'PENDING', 16, '系统', 'SYSTEM', '订单创建', '2026-05-12 11:50:34');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '下单客户ID',
  `companion_id` bigint NULL DEFAULT NULL COMMENT '陪玩师ID',
  `companion_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '陪玩师名称',
  `service_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `game_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型',
  `price_rule_id` bigint NULL DEFAULT NULL COMMENT '价格规则ID',
  `order_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待确认，CONFIRMED-已确认，IN_PROGRESS-进行中，COMPLETED-已完成，PENDING_REVIEW-待评价，CANCELLED-已取消，REFUNDED-已退款，ABNORMAL-异常，ARCHIVED-已归档',
  `scheduled_start` datetime NULL DEFAULT NULL COMMENT '预约开始时间',
  `scheduled_end` datetime NULL DEFAULT NULL COMMENT '预约结束时间',
  `actual_start` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `actual_end` datetime NULL DEFAULT NULL COMMENT '实际结束时间',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '服务时长（分钟）',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `paid_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '实付金额',
  `payment_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付，PARTIAL-部分支付，PAID-已支付',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `transaction_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付流水号',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `work_order_id` bigint NULL DEFAULT NULL COMMENT '关联工单ID',
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `time_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时间选择方式',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消/拒单原因',
  `schedule_id` bigint NULL DEFAULT NULL COMMENT '关联排班记录ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_orders_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_orders_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_orders_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_orders_status_created`(`order_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_orders_transaction_id`(`transaction_id` ASC) USING BTREE,
  INDEX `idx_orders_status_payment_created`(`order_status` ASC, `payment_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_orders_user_id_created`(`user_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_orders_companion_status`(`companion_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `idx_orders_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_orders_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_orders_schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_orders_status_deleted`(`order_status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_orders_user_status`(`user_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `idx_orders_payment_status`(`payment_status` ASC) USING BTREE,
  INDEX `idx_orders_created_status`(`created_at` ASC, `order_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD-20260501-001', 1, 1, '浩神带你飞', 'ACCOMPANY', NULL, 5, 'COMPLETED', '2026-05-01 18:00:00', '2026-05-01 20:00:00', '2026-05-01 18:00:00', '2026-05-01 20:00:00', 120, 1000.00, 1000.00, 'PAID', 'WECHAT', 'TXN_ORD_001', '2026-05-01 17:30:00', NULL, '回头客', NULL, NULL, NULL, '2026-05-01 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (2, 'ORD-20260420-002', 2, 3, '大力出奇迹', 'ACCOMPANY', NULL, 2, 'PENDING_REVIEW', '2026-04-20 19:00:00', '2026-04-20 20:30:00', '2026-04-20 19:00:00', '2026-04-20 20:30:00', 90, 450.00, 450.00, 'PAID', 'ALIPAY', 'TXN_ORD_002', '2026-04-20 18:30:00', NULL, '待评价', NULL, NULL, NULL, '2026-04-20 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (3, 'ORD-20260315-003', 3, 5, '星星点灯', 'TEACHING', NULL, 7, 'REFUNDED', '2026-03-15 15:00:00', '2026-03-15 16:00:00', '2026-03-15 15:15:00', '2026-03-15 16:00:00', 45, 200.00, 200.00, 'PAID', 'WECHAT', 'TXN_ORD_003', '2026-03-15 14:30:00', 1, '陪玩师迟到退款', NULL, NULL, NULL, '2026-03-15 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (4, 'ORD-20260415-004', 5, 2, '美美陪玩', 'ACCOMPANY', NULL, 6, 'COMPLETED', '2026-04-15 20:00:00', '2026-04-15 22:00:00', '2026-04-15 20:00:00', '2026-04-15 22:00:00', 120, 1000.00, 1000.00, 'PAID', 'WECHAT', 'TXN_ORD_004', '2026-04-15 19:00:00', NULL, NULL, NULL, NULL, NULL, '2026-04-15 16:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (5, 'ORD-20260410-005', 6, 4, '小龙大魔王', 'ACCOMPANY', NULL, 2, 'COMPLETED', '2026-04-10 15:00:00', '2026-04-10 16:30:00', '2026-04-10 15:00:00', '2026-04-10 16:30:00', 90, 450.00, 450.00, 'PAID', 'ALIPAY', 'TXN_ORD_005', '2026-04-10 14:30:00', NULL, NULL, NULL, NULL, NULL, '2026-04-10 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (6, 'ORD-20260320-006', 7, 1, '浩神带你飞', 'TEACHING', NULL, 8, 'COMPLETED', '2026-03-20 18:00:00', '2026-03-20 20:00:00', '2026-03-20 18:00:00', '2026-03-20 20:00:00', 120, 1000.00, 1000.00, 'PAID', 'WECHAT', 'TXN_ORD_006', '2026-03-20 17:00:00', NULL, NULL, NULL, NULL, NULL, '2026-03-20 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (7, 'ORD-20260324-007', 9, 6, '天哥娱乐', 'ACCOMPANY', NULL, 1, 'REFUNDED', '2026-03-24 21:00:00', '2026-03-24 22:00:00', '2026-03-24 21:00:00', '2026-03-24 21:45:00', 45, 150.00, 150.00, 'PAID', 'WECHAT', 'TXN_ORD_007', '2026-03-24 20:30:00', 3, '客户投诉已退款', NULL, NULL, NULL, '2026-03-24 19:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (8, 'ORD-20260425-008', 10, 2, '美美陪玩', 'TEACHING', NULL, 7, 'COMPLETED', '2026-04-25 10:00:00', '2026-04-25 11:30:00', '2026-04-25 10:00:00', '2026-04-25 11:30:00', 90, 450.00, 450.00, 'PAID', 'ALIPAY', 'TXN_ORD_008', '2026-04-25 09:30:00', NULL, NULL, NULL, NULL, NULL, '2026-04-25 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (9, 'ORD-20260502-009', 14, 3, '大力出奇迹', 'PACKAGE', NULL, 3, 'COMPLETED', '2026-05-02 10:00:00', '2026-05-02 13:00:00', '2026-05-02 10:00:00', '2026-05-02 13:00:00', 180, 1500.00, 1440.00, 'PAID', 'WECHAT', 'TXN_ORD_009', '2026-05-02 09:30:00', NULL, '会员折扣', NULL, NULL, NULL, '2026-05-02 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (10, 'ORD-20260505-010', 16, 4, '小龙大魔王', 'ACCOMPANY', NULL, 2, 'COMPLETED', '2026-05-05 08:00:00', '2026-05-05 10:00:00', '2026-05-05 08:00:00', '2026-05-05 10:00:00', 120, 600.00, 600.00, 'PAID', 'ALIPAY', 'TXN_ORD_010', '2026-05-05 07:30:00', NULL, NULL, NULL, NULL, NULL, '2026-05-05 06:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (11, 'ORD-20260512-011', 1, 1, '浩神带你飞', 'ACCOMPANY', NULL, 5, 'CANCELLED', '2026-05-12 20:00:00', '2026-05-12 22:00:00', NULL, NULL, NULL, 1000.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '待支付 | 系统自动取消：超过30分钟未支付确认', NULL, NULL, NULL, '2026-05-12 10:00:00', '2026-05-12 10:33:57', 0, NULL);
INSERT INTO `orders` VALUES (12, 'ORD-20260512-012', 7, NULL, NULL, 'ACCOMPANY', NULL, NULL, 'CANCELLED', '2026-05-13 18:00:00', '2026-05-13 20:00:00', NULL, NULL, NULL, 300.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '待匹配陪玩师 | 系统自动取消：超过30分钟未支付确认', NULL, NULL, NULL, '2026-05-12 09:00:00', '2026-05-12 10:33:57', 0, NULL);
INSERT INTO `orders` VALUES (13, 'ORD-20260509-013', 17, 2, '美美陪玩', 'ACCOMPANY', NULL, 6, 'CONFIRMED', '2026-05-09 15:00:00', '2026-05-09 17:00:00', NULL, NULL, NULL, 1000.00, 1000.00, 'PAID', 'WECHAT', 'TXN_ORD_013', '2026-05-09 14:00:00', NULL, '已确认待开始', NULL, NULL, NULL, '2026-05-09 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (14, 'ORD-20260512-014', 16, 10, '明星杨杨', 'ACCOMPANY', NULL, 6, 'IN_PROGRESS', '2026-05-12 14:00:00', '2026-05-12 16:00:00', '2026-05-12 14:00:00', NULL, NULL, 2000.00, 2000.00, 'PAID', 'BANK', 'TXN_ORD_014', '2026-05-12 13:00:00', NULL, '进行中', NULL, NULL, NULL, '2026-05-12 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (15, 'ORD-20260501-015', 15, 5, '星星点灯', 'ACCOMPANY', NULL, 1, 'ABNORMAL', '2026-05-01 22:00:00', '2026-05-01 23:00:00', '2026-05-01 22:00:00', '2026-05-01 22:15:00', 15, 100.00, 100.00, 'PAID', 'WECHAT', 'TXN_ORD_015', '2026-05-01 21:30:00', 5, '客户中途退出', NULL, NULL, NULL, '2026-05-01 20:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (16, 'ORD-20260105-016', 16, 4, '小龙大魔王', 'ACCOMPANY', NULL, 2, 'ARCHIVED', '2026-01-10 10:00:00', '2026-01-10 12:00:00', '2026-01-10 10:00:00', '2026-01-10 12:00:00', 120, 600.00, 600.00, 'PAID', 'WECHAT', 'TXN_ORD_016', '2026-01-10 09:00:00', NULL, '历史订单已归档', NULL, NULL, NULL, '2026-01-05 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (17, 'ORD-20260508-017', 1, 2, '美美陪玩', 'ACCOMPANY', NULL, 5, 'COMPLETED', '2026-05-08 20:00:00', '2026-05-08 21:00:00', '2026-05-08 20:00:00', '2026-05-08 21:00:00', 60, 500.00, 500.00, 'PAID', 'WECHAT', 'TXN_ORD_017', '2026-05-08 19:30:00', NULL, NULL, NULL, NULL, NULL, '2026-05-08 18:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (18, 'ORD-20260510-018', 16, 12, '独秀同志', 'SOCIAL', NULL, 10, 'CANCELLED', '2026-05-10 20:00:00', '2026-05-10 22:00:00', NULL, NULL, NULL, 2000.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '客户主动取消', NULL, NULL, NULL, '2026-05-10 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (19, 'ORD-20260506-019', 7, 1, '浩神带你飞', 'ACCOMPANY', NULL, 5, 'COMPLETED', '2026-05-06 15:00:00', '2026-05-06 17:30:00', '2026-05-06 15:00:00', '2026-05-06 17:30:00', 150, 1250.00, 1250.00, 'PAID', 'WECHAT', 'TXN_ORD_019', '2026-05-06 14:00:00', NULL, NULL, NULL, NULL, NULL, '2026-05-06 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `orders` VALUES (20, 'ORD-20260511-020', 19, NULL, NULL, 'TEACHING', NULL, NULL, 'CANCELLED', '2026-05-13 10:00:00', '2026-05-13 12:00:00', NULL, NULL, NULL, 600.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '新客户首次下单 | 系统自动取消：超过30分钟未支付确认', NULL, NULL, NULL, '2026-05-11 15:00:00', '2026-05-12 10:33:53', 0, NULL);
INSERT INTO `orders` VALUES (24, 'ORD20260512717000272', 1, 9, '技术刘老师', 'ACCOMPANY_PLAY', 'MOBA', NULL, 'CANCELLED', '2026-05-14 20:00:00', '2026-05-14 22:00:00', NULL, NULL, 120, 300.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, 'API楠岃瘉娴嬭瘯璁㈠崟 | 系统自动取消：超过30分钟未支付确认', 'SYSTEM', NULL, NULL, '2026-05-12 11:48:51', '2026-05-12 12:19:08', 0, NULL);
INSERT INTO `orders` VALUES (25, 'ORD20260512332600766', 1, 9, '技术刘老师', 'ACCOMPANY_PLAY', 'MOBA', NULL, 'CANCELLED', '2026-05-15 20:00:00', '2026-05-15 22:00:00', NULL, NULL, 120, 300.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '鍏ㄩ潰娴嬭瘯璁㈠崟1 | 系统自动取消：超过30分钟未支付确认', 'SYSTEM', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 12:21:07', 0, NULL);
INSERT INTO `orders` VALUES (26, 'ORD20260512380500974', 16, 12, '独秀同志', 'GAME_TUTORING', 'SOCIAL', NULL, 'CANCELLED', '2026-05-16 14:00:00', '2026-05-16 16:00:00', NULL, NULL, 120, 2000.00, 0.00, 'UNPAID', NULL, NULL, NULL, NULL, '鍏ㄩ潰娴嬭瘯璁㈠崟2 | 系统自动取消：超过30分钟未支付确认', 'MANUAL', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 12:21:07', 0, NULL);

-- ----------------------------
-- Table structure for pending_messages
-- ----------------------------
DROP TABLE IF EXISTS `pending_messages`;
CREATE TABLE `pending_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '客户ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平台',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `content_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '内容类型',
  `pending_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '待处理原因',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '优先级：NORMAL-普通，URGENT-紧急，CRITICAL-特急',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '处理状态',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `escalation_level` int NULL DEFAULT 0 COMMENT '升级级别',
  `assigned_cs_user_id` bigint NULL DEFAULT NULL COMMENT '分配的客服ID',
  `reminder_count` int NULL DEFAULT 0 COMMENT '提醒次数',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发关键词',
  `handled_by` bigint NULL DEFAULT NULL COMMENT '处理人用户ID',
  `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理备注',
  `context_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '上下文摘要',
  `auto_reply_keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自动回复关键词',
  `matched_intent` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '匹配的意图',
  `intent_confidence` decimal(5, 2) NULL DEFAULT NULL COMMENT '意图置信度',
  `auto_reply_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自动回复模板',
  `auto_reply_at` datetime NULL DEFAULT NULL COMMENT '自动回复时间',
  `auto_reply_used` tinyint NULL DEFAULT 0 COMMENT '是否使用了自动回复',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pm_status`(`status` ASC) USING BTREE,
  INDEX `idx_pm_status_deadline`(`status` ASC, `deadline` ASC) USING BTREE,
  INDEX `idx_pm_status_deleted_deadline`(`status` ASC, `deleted` ASC, `deadline` ASC) USING BTREE,
  INDEX `idx_pm_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_pm_assigned_cs`(`assigned_cs_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '待处理消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pending_messages
-- ----------------------------
INSERT INTO `pending_messages` VALUES (1, 13, 'yy', '请问一下怎么下单啊？我是新来的不太懂', 'text', '新用户首次咨询，需人工引导', 'NORMAL', '2026-05-12 13:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-13 18:00:00', 0, 3, 0, '新用户', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (2, 15, 'yy', '你们的服务太差了，我要投诉！已经是第三次了！', 'text', '多次投诉客户，需重点关注', 'URGENT', '2026-05-11 22:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-12 20:00:00', 1, 3, 2, '投诉', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (3, 8, 'kook', '请问支持支付宝支付吗？', 'text', '支付方式咨询', 'NORMAL', '2026-05-12 11:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-13 18:00:00', 0, 4, 0, '支付', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (4, 18, 'test', '我觉得APP有点卡，能不能优化一下', 'text', '技术反馈', 'NORMAL', '2026-05-12 09:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-14 18:00:00', 0, 3, 0, '卡顿', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (5, 4, 'wechat', '我想问一下，如果陪玩师不满意可以换人吗？', 'text', '服务政策咨询', 'NORMAL', '2026-05-12 10:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-13 18:00:00', 0, 4, 0, '换人', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (6, 2, 'wechat', '我要退款！刚刚下的单不要了', 'text', '退款请求', 'URGENT', '2026-05-12 14:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-12 18:00:00', 0, 4, 1, '退款', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (7, 11, 'yy', '你们的明星陪玩师多少钱一小时？', 'text', '价格咨询', 'NORMAL', '2026-05-12 12:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-13 18:00:00', 0, 3, 0, '价格', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `pending_messages` VALUES (8, 19, 'test', '我刚注册，有没有新客优惠啊？', 'text', '新客优惠咨询', 'NORMAL', '2026-05-12 08:00:00', '2026-05-12 02:04:10', 0, NULL, 'pending', '2026-05-13 18:00:00', 0, 3, 0, '新客,优惠', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for platform_configs
-- ----------------------------
DROP TABLE IF EXISTS `platform_configs`;
CREATE TABLE `platform_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台标识：wechat-微信，wework-企微，kook-KOOK，yy-YY',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用该平台接入',
  `config` json NULL COMMENT '平台特定配置，JSON格式存储',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_platform_configs_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '平台配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of platform_configs
-- ----------------------------
INSERT INTO `platform_configs` VALUES (1, 'wechat', 1, '{\"appId\": \"wx_test_app_001\", \"token\": \"delta_token_001\", \"appSecret\": \"***\", \"messageFormat\": \"XML\", \"encodingAESKey\": \"***\"}', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `platform_configs` VALUES (2, 'kook', 1, '{\"botToken\": \"***\", \"clientId\": \"kook_client_001\", \"verifyToken\": \"kappa_verify_001\", \"clientSecret\": \"***\"}', '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `platform_configs` VALUES (3, 'yy', 1, '{\"appId\": \"yy_app_001\", \"scopes\": \"user_info,message\", \"appSecret\": \"***\", \"redirectUri\": \"https://delta.com/yy/callback\"}', '2026-03-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `platform_configs` VALUES (4, 'test', 1, '{\"apiBase\": \"https://test-api.delta.com\", \"testMode\": true, \"autoApprove\": true, \"mockEnabled\": true}', '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for pricing_plan
-- ----------------------------
DROP TABLE IF EXISTS `pricing_plan`;
CREATE TABLE `pricing_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '方案编码：BASIC/PRO/ENTERPRISE',
  `plan_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '方案名称：基础版/专业版/企业版',
  `monthly_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '月费价格',
  `yearly_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '年费价格（购买年费享折扣）',
  `per_call_price` decimal(10, 4) NULL DEFAULT 0.0000 COMMENT '按次付费单价（元/次AI对话）',
  `daily_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '日会员价格（9.9元/日）',
  `max_companions` int NULL DEFAULT 5 COMMENT '陪玩师数量上限',
  `max_monthly_messages` int NULL DEFAULT 0 COMMENT '月消息量上限（0=无限制）',
  `max_personality_templates` int NULL DEFAULT 2 COMMENT 'AI人格模板数量上限',
  `emotion_intelligence_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'BASIC' COMMENT '情绪智能等级：BASIC/ADVANCED/PREMIUM',
  `include_smart_dispatch` tinyint(1) NULL DEFAULT 0 COMMENT '是否包含智能派单',
  `include_full_quality_check` tinyint(1) NULL DEFAULT 0 COMMENT '是否包含全流程质检',
  `include_analytics` tinyint(1) NULL DEFAULT 0 COMMENT '是否包含数据分析',
  `include_brand_custom` tinyint(1) NULL DEFAULT 0 COMMENT '是否支持自定义品牌',
  `include_api_access` tinyint(1) NULL DEFAULT 0 COMMENT '是否支持API接入',
  `features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '功能描述（Markdown格式）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pp_plan_code`(`plan_code` ASC) USING BTREE,
  INDEX `idx_pp_status`(`status` ASC) USING BTREE,
  INDEX `idx_pp_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定价方案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pricing_plan
-- ----------------------------
INSERT INTO `pricing_plan` VALUES (1, 'BASIC', '基础版', 0.00, 0.00, 0.0010, 0.00, 5, 5000, 2, 'BASIC', 0, 0, 0, 0, 0, '- AI自动回复\n- 基础关键词触发\n- 5名陪玩师\n- 月消息量5000条', 1, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `pricing_plan` VALUES (2, 'PRO', '专业版', 299.00, 2999.00, 0.0005, 9.90, 50, 50000, 10, 'ADVANCED', 1, 1, 1, 0, 0, '- 全部基础版功能\n- 高级情绪智能\n- 50名陪玩师\n- 智能派单\n- 质检功能\n- 数据分析\n- 10个AI人格模板', 2, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `pricing_plan` VALUES (3, 'ENTERPRISE', '企业版', 999.00, 9999.00, 0.0002, 0.00, 200, 0, 50, 'PREMIUM', 1, 1, 1, 1, 1, '- 全部专业版功能\n- 顶级情绪智能\n- 无限陪玩师\n- 无限消息量\n- 自定义品牌\n- API接入\n- 50个AI人格模板\n- 专属技术支持', 3, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);

-- ----------------------------
-- Table structure for quality_check_record
-- ----------------------------
DROP TABLE IF EXISTS `quality_check_record`;
CREATE TABLE `quality_check_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `companion_id` bigint NULL DEFAULT NULL COMMENT '关联陪玩师ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联客户ID',
  `check_time` datetime NOT NULL COMMENT '检测时间',
  `check_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检测类型：SERVICE-服务质量，CONTENT-内容合规，ATTITUDE-服务态度，SPEED-响应速度',
  `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SAFE' COMMENT '风险等级：SAFE-安全，LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-严重违规',
  `score` int NULL DEFAULT 0 COMMENT '检测得分(1-100)',
  `violation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '违规类型：SEXUAL-涉黄，GAMBLING-涉赌，CHEAT-外挂，ABUSE-辱骂，REPLACE-代打，OTHER-其他',
  `violation_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '违规内容摘要',
  `evidence_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证据截图/录音URL',
  `action_suggestion` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理建议',
  `handle_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理，REVIEWED-已审核，RESOLVED-已处理，IGNORED-已忽略',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qcr_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_qcr_companion_id`(`companion_id` ASC) USING BTREE,
  INDEX `idx_qcr_check_time`(`check_time` ASC) USING BTREE,
  INDEX `idx_qcr_risk_level`(`risk_level` ASC) USING BTREE,
  INDEX `idx_qcr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务质量检测记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quality_check_record
-- ----------------------------
INSERT INTO `quality_check_record` VALUES (1, 3, 5, 3, '2026-03-16 14:00:00', 'SERVICE', 'MEDIUM', 55, NULL, '陪玩师周星星迟到15分钟，未提前通知客户', 'https://cdn.delta.com/evidence/qc001.png', '警告处分，扣除当月绩效50元', 'RESOLVED', 4, '已警告并扣款', '2026-03-16 14:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `quality_check_record` VALUES (2, 7, 6, 9, '2026-03-25 08:00:00', 'ATTITUDE', 'CRITICAL', 20, 'ABUSE', '陪玩师吴天在服务中频繁催促结束，态度恶劣，客户极度不满', 'https://cdn.delta.com/evidence/qc002.png', '停权处理，安排合规再培训，全额退款', 'RESOLVED', 4, '已停权并安排培训', '2026-03-25 08:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `quality_check_record` VALUES (3, 1, 1, 1, '2026-05-02 10:00:00', 'SERVICE', 'SAFE', 95, NULL, '服务表现优秀，客户评价5星', NULL, '继续保持高水平服务', 'REVIEWED', 3, '优秀，值得表扬', '2026-05-02 10:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `quality_check_record` VALUES (4, 4, 2, 5, '2026-04-16 10:00:00', 'CONTENT', 'LOW', 78, NULL, '聊天内容中偶有不当网络用语，建议改善', 'https://cdn.delta.com/evidence/qc004.png', '提醒注意语言规范', 'REVIEWED', 3, '已口头提醒', '2026-04-16 10:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `quality_check_record` VALUES (5, 9, 3, 14, '2026-05-03 14:00:00', 'SPEED', 'SAFE', 88, NULL, '响应速度良好，服务流程规范', NULL, '保持服务水准', 'PENDING', NULL, NULL, '2026-05-03 14:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for referral_record
-- ----------------------------
DROP TABLE IF EXISTS `referral_record`;
CREATE TABLE `referral_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campaign_id` bigint NULL DEFAULT NULL COMMENT '关联营销活动ID',
  `referrer_user_id` bigint NOT NULL COMMENT '推荐人用户ID（老客户）',
  `referee_user_id` bigint NULL DEFAULT NULL COMMENT '被推荐人用户ID（新客户）',
  `referral_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐码',
  `referral_time` datetime NOT NULL COMMENT '推荐时间',
  `conversion_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '转化状态：PENDING-待注册，REGISTERED-已注册，TRIALING-试用中，SUBSCRIBED-已付费',
  `converted_at` datetime NULL DEFAULT NULL COMMENT '转化时间（注册/付费时间）',
  `reward_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐人奖励类型：MONTH_FREE-赠送月会员，CASH-现金奖励，POINTS-积分奖励',
  `reward_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '推荐人奖励金额（元）',
  `reward_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '奖励发放状态：PENDING-待发放，ISSUED-已发放，CANCELLED-已取消',
  `reward_issued_at` datetime NULL DEFAULT NULL COMMENT '奖励发放时间',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rr_referrer_id`(`referrer_user_id` ASC) USING BTREE,
  INDEX `idx_rr_referee_id`(`referee_user_id` ASC) USING BTREE,
  INDEX `idx_rr_status`(`conversion_status` ASC) USING BTREE,
  INDEX `idx_rr_campaign_id`(`campaign_id` ASC) USING BTREE,
  INDEX `idx_rr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '裂变推荐记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of referral_record
-- ----------------------------
INSERT INTO `referral_record` VALUES (1, 1, 1, 2, 'REF001_DELTA', '2026-05-01 10:00:00', 'SUBSCRIBED', '2026-05-03 14:00:00', 'MONTH_FREE', 100.00, 'ISSUED', '2026-05-05 09:00:00', NULL, '2026-05-01 10:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (2, 1, 1, 4, 'REF002_DELTA', '2026-05-02 15:00:00', 'TRIALING', NULL, 'CASH', 100.00, 'PENDING', NULL, NULL, '2026-05-02 15:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (3, 1, 5, 6, 'REF003_DELTA', '2026-05-03 12:00:00', 'REGISTERED', '2026-05-03 15:00:00', 'CASH', 100.00, 'PENDING', NULL, NULL, '2026-05-03 12:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (4, 1, 16, 8, 'REF004_DELTA', '2026-05-04 09:00:00', 'REGISTERED', '2026-05-04 12:00:00', 'CASH', 100.00, 'PENDING', NULL, NULL, '2026-05-04 09:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (5, 2, 7, 13, 'REF005_DELTA', '2026-05-10 14:00:00', 'PENDING', NULL, NULL, 0.00, 'PENDING', NULL, NULL, '2026-05-10 14:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (6, 1, 16, 19, 'REF006_DELTA', '2026-05-06 11:00:00', 'REGISTERED', '2026-05-06 14:00:00', 'CASH', 100.00, 'PENDING', NULL, NULL, '2026-05-06 11:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (7, 1, 17, 3, 'REF007_DELTA', '2026-05-07 16:00:00', 'SUBSCRIBED', '2026-05-08 10:00:00', 'MONTH_FREE', 100.00, 'ISSUED', '2026-05-09 10:00:00', NULL, '2026-05-07 16:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `referral_record` VALUES (8, 2, 9, 15, 'REF008_DELTA', '2026-05-11 20:00:00', 'PENDING', NULL, NULL, 0.00, 'PENDING', NULL, NULL, '2026-05-11 20:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for replies
-- ----------------------------
DROP TABLE IF EXISTS `replies`;
CREATE TABLE `replies`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trigger_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发类型：keyword-关键词触发，welcome-新用户关注触发，default-默认回复',
  `trigger_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发键，关键词触发时为具体关键词，欢迎触发时为\"welcome\"',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_replies_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_replies_trigger_key`(`trigger_key`(191) ASC) USING BTREE,
  INDEX `idx_replies_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '自动回复规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of replies
-- ----------------------------
INSERT INTO `replies` VALUES (1, 'keyword', '下单', '好的！请问您想预约什么时间段的陪玩服务呢？我们可以为您匹配最适合的陪玩师。', 1, '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `replies` VALUES (2, 'keyword', '预约', '很高兴为您服务！请告诉我您想要的游戏类型、时段和陪玩师等级，我来为您安排。', 1, '2026-01-15 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `replies` VALUES (3, 'keyword', '价格', '我们的服务价格根据等级不同：二品150元/小时、一品300元/小时、顶尖500元/小时、明星1000元/小时。新用户首单享8折哦！', 1, '2026-02-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `replies` VALUES (4, 'welcome', 'welcome', '欢迎加入{俱乐部名称}！我是您的AI助手，随时为您解答问题、预约服务。请问有什么可以帮您的？', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `replies` VALUES (5, 'default', 'default', '很抱歉，我暂时无法完全理解您的问题。正在为您转接人工客服，请稍等～', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `replies` VALUES (6, 'keyword', '帮助', '我可以帮您：1.查询价格 2.预约陪玩 3.了解套餐 4.转人工服务 5.查询订单。请告诉我您需要什么帮助？', 1, '2026-03-01 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for reply_usage_stat
-- ----------------------------
DROP TABLE IF EXISTS `reply_usage_stat`;
CREATE TABLE `reply_usage_stat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reply_id` bigint NOT NULL COMMENT '快捷回复ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `conversion_count` int NOT NULL DEFAULT 0 COMMENT '转化次数（使用后用户下单）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rus_reply_date`(`reply_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_rus_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '快捷回复使用统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reply_usage_stat
-- ----------------------------

-- ----------------------------
-- Table structure for revenue_daily_report
-- ----------------------------
DROP TABLE IF EXISTS `revenue_daily_report`;
CREATE TABLE `revenue_daily_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NOT NULL COMMENT '俱乐部配置ID',
  `report_date` date NOT NULL COMMENT '报表日期',
  `game_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游戏类型（null=全游戏合计）',
  `total_orders` int NULL DEFAULT 0 COMMENT '订单总数',
  `completed_orders` int NULL DEFAULT 0 COMMENT '已完成订单数',
  `refund_orders` int NULL DEFAULT 0 COMMENT '退款订单数',
  `total_revenue` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '订单总收入',
  `platform_income` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '平台分成收入',
  `ai_conversations` int NULL DEFAULT 0 COMMENT 'AI会话总数',
  `ai_handle_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'AI处理率(%)',
  `avg_satisfaction` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '客户满意度均值',
  `new_customers` int NULL DEFAULT 0 COMMENT '新客户数',
  `repeat_customers` int NULL DEFAULT 0 COMMENT '老客户复购数',
  `active_companions` int NULL DEFAULT 0 COMMENT '活跃陪玩师数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rdr_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_rdr_report_date`(`report_date` ASC) USING BTREE,
  INDEX `idx_rdr_game_type`(`game_type` ASC) USING BTREE,
  INDEX `idx_rdr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '营收日报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of revenue_daily_report
-- ----------------------------
INSERT INTO `revenue_daily_report` VALUES (1, 1, '2026-05-06', 'FPS', 45, 40, 2, 22000.00, 4400.00, 380, 72.50, 4.50, 12, 28, 8, '2026-05-07 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (2, 1, '2026-05-07', 'FPS', 52, 48, 1, 26000.00, 5200.00, 420, 73.80, 4.60, 15, 33, 10, '2026-05-08 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (3, 1, '2026-05-08', 'FPS', 48, 45, 0, 24000.00, 4800.00, 390, 75.20, 4.70, 10, 35, 9, '2026-05-09 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (4, 2, '2026-05-06', 'FPS', 28, 25, 2, 7000.00, 1400.00, 150, 68.00, 4.20, 8, 17, 4, '2026-05-07 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (5, 2, '2026-05-07', 'FPS', 32, 30, 1, 8000.00, 1600.00, 170, 70.50, 4.30, 6, 24, 5, '2026-05-08 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (6, 3, '2026-05-06', 'FPS', 15, 12, 1, 1800.00, 360.00, 80, 85.00, 4.80, 5, 7, 3, '2026-05-07 00:00:00', '2026-05-12 02:04:11', 0, NULL);
INSERT INTO `revenue_daily_report` VALUES (7, 1, '2026-05-09', 'FPS', 55, 52, 3, 27500.00, 5500.00, 450, 74.00, 4.50, 18, 34, 10, '2026-05-10 00:00:00', '2026-05-12 02:04:11', 0, NULL);

-- ----------------------------
-- Table structure for service_item
-- ----------------------------
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `club_config_id` bigint NULL DEFAULT NULL COMMENT '俱乐部配置ID',
  `game_config_id` bigint NULL DEFAULT NULL COMMENT '游戏配置ID',
  `service_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `service_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目编码',
  `service_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `service_icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类：ACCOMPANY-陪玩，PACKAGE-套餐，TEACHING-教学，SOCIAL-社交',
  `base_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '基础价格',
  `price_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'HOUR' COMMENT '价格单位：HOUR-小时，NIGHT-包夜，ORDER-按单',
  `min_duration` decimal(10, 2) NULL DEFAULT NULL COMMENT '最短时长',
  `guarantee_text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '保障说明',
  `refund_policy` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款政策',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_si_club_config_id`(`club_config_id` ASC) USING BTREE,
  INDEX `idx_si_game_config_id`(`game_config_id` ASC) USING BTREE,
  INDEX `idx_si_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_item
-- ----------------------------
INSERT INTO `service_item` VALUES (1, 1, 1, '普通陪玩', 'SVC_ACCOMPANY', '标准陪玩服务，陪玩师陪同客户进行游戏', NULL, 'ACCOMPANY', 200.00, 'HOUR', 1.00, '不满意免费重来', '开始前可全额退款', 1, 1, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (2, 1, 1, '高端陪玩', 'SVC_ELITE', '顶尖陪玩师专属服务，包含语音指导和战术分析', NULL, 'ACCOMPANY', 500.00, 'HOUR', 1.00, '保证上分，否则退款', '开始前可全额退款', 1, 2, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (3, 1, 1, '游戏教学', 'SVC_TEACHING', '一对一游戏教学，包括基础操作和高级技巧', NULL, 'TEACHING', 300.00, 'HOUR', 1.50, '学不会免费重教', '开始前可全额退款', 1, 3, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (4, 1, 1, '社交陪聊', 'SVC_SOCIAL', '纯社交互动服务，语音陪聊、情感陪伴', NULL, 'SOCIAL', 150.00, 'HOUR', 0.50, '不满意随时退出', '按实际时长计费', 1, 4, '2026-01-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (5, 2, 4, '深夜陪玩', 'SVC_NIGHT', '深夜时段(22:00-06:00)专属陪玩服务', NULL, 'ACCOMPANY', 250.00, 'HOUR', 1.00, '深夜专属保障', '开始前可全额退款', 1, 1, '2026-01-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (6, 3, 5, '新手教学', 'SVC_BEGINNER', '适合零基础新手的入门教学服务', NULL, 'TEACHING', 120.00, 'HOUR', 2.00, '包教包会', '未学会可免费重学', 1, 1, '2026-02-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (7, 1, 2, 'CSGO陪玩', 'SVC_CSGO', 'CSGO专业陪玩，含战术指导和团队配合', NULL, 'ACCOMPANY', 180.00, 'HOUR', 1.00, '保证胜率', '开始前可全额退款', 1, 5, '2026-02-15 00:00:00', '2026-05-12 02:04:09', 0, NULL);
INSERT INTO `service_item` VALUES (8, 3, 6, '战术教学', 'SVC_TACTICS', 'R6专业战术教学，包含地图分析和团队战术', NULL, 'TEACHING', 200.00, 'HOUR', 1.50, '保证战术提升', '开始前可全额退款', 1, 2, '2026-03-01 00:00:00', '2026-05-12 02:04:09', 0, NULL);

-- ----------------------------
-- Table structure for service_price_rule
-- ----------------------------
DROP TABLE IF EXISTS `service_price_rule`;
CREATE TABLE `service_price_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_item_id` bigint NOT NULL COMMENT '服务项目ID',
  `companion_level_id` bigint NOT NULL COMMENT '陪玩师等级ID',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `price_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'HOUR' COMMENT '价格单位',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_spr_service_item_id`(`service_item_id` ASC) USING BTREE,
  INDEX `idx_spr_companion_level_id`(`companion_level_id` ASC) USING BTREE,
  INDEX `idx_spr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务价格规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_price_rule
-- ----------------------------
INSERT INTO `service_price_rule` VALUES (1, 1, 1, 150.00, 200.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (2, 1, 2, 300.00, 350.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (3, 1, 3, 500.00, 600.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (4, 1, 4, 1000.00, 1200.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (5, 2, 3, 500.00, 600.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (6, 2, 4, 1000.00, 1200.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (7, 3, 2, 300.00, 350.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (8, 3, 3, 500.00, 600.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (9, 4, 1, 150.00, 180.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (10, 4, 2, 300.00, 350.00, 'HOUR', 1, '2026-01-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (11, 5, 1, 200.00, 250.00, 'HOUR', 1, '2026-01-15 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (12, 5, 2, 350.00, 400.00, 'HOUR', 1, '2026-01-15 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (13, 6, 1, 120.00, 150.00, 'HOUR', 1, '2026-02-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (14, 6, 2, 200.00, 250.00, 'HOUR', 1, '2026-02-01 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (15, 7, 2, 250.00, 300.00, 'HOUR', 1, '2026-02-15 00:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_price_rule` VALUES (16, 7, 3, 450.00, 500.00, 'HOUR', 1, '2026-02-15 00:00:00', '2026-05-12 02:04:10', 0, NULL);

-- ----------------------------
-- Table structure for service_tracks
-- ----------------------------
DROP TABLE IF EXISTS `service_tracks`;
CREATE TABLE `service_tracks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '客户ID',
  `track_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '追踪类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID',
  `track_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态：CONSULTING-咨询中，BOOKED-已预约，SERVICING-服务中，SERVICE_DONE-服务完成，CONFIRMED-已确认',
  `current_step` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前步骤',
  `track_data` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '追踪数据（JSON格式）',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '持续时长（秒）',
  `result` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_st_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_st_related_id`(`related_id` ASC) USING BTREE,
  INDEX `idx_st_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务追踪表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_tracks
-- ----------------------------
INSERT INTO `service_tracks` VALUES (1, 1, 'ACCOMPANY', 1, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"服务\",\"评价\"]}', '2026-05-01 18:00:00', '2026-05-01 20:00:00', 7200, '完成', '2026-05-01 18:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (2, 2, 'ACCOMPANY', 2, 'SERVICE_DONE', '待评价', '{\"steps\":[\"咨询\",\"匹配\",\"服务\"]}', '2026-04-20 19:00:00', '2026-04-20 20:30:00', 5400, '待评价', '2026-04-20 19:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (3, 3, 'TEACHING', 3, 'SERVICE_DONE', '待评价', '{\"steps\":[\"咨询\",\"匹配\",\"教学\"]}', '2026-03-15 15:00:00', '2026-03-15 16:00:00', 3600, '待评价', '2026-03-15 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (4, 5, 'ACCOMPANY', 4, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"服务\",\"评价\"]}', '2026-04-15 20:00:00', '2026-04-15 22:00:00', 7200, '完成', '2026-04-15 20:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (5, 6, 'ACCOMPANY', 5, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"服务\",\"评价\"]}', '2026-04-10 15:00:00', '2026-04-10 16:30:00', 5400, '完成', '2026-04-10 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (6, 7, 'TEACHING', 6, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"教学\",\"评价\"]}', '2026-03-20 18:00:00', '2026-03-20 20:00:00', 7200, '完成', '2026-03-20 18:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (7, 9, 'ACCOMPANY', 7, 'SERVICE_DONE', '退款中', '{\"steps\":[\"咨询\",\"匹配\",\"服务\",\"投诉\"]}', '2026-03-24 21:00:00', '2026-03-24 22:00:00', 3600, '已退款', '2026-03-24 21:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (8, 10, 'TEACHING', 8, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"教学\",\"评价\"]}', '2026-04-25 10:00:00', '2026-04-25 11:30:00', 5400, '完成', '2026-04-25 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (9, 14, 'PACKAGE', 9, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"选择套餐\",\"匹配\",\"服务\",\"评价\"]}', '2026-05-02 10:00:00', '2026-05-02 13:00:00', 10800, '完成', '2026-05-02 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (10, 16, 'ACCOMPANY', 10, 'CONFIRMED', '完成', '{\"steps\":[\"咨询\",\"匹配\",\"服务\",\"评价\"]}', '2026-05-05 08:00:00', '2026-05-05 10:00:00', 7200, '完成', '2026-05-05 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `service_tracks` VALUES (11, 1, NULL, NULL, 'CONSULTING', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-12 11:48:52', '2026-05-12 11:48:52', 0, NULL);
INSERT INTO `service_tracks` VALUES (12, 1, NULL, NULL, 'CONSULTING', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);
INSERT INTO `service_tracks` VALUES (13, 16, NULL, NULL, 'CONSULTING', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人名称',
  `module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作模块',
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `target_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作对象类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '操作对象ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作内容描述',
  `before_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '变更前数据(JSON)',
  `after_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '变更后数据(JSON)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器UA',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `status` int NULL DEFAULT 1 COMMENT '操作状态：1-成功，0-失败',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sol_operator`(`operator_id` ASC) USING BTREE,
  INDEX `idx_sol_module`(`module` ASC) USING BTREE,
  INDEX `idx_sol_operate_time`(`operate_time` ASC) USING BTREE,
  INDEX `idx_sol_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统操作审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限主键ID',
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `perm_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `perm_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限分组',
  `action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sp_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_sp_group`(`perm_group` ASC) USING BTREE,
  INDEX `idx_sp_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 87 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'permission:manage', '权限管理', 'permission', 'manage', '管理角色和权限分配', 1, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (2, 'dashboard:view', '查看工作台', 'dashboard', 'view', '查看运营数据仪表盘', 2, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (3, 'stats:view', '查看统计', 'stats', 'view', '查看统计数据', 3, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (4, 'customer:view', '查看客户', 'customer', 'view', '查看客户名录', 4, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (5, 'customer:edit', '编辑客户', 'customer', 'edit', '编辑客户信息', 5, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (6, 'customer:assign', '分配客户', 'customer', 'manage', '将客户分配给客服', 6, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (7, 'customer:export', '导出客户', 'customer', 'export', '导出客户数据', 7, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (8, 'customer_profile:view', '查看客户画像', 'customer', 'view', '查看客户画像和消费记录', 8, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (9, 'customer_profile:edit', '编辑客户画像', 'customer', 'edit', '编辑客户画像和消费记录', 9, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (10, 'customer_satisfaction:view', '查看满意度', 'customer', 'view', '查看客户满意度评价', 10, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (11, 'customer_satisfaction:edit', '管理满意度', 'customer', 'edit', '回复和管理满意度评价', 11, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (12, 'customer_lifecycle:view', '查看生命周期', 'customer', 'view', '查看客户生命周期阶段', 12, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (13, 'customer_lifecycle:edit', '管理生命周期', 'customer', 'edit', '管理客户生命周期阶段', 13, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (14, 'companion:view', '查看陪玩师', 'companion', 'view', '查看陪玩师列表和详情', 14, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (15, 'companion:edit', '编辑陪玩师', 'companion', 'edit', '新增/编辑陪玩师', 15, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (16, 'companion:export', '导出陪玩师', 'companion', 'export', '导出陪玩师Excel', 16, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (17, 'companion:import', '导入陪玩师', 'companion', 'import', '导入陪玩师Excel', 17, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (18, 'companion:rating', '评分看板', 'companion', 'view', '查看陪玩师评分数据', 18, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (19, 'companion_level:view', '查看等级', 'companion', 'view', '查看陪玩师等级', 19, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (20, 'companion_level:edit', '编辑等级', 'companion', 'edit', '编辑陪玩师等级', 20, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (21, 'companion_level:export', '导出等级', 'companion', 'export', '导出等级Excel', 21, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (22, 'companion_level:import', '导入等级', 'companion', 'import', '导入等级Excel', 22, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (23, 'companion_settlement:view', '查看结算', 'companion', 'view', '查看陪玩师结算', 23, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (24, 'companion_settlement:edit', '编辑结算', 'companion', 'edit', '编辑陪玩师结算', 24, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (25, 'companion_training:view', '查看培训', 'companion', 'view', '查看陪玩师培训', 25, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (26, 'companion_training:edit', '编辑培训', 'companion', 'edit', '编辑陪玩师培训', 26, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (27, 'schedule:view', '查看排班', 'schedule', 'view', '查看陪玩师排班', 27, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (28, 'schedule:edit', '编辑排班', 'schedule', 'edit', '编辑陪玩师排班', 28, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (29, 'schedule:export', '导出排班', 'schedule', 'export', '导出排班Excel', 29, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (30, 'schedule:import', '导入排班', 'schedule', 'import', '导入排班Excel', 30, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (31, 'order:view', '查看订单', 'order', 'view', '查看订单列表和详情', 31, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (32, 'order:edit', '编辑订单', 'order', 'edit', '修改订单状态', 32, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (33, 'order:review', '订单评价', 'order', 'edit', '提交订单评价', 33, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (34, 'order_status_history:view', '订单历史', 'order', 'view', '查看订单状态变更历史', 34, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (35, 'work_order:view', '查看工单', 'order', 'view', '查看客服工单', 35, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (36, 'work_order:edit', '编辑工单', 'order', 'edit', '编辑客服工单', 36, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (37, 'service_item:view', '查看服务项目', 'service', 'view', '查看服务项目', 37, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (38, 'service_item:edit', '编辑服务项目', 'service', 'edit', '编辑服务项目', 38, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (39, 'service_track:view', '服务追踪', 'service', 'view', '查看服务追踪记录', 39, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (40, 'pricing_plan:view', '查看定价', 'service', 'view', '查看定价方案', 40, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (41, 'pricing_plan:edit', '编辑定价', 'service', 'edit', '编辑定价方案', 41, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (42, 'activity_package:view', '查看套餐', 'activity', 'view', '查看活动套餐', 42, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (43, 'activity_package:edit', '编辑套餐', 'activity', 'edit', '编辑活动套餐', 43, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (44, 'activity_package:export', '导出套餐', 'activity', 'export', '导出套餐Excel', 44, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (45, 'activity_package:import', '导入套餐', 'activity', 'import', '导入套餐Excel', 45, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (46, 'subscription:view', '查看订阅', 'subscription', 'view', '查看俱乐部订阅', 46, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (47, 'subscription:edit', '编辑订阅', 'subscription', 'edit', '管理俱乐部订阅', 47, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (48, 'campaign:view', '查看活动', 'campaign', 'view', '查看营销活动', 48, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (49, 'campaign:edit', '编辑活动', 'campaign', 'edit', '管理营销活动', 49, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (50, 'message:view', '查看消息', 'message', 'view', '查看消息记录', 50, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (51, 'pending_message:view', '查看待办', 'message', 'view', '查看待办消息', 51, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (52, 'pending_message:edit', '处理待办', 'message', 'edit', '认领和处理待办消息', 52, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (53, 'pending_message:export', '导出待办', 'message', 'export', '导出待办消息', 53, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (54, 'chat:view', '对话测试', 'message', 'view', '使用AI对话测试沙箱', 54, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (55, 'referral:view', '查看推荐', 'referral', 'view', '查看裂变推荐记录', 55, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (56, 'referral:edit', '编辑推荐', 'referral', 'edit', '管理裂变推荐', 56, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (57, 'cs_assignment:view', '查看分配', 'assignment', 'view', '查看客服客户分配', 57, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (58, 'cs_assignment:edit', '编辑分配', 'assignment', 'edit', '管理客服客户分配', 58, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (59, 'platform:manage', '平台配置', 'config', 'manage', '管理外部平台接入参数', 59, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (60, 'club_config:view', '查看俱乐部', 'config', 'view', '查看俱乐部品牌配置', 60, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (61, 'club_config:edit', '编辑俱乐部', 'config', 'edit', '编辑俱乐部品牌配置', 61, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (62, 'game_config:view', '查看游戏', 'config', 'view', '查看游戏配置', 62, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (63, 'game_config:edit', '编辑游戏', 'config', 'edit', '编辑游戏配置', 63, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (64, 'game_config:export', '导出游戏', 'config', 'export', '导出游戏配置', 64, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (65, 'ai_config:view', '查看AI配置', 'config', 'view', '查看AI引擎参数', 65, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (66, 'ai_config:edit', '编辑AI配置', 'config', 'edit', '编辑AI引擎参数', 66, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (67, 'faq_item:view', '查看FAQ', 'config', 'view', '查看FAQ知识库', 67, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (68, 'faq_item:edit', '编辑FAQ', 'config', 'edit', '编辑FAQ知识库', 68, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (69, 'faq_item:import', '导入FAQ', 'config', 'import', '导入FAQ知识库', 69, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (70, 'keyword:view', '查看关键词', 'config', 'view', '查看关键词规则', 70, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (71, 'keyword:edit', '编辑关键词', 'config', 'edit', '编辑关键词规则', 71, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (72, 'keyword:import', '导入关键词', 'config', 'import', '导入关键词Excel', 72, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (73, 'reply:view', '查看话术', 'config', 'view', '查看回复话术模板', 73, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (74, 'reply:edit', '编辑话术', 'config', 'edit', '编辑回复话术模板', 74, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (75, 'reply:import', '导入话术', 'config', 'import', '导入话术Excel', 75, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (76, 'sys_user:view', '查看用户', 'config', 'view', '查看系统用户', 76, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (77, 'sys_user:edit', '编辑用户', 'config', 'edit', '管理系统用户', 77, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (78, 'sys_user:audit', '审核用户', 'config', 'edit', '审核用户注册', 78, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (79, 'sys_user:export', '导出用户', 'config', 'export', '导出用户Excel', 79, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (80, 'quality_check:view', '查看质检', 'quality', 'view', '查看质检记录', 80, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (81, 'quality_check:edit', '编辑质检', 'quality', 'edit', '编辑质检记录', 81, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (82, 'revenue_report:view', '查看财报', 'revenue', 'view', '查看营收日报', 82, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (83, 'revenue_report:edit', '编辑财报', 'revenue', 'edit', '编辑营收数据', 83, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (84, 'cache:view', '查看缓存', 'cache', 'view', '查看缓存状态', 84, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (85, 'cache:edit', '管理缓存', 'cache', 'edit', '清除和管理缓存', 85, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_permission` VALUES (86, 'system:admin', '系统管理', 'system', 'manage', '最高权限，管理所有系统配置', 86, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `is_system` tinyint NULL DEFAULT 0 COMMENT '是否系统内置角色：1-是，0-否',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sr_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_sr_status`(`status` ASC) USING BTREE,
  INDEX `idx_sr_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'SYS_ADMIN', '超级管理员', '拥有系统所有权限，可管理系统配置、用户、角色和权限', 1, 1, 1, NULL, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role` VALUES (2, 'CS_LEADER', '客服主管', '管理客服团队、查看所有数据和报表、管理陪玩师和订单', 1, 1, 2, NULL, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role` VALUES (3, 'CS_STAFF', '客服人员', '处理客户咨询、管理工单和消息、查看分配客户数据', 1, 1, 3, NULL, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role` VALUES (4, 'COMPANION', '陪玩师', '查看个人排班、订单和结算信息，参与培训', 1, 1, 4, NULL, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `perm_id` ASC) USING BTREE,
  INDEX `idx_srp_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_srp_perm_id`(`perm_id` ASC) USING BTREE,
  INDEX `idx_srp_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 228 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (2, 1, 2, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (3, 1, 3, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (4, 1, 4, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (5, 1, 5, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (6, 1, 6, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (7, 1, 7, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (8, 1, 8, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (9, 1, 9, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (10, 1, 10, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (11, 1, 11, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (12, 1, 12, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (13, 1, 13, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (14, 1, 14, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (15, 1, 15, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (16, 1, 16, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (17, 1, 17, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (18, 1, 18, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (19, 1, 19, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (20, 1, 20, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (21, 1, 21, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (22, 1, 22, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (23, 1, 23, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (24, 1, 24, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (25, 1, 25, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (26, 1, 26, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (27, 1, 27, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (28, 1, 28, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (29, 1, 29, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (30, 1, 30, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (31, 1, 31, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (32, 1, 32, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (33, 1, 33, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (34, 1, 34, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (35, 1, 35, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (36, 1, 36, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (37, 1, 37, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (38, 1, 38, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (39, 1, 39, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (40, 1, 40, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (41, 1, 41, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (42, 1, 42, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (43, 1, 43, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (44, 1, 44, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (45, 1, 45, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (46, 1, 46, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (47, 1, 47, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (48, 1, 48, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (49, 1, 49, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (50, 1, 50, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (51, 1, 51, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (52, 1, 52, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (53, 1, 53, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (54, 1, 54, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (55, 1, 55, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (56, 1, 56, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (57, 1, 57, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (58, 1, 58, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (59, 1, 59, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (60, 1, 60, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (61, 1, 61, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (62, 1, 62, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (63, 1, 63, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (64, 1, 64, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (65, 1, 65, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (66, 1, 66, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (67, 1, 67, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (68, 1, 68, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (69, 1, 69, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (70, 1, 70, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (71, 1, 71, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (72, 1, 72, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (73, 1, 73, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (74, 1, 74, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (75, 1, 75, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (76, 1, 76, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (77, 1, 77, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (78, 1, 78, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (79, 1, 79, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (80, 1, 80, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (81, 1, 81, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (82, 1, 82, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (83, 1, 83, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (84, 1, 84, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (85, 1, 85, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (86, 1, 86, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (87, 2, 2, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (88, 2, 3, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (89, 2, 4, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (90, 2, 5, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (91, 2, 6, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (92, 2, 7, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (93, 2, 8, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (94, 2, 9, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (95, 2, 10, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (96, 2, 11, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (97, 2, 12, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (98, 2, 13, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (99, 2, 14, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (100, 2, 15, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (101, 2, 16, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (102, 2, 17, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (103, 2, 18, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (104, 2, 19, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (105, 2, 20, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (106, 2, 21, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (107, 2, 22, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (108, 2, 23, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (109, 2, 24, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (110, 2, 25, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (111, 2, 26, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (112, 2, 27, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (113, 2, 28, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (114, 2, 29, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (115, 2, 30, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (116, 2, 31, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (117, 2, 32, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (118, 2, 33, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (119, 2, 34, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (120, 2, 35, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (121, 2, 36, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (122, 2, 37, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (123, 2, 38, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (124, 2, 39, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (125, 2, 40, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (126, 2, 41, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (127, 2, 42, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (128, 2, 43, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (129, 2, 44, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (130, 2, 45, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (131, 2, 46, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (132, 2, 47, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (133, 2, 48, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (134, 2, 49, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (135, 2, 50, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (136, 2, 51, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (137, 2, 52, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (138, 2, 53, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (139, 2, 54, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (140, 2, 55, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (141, 2, 56, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (142, 2, 57, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (143, 2, 58, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (144, 2, 59, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (145, 2, 60, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (146, 2, 61, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (147, 2, 62, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (148, 2, 63, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (149, 2, 64, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (150, 2, 65, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (151, 2, 66, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (152, 2, 67, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (153, 2, 68, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (154, 2, 69, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (155, 2, 70, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (156, 2, 71, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (157, 2, 72, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (158, 2, 73, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (159, 2, 74, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (160, 2, 75, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (161, 2, 76, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (162, 2, 77, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (163, 2, 78, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (164, 2, 79, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (165, 2, 80, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (166, 2, 81, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (167, 2, 82, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (168, 2, 83, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (169, 2, 84, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (170, 2, 85, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (171, 3, 2, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (172, 3, 3, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (173, 3, 4, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (174, 3, 5, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (175, 3, 8, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (176, 3, 10, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (177, 3, 12, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (178, 3, 14, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (179, 3, 18, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (180, 3, 19, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (181, 3, 31, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (182, 3, 32, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (183, 3, 33, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (184, 3, 34, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (185, 3, 35, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (186, 3, 36, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (187, 3, 37, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (188, 3, 39, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (189, 3, 40, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (190, 3, 42, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (191, 3, 46, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (192, 3, 48, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (193, 3, 50, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (194, 3, 51, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (195, 3, 52, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (196, 3, 54, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (197, 3, 55, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (198, 3, 57, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (199, 3, 60, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (200, 3, 62, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (201, 3, 65, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (202, 3, 67, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (203, 3, 70, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (204, 3, 73, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (205, 3, 76, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (206, 3, 77, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (207, 3, 80, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (208, 3, 82, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (209, 3, 84, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (210, 4, 14, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (211, 4, 18, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (212, 4, 19, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (213, 4, 23, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (214, 4, 25, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (215, 4, 27, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (216, 4, 31, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (217, 4, 34, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (218, 4, 37, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (219, 4, 39, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (220, 4, 42, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (221, 4, 46, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (222, 4, 50, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (223, 4, 54, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (224, 4, 60, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (225, 4, 62, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (226, 4, 67, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_role_permission` VALUES (227, 4, 73, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CS_STAFF' COMMENT '角色：SYS_ADMIN-系统管理员，CS_LEADER-客服负责人，CS_STAFF-普通客服',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：PENDING-待审核，ACTIVE-正常，DISABLED-禁用',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `two_factor_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用双因素认证：0-未启用，1-已启用',
  `two_factor_secret` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '双因素密钥（Base32编码）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  INDEX `idx_sys_user_role`(`role` ASC) USING BTREE,
  INDEX `idx_sys_user_status`(`status` ASC) USING BTREE,
  INDEX `idx_sys_user_role_status`(`role` ASC, `status` ASC) USING BTREE,
  INDEX `idx_sys_user_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '系统管理员', NULL, NULL, 'SYS_ADMIN', 'ACTIVE', NULL, 0, NULL, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_user` VALUES (2, 'zhangsan', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '张三', '13800001001', 'zhangsan@delta.com', 'CS_LEADER', 'ACTIVE', 1, 0, NULL, '2026-01-15 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (3, 'lisi', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '李四', '13800001002', 'lisi@delta.com', 'CS_STAFF', 'ACTIVE', 2, 0, NULL, '2026-01-15 09:30:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (4, 'wangwu', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '王五', '13800001003', 'wangwu@delta.com', 'CS_STAFF', 'ACTIVE', 2, 0, NULL, '2026-02-01 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (5, 'zhaoliu', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '赵六', '13800001004', 'zhaoliu@delta.com', 'CS_STAFF', 'DISABLED', 2, 0, NULL, '2026-02-15 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (6, 'companion_chen', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '陈陪玩', '13800001005', 'chen@delta.com', 'COMPANION', 'ACTIVE', 2, 0, NULL, '2026-03-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (7, 'pending_user', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '待审核用户', '13800001006', 'pending@delta.com', 'CS_STAFF', 'PENDING', NULL, 0, NULL, '2026-05-10 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user` VALUES (8, 'auditor_liu', '$2a$10$GP91WyYkbRTem9Od9fFH9O1z6ZKJrGhe63JY7cSsEK9n.0QhkKBMa', '刘审计', '13800001007', 'liu_audit@delta.com', 'CS_LEADER', 'ACTIVE', 1, 0, NULL, '2026-04-01 09:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_sur_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_sur_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_sur_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户-角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-05-12 02:03:34', '2026-05-12 02:03:34', 0, NULL);
INSERT INTO `sys_user_role` VALUES (2, 2, 2, '2026-01-15 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (3, 3, 3, '2026-01-15 09:30:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (4, 4, 3, '2026-02-01 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (5, 5, 3, '2026-02-15 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (6, 6, 4, '2026-03-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (7, 7, 3, '2026-05-10 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `sys_user_role` VALUES (8, 8, 2, '2026-04-01 09:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源平台：wechat、test等',
  `platform_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平台侧用户ID，如微信的openid',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `ai_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用AI自动回复：1-AI回复，0-仅人工',
  `assigned_cs_user_id` bigint NULL DEFAULT NULL COMMENT '分配的专属客服ID（sys_user表），NULL表示未分配',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users_platform_user`(`platform` ASC, `platform_user_id` ASC) USING BTREE,
  INDEX `idx_users_platform`(`platform` ASC) USING BTREE,
  INDEX `idx_users_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_users_assigned_cs`(`assigned_cs_user_id` ASC) USING BTREE,
  INDEX `idx_users_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'wechat', 'wx_openid_001', '三角洲战神', 'https://cdn.delta.com/avatars/user_001.png', 1, 3, '2026-01-10 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (2, 'wechat', 'wx_openid_002', '吃鸡达人小王', 'https://cdn.delta.com/avatars/user_002.png', 1, 3, '2026-01-15 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (3, 'wechat', 'wx_openid_003', '狙击手阿强', 'https://cdn.delta.com/avatars/user_003.png', 0, 4, '2026-02-01 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (4, 'wechat', 'wx_openid_004', '游戏小白', 'https://cdn.delta.com/avatars/user_004.png', 1, NULL, '2026-03-10 16:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (5, 'wechat', 'wx_openid_005', '老兵不死', 'https://cdn.delta.com/avatars/user_005.png', 1, 3, '2026-01-20 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (6, 'kook', 'kook_uid_001', 'K神降临', 'https://cdn.delta.com/avatars/user_006.png', 1, 4, '2026-02-10 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (7, 'kook', 'kook_uid_002', '枪王之王', 'https://cdn.delta.com/avatars/user_007.png', 1, 3, '2026-02-20 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (8, 'kook', 'kook_uid_003', '突击手小白', 'https://cdn.delta.com/avatars/user_008.png', 1, NULL, '2026-03-15 12:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (9, 'kook', 'kook_uid_004', '夜猫子玩家', 'https://cdn.delta.com/avatars/user_009.png', 1, 4, '2026-03-20 20:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (10, 'kook', 'kook_uid_005', '战术大师Leo', 'https://cdn.delta.com/avatars/user_010.png', 0, 3, '2026-04-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (11, 'yy', 'yy_uid_001', 'YY一哥', 'https://cdn.delta.com/avatars/user_011.png', 1, 3, '2026-03-01 14:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (12, 'yy', 'yy_uid_002', '颜值区扛把子', 'https://cdn.delta.com/avatars/user_012.png', 1, 4, '2026-04-05 19:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (13, 'yy', 'yy_uid_003', '冲锋战神', 'https://cdn.delta.com/avatars/user_013.png', 1, NULL, '2026-04-10 13:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (14, 'yy', 'yy_uid_004', '守点老王', 'https://cdn.delta.com/avatars/user_014.png', 1, 3, '2026-04-15 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (15, 'yy', 'yy_uid_005', '挂机专业户', 'https://cdn.delta.com/avatars/user_015.png', 0, NULL, '2026-04-20 22:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (16, 'test', 'test_uid_001', '测试用户A', 'https://cdn.delta.com/avatars/user_016.png', 1, 4, '2026-01-05 09:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (17, 'test', 'test_uid_002', '测试用户B', 'https://cdn.delta.com/avatars/user_017.png', 1, 3, '2026-01-08 10:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (18, 'test', 'test_uid_003', '测试用户C', 'https://cdn.delta.com/avatars/user_018.png', 0, NULL, '2026-02-28 15:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (19, 'test', 'test_uid_004', '测试用户D', 'https://cdn.delta.com/avatars/user_019.png', 1, NULL, '2026-04-25 11:00:00', '2026-05-12 02:04:08', 0, NULL);
INSERT INTO `users` VALUES (20, 'test', 'test_uid_005', '测试流失用户', 'https://cdn.delta.com/avatars/user_020.png', 1, NULL, '2026-01-01 08:00:00', '2026-05-12 02:04:08', 0, NULL);

-- ----------------------------
-- Table structure for work_order_attachments
-- ----------------------------
DROP TABLE IF EXISTS `work_order_attachments`;
CREATE TABLE `work_order_attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint NOT NULL COMMENT '关联工单ID',
  `record_id` bigint NULL DEFAULT NULL COMMENT '关联记录ID',
  `file_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型：IMAGE-图片，VIDEO-视频，DOCUMENT-文档，AUDIO-音频',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `uploaded_by` bigint NULL DEFAULT NULL COMMENT '上传人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_order_attachments_work_order_id`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_woa_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_order_attachments
-- ----------------------------
INSERT INTO `work_order_attachments` VALUES (1, 1, 2, '聊天记录截图.png', '/uploads/work_orders/1/chat_record.png', 'IMAGE', 256000, 4, '2026-03-16 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_attachments` VALUES (2, 2, 5, '服务录音.mp3', '/uploads/work_orders/2/service_record.mp3', 'AUDIO', 5120000, 4, '2026-03-25 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_attachments` VALUES (3, 2, 5, '客户评价截图.png', '/uploads/work_orders/2/review.png', 'IMAGE', 128000, 4, '2026-03-25 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_attachments` VALUES (4, 3, 7, '订单异常日志.pdf', '/uploads/work_orders/3/error_log.pdf', 'DOCUMENT', 102400, 3, '2026-05-01 23:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_attachments` VALUES (5, 6, 11, '服务介绍文档.pdf', '/uploads/work_orders/6/service_intro.pdf', 'DOCUMENT', 204800, 4, '2026-05-03 14:00:00', '2026-05-12 02:04:10', 0, NULL);

-- ----------------------------
-- Table structure for work_order_records
-- ----------------------------
DROP TABLE IF EXISTS `work_order_records`;
CREATE TABLE `work_order_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `record_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记录类型：STATUS_CHANGE-状态变更，HANDLE_RECORD-处理记录，INTERNAL_NOTE-内部备注，SYSTEM_LOG-系统日志',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人名称',
  `operator_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人角色',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `old_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '旧状态',
  `new_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wor_work_order_id`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_wor_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_wor_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_order_records
-- ----------------------------
INSERT INTO `work_order_records` VALUES (1, 1, 'STATUS_CHANGE', 4, '王五', 'CS_STAFF', '受理投诉工单，开始调查', 'NEW', 'PROCESSING', '2026-03-15 17:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (2, 1, 'HANDLE_RECORD', 4, '王五', 'CS_STAFF', '已核实：陪玩师周星星确实迟到15分钟，游戏技术不达标。已对陪玩师进行警告并安排退款。', 'PROCESSING', 'PROCESSING', '2026-03-16 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (3, 1, 'STATUS_CHANGE', 4, '王五', 'CS_STAFF', '投诉处理完毕，全额退款', 'PROCESSING', 'COMPLETED', '2026-03-16 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (4, 2, 'STATUS_CHANGE', 4, '王五', 'CS_STAFF', '收到紧急投诉，立即受理', 'NEW', 'PROCESSING', '2026-03-24 23:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (5, 2, 'HANDLE_RECORD', 4, '王五', 'CS_STAFF', '已核实：陪玩师吴天在服务中频繁催促，态度恶劣。决定：陪玩师停权处理，安排合规培训，全额退款。', 'PROCESSING', 'PROCESSING', '2026-03-25 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (6, 2, 'STATUS_CHANGE', 2, '张三', 'CS_LEADER', '审核通过处理方案，批准陪玩师停权和退款', 'PROCESSING', 'COMPLETED', '2026-03-25 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (7, 3, 'STATUS_CHANGE', 3, '李四', 'CS_STAFF', '受理异常订单工单', 'NEW', 'PROCESSING', '2026-05-01 23:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (8, 3, 'HANDLE_RECORD', 3, '李四', 'CS_STAFF', '已联系客户未接通，留言等待回复', 'PROCESSING', 'PROCESSING', '2026-05-02 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (9, 5, 'STATUS_CHANGE', 3, '李四', 'CS_STAFF', '客户再次投诉，工单升级', 'NEW', 'PROCESSING', '2026-05-02 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (10, 5, 'INTERNAL_NOTE', 2, '张三', 'CS_LEADER', '该客户近期投诉频繁，建议安排专人跟进，必要时升级VIP服务', 'PROCESSING', 'PROCESSING', '2026-05-03 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (11, 6, 'HANDLE_RECORD', 4, '王五', 'CS_STAFF', '向客户详细介绍了服务内容、价格方案和新客优惠', 'NEW', 'PROCESSING', '2026-05-03 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (12, 6, 'STATUS_CHANGE', 4, '王五', 'CS_STAFF', '咨询完毕，客户满意', 'PROCESSING', 'CLOSED', '2026-05-04 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (13, 8, 'HANDLE_RECORD', 3, '李四', 'CS_STAFF', '提供课程方案A（基础入门）和方案B（进阶提升），客户选择了方案B', 'NEW', 'PROCESSING', '2026-04-01 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (14, 8, 'STATUS_CHANGE', 3, '李四', 'CS_STAFF', '客户已选课并完成支付', 'PROCESSING', 'COMPLETED', '2026-04-02 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (15, 15, 'HANDLE_RECORD', 3, '李四', 'CS_STAFF', '正在向用户介绍新客教学套餐和优惠活动', 'NEW', 'PROCESSING', '2026-05-12 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (16, 3, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:20', '2026-05-12 02:07:20', 0, NULL);
INSERT INTO `work_order_records` VALUES (17, 4, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:20', '2026-05-12 02:07:20', 0, NULL);
INSERT INTO `work_order_records` VALUES (18, 5, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:20', '2026-05-12 02:07:20', 0, NULL);
INSERT INTO `work_order_records` VALUES (19, 7, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:20', '2026-05-12 02:07:20', 0, NULL);
INSERT INTO `work_order_records` VALUES (20, 10, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:21', '2026-05-12 02:07:21', 0, NULL);
INSERT INTO `work_order_records` VALUES (21, 12, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:21', '2026-05-12 02:07:21', 0, NULL);
INSERT INTO `work_order_records` VALUES (22, 14, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 02:07:21', '2026-05-12 02:07:21', 0, NULL);
INSERT INTO `work_order_records` VALUES (23, 15, 'SYSTEM_LOG', NULL, '系统', NULL, '工单处理超时，已发送提醒', NULL, NULL, '2026-05-12 08:30:18', '2026-05-12 08:30:18', 0, NULL);
INSERT INTO `work_order_records` VALUES (24, 15, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 09:00:18', '2026-05-12 09:00:18', 0, NULL);
INSERT INTO `work_order_records` VALUES (25, 16, 'SYSTEM_LOG', NULL, '系统', NULL, '工单创建，来源：SYSTEM', NULL, NULL, '2026-05-12 11:48:52', '2026-05-12 11:48:52', 0, NULL);
INSERT INTO `work_order_records` VALUES (26, 17, 'SYSTEM_LOG', NULL, '系统', NULL, '工单创建，来源：SYSTEM', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);
INSERT INTO `work_order_records` VALUES (27, 18, 'SYSTEM_LOG', NULL, '系统', NULL, '工单创建，来源：SYSTEM', NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);
INSERT INTO `work_order_records` VALUES (28, 16, 'SYSTEM_LOG', NULL, '系统', NULL, '工单处理超时，已发送提醒', NULL, NULL, '2026-05-12 12:19:10', '2026-05-12 12:19:10', 0, NULL);
INSERT INTO `work_order_records` VALUES (29, 17, 'SYSTEM_LOG', NULL, '系统', NULL, '工单处理超时，已发送提醒', NULL, NULL, '2026-05-12 12:21:08', '2026-05-12 12:21:08', 0, NULL);
INSERT INTO `work_order_records` VALUES (30, 18, 'SYSTEM_LOG', NULL, '系统', NULL, '工单处理超时，已发送提醒', NULL, NULL, '2026-05-12 12:21:08', '2026-05-12 12:21:08', 0, NULL);
INSERT INTO `work_order_records` VALUES (31, 16, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 12:49:08', '2026-05-12 12:49:08', 0, NULL);
INSERT INTO `work_order_records` VALUES (32, 17, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 12:51:08', '2026-05-12 12:51:08', 0, NULL);
INSERT INTO `work_order_records` VALUES (33, 18, 'SYSTEM_LOG', NULL, '系统', NULL, '工单已升级到负责人处理', NULL, NULL, '2026-05-12 12:51:08', '2026-05-12 12:51:08', 0, NULL);

-- ----------------------------
-- Table structure for work_order_sla
-- ----------------------------
DROP TABLE IF EXISTS `work_order_sla`;
CREATE TABLE `work_order_sla`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint NOT NULL COMMENT '关联工单ID',
  `priority_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优先级：URGENT(15分钟) / HIGH(30分钟) / NORMAL(60分钟) / LOW(120分钟)',
  `deadline_time` datetime NOT NULL COMMENT 'SLA截止时间',
  `warn_time` datetime NULL DEFAULT NULL COMMENT '预警时间（截止前N分钟预警）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-等待中 / WARNED-已预警 / BREACHED-已超时 / COMPLETED-已完成',
  `alert_sent` int NULL DEFAULT 0 COMMENT '是否已发送告警通知：0-未发送，1-已发送',
  `breach_minutes` int NULL DEFAULT NULL COMMENT '超时时长（分钟，实际处理时间 - 截止时间）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wsla_work_order_id`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_wsla_status`(`status` ASC) USING BTREE,
  INDEX `idx_wsla_deadline`(`deadline_time` ASC) USING BTREE,
  INDEX `idx_wsla_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单SLA追踪表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_order_sla
-- ----------------------------

-- ----------------------------
-- Table structure for work_orders
-- ----------------------------
DROP TABLE IF EXISTS `work_orders`;
CREATE TABLE `work_orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单编号',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID',
  `order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工单类型：CONSULT-咨询，BOOKING-预约，COMPLAINT-投诉，REFUND-退款，SERVICE_TRACK-服务追踪，OTHER-其他',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '优先级：NORMAL-普通，URGENT-紧急，CRITICAL-特急',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平台',
  `user_id` bigint NULL DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户姓名',
  `customer_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户联系方式',
  `customer_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户等级',
  `service_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `service_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务状态：PRE_SERVICE-服务前，IN_SERVICE-服务中，POST_SERVICE-服务后',
  `problem_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '问题描述',
  `problem_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '问题分类',
  `trigger_keyword` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发关键词',
  `context_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '上下文摘要',
  `assigned_cs_user_id` bigint NULL DEFAULT NULL COMMENT '分配的客服ID',
  `assigned_cs_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分配的客服名称',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理人名称',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理结果',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NEW' COMMENT '工单状态：NEW-新建，PROCESSING-处理中，PENDING_CONFIRM-待确认，COMPLETED-已完成，CLOSED-已关闭，CANCELLED-已取消',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `escalation_level` int NULL DEFAULT 0 COMMENT '升级级别',
  `reminder_count` int NULL DEFAULT 0 COMMENT '提醒次数',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `closed_at` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `related_companion_id` bigint NULL DEFAULT NULL COMMENT '关联陪玩师ID',
  `satisfaction_score` int NULL DEFAULT NULL COMMENT '满意度评分',
  `satisfaction_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '满意度备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_orders_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_work_orders_assigned_cs_user_id`(`assigned_cs_user_id` ASC) USING BTREE,
  INDEX `idx_work_orders_status_priority`(`status` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_work_orders_order_type`(`order_type` ASC) USING BTREE,
  INDEX `idx_work_orders_platform`(`platform` ASC) USING BTREE,
  INDEX `idx_wo_status_deleted_ctime`(`status` ASC, `deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_work_orders_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_work_orders_cs_status`(`assigned_cs_user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_work_orders_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_work_orders_status_escalation`(`status` ASC, `escalation_level` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_work_orders_assigned_cs`(`assigned_cs_user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_orders
-- ----------------------------
INSERT INTO `work_orders` VALUES (1, 'WO-20260315-001', 3, 'COMPLAINT', 'URGENT', 'wechat', 3, '狙击手阿强', '13800001002', 'SILVER', 'TEACHING', 'POST_SERVICE', '陪玩师迟到半小时，而且技术不如预期，要求退款和道歉', '服务投诉', '退款,投诉,迟到', '客户订单ORD-20260315-003，陪玩师周星星迟到15分钟，游戏技术不达标', 4, '王五', 4, '王五', '已核实迟到情况，对陪玩师进行警告处罚，为客户全额退款', 'COMPLETED', '2026-03-16 18:00:00', 1, 2, '2026-03-16 15:00:00', NULL, 3, 5, 3, '退款处理中', '2026-03-15 16:30:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (2, 'WO-20260324-002', 7, 'COMPLAINT', 'CRITICAL', 'kook', 9, '夜猫子玩家', 'kook_uid_004', 'BRONZE', 'ACCOMPANY', 'POST_SERVICE', '陪玩师一直催单，态度恶劣，要求退款！', '服务投诉', '投诉,退款,态度差', '订单ORD-20260324-007，陪玩师吴天在服务过程中频繁催促结束，态度恶劣', 4, '王五', 4, '王五', '已对陪玩师进行停权处理和合规培训安排，为客全额退款', 'COMPLETED', '2026-03-25 18:00:00', 2, 3, '2026-03-25 12:00:00', NULL, 7, 6, 1, '极不满意', '2026-03-24 22:30:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (3, 'WO-20260501-003', 15, 'SERVICE_TRACK', 'NORMAL', 'yy', 15, '挂机专业户', 'yy_uid_005', 'NORMAL', 'ACCOMPANY', 'POST_SERVICE', '订单异常中止，客户中途退出，需要调查原因', '服务异常', '异常,退出', '订单ORD-20260501-015，客户在服务开始15分钟后突然退出', 3, '李四', 3, '李四', '正在联系客户了解退出原因', 'PROCESSING', '2026-05-15 18:00:00', 2, 1, NULL, NULL, 15, 5, NULL, NULL, '2026-05-01 22:30:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (4, 'WO-20260510-004', 18, 'REFUND', 'NORMAL', 'test', 16, '测试用户A', 'test_uid_001', 'DIAMOND', 'SOCIAL', 'PRE_SERVICE', '客户取消订单请求退款', '退款请求', '取消,退款', '订单ORD-20260510-018，客户在服务开始前取消', 3, '李四', NULL, NULL, NULL, 'NEW', '2026-05-12 18:00:00', 2, 0, NULL, NULL, 18, NULL, NULL, NULL, '2026-05-10 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (5, 'WO-20260501-005', 15, 'SERVICE_TRACK', 'URGENT', 'yy', 15, '挂机专业户', 'yy_uid_005', 'NORMAL', 'ACCOMPANY', 'POST_SERVICE', '客户多次投诉订单异常，需要紧急处理', '服务异常', '多次投诉', '该客户近期投诉频率非常高，需要重点关注', 3, '李四', NULL, NULL, NULL, 'PROCESSING', '2026-05-13 18:00:00', 2, 3, NULL, NULL, 15, 5, NULL, NULL, '2026-05-02 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (6, 'WO-20260503-006', NULL, 'CONSULT', 'NORMAL', 'wechat', 4, '游戏小白', 'wx_openid_004', 'NORMAL', 'ACCOMPANY', 'PRE_SERVICE', '咨询陪玩师价格和服务内容', '服务咨询', '价格,咨询', '新客户想了解陪玩服务的具体内容和价格方案', 4, '王五', 4, '王五', '已详细介绍服务内容和价格，客户表示考虑中', 'CLOSED', '2026-05-05 18:00:00', 0, 0, '2026-05-04 15:00:00', NULL, NULL, NULL, 5, '非常满意', '2026-05-03 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (7, 'WO-20260504-007', NULL, 'BOOKING', 'NORMAL', 'kook', 8, '突击手小白', 'kook_uid_003', 'NORMAL', 'ACCOMPANY', 'PRE_SERVICE', '客户通过AI系统自助预约陪玩服务', '预约服务', '预约,陪玩', '客户通过AI系统匹配后自动生成预约工单', 3, '李四', NULL, NULL, NULL, 'NEW', '2026-05-06 18:00:00', 2, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-04 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (8, 'WO-20260401-008', NULL, 'CONSULT', 'NORMAL', 'test', 17, '测试用户B', 'test_uid_002', 'PLATINUM', 'TEACHING', 'PRE_SERVICE', '咨询游戏教学的课程安排和收费标准', '课程咨询', '教学,课程,收费', '老客户想报名教学课程', 3, '李四', 3, '李四', '已提供课程方案，客户已选课并下单', 'COMPLETED', '2026-04-03 18:00:00', 0, 1, '2026-04-02 10:00:00', NULL, NULL, NULL, 4, '满意', '2026-04-01 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (9, 'WO-20260505-009', NULL, 'SERVICE_TRACK', 'NORMAL', 'wechat', 14, '守点老王', 'yy_uid_004', 'GOLD', 'PACKAGE', 'POST_SERVICE', '服务完成后的售后服务跟踪', '售后跟踪', '售后,回访', '套餐服务完成后的常规售后回访', 3, '李四', NULL, NULL, NULL, 'PENDING_CONFIRM', '2026-05-07 18:00:00', 0, 0, NULL, NULL, 9, 3, NULL, NULL, '2026-05-05 15:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (10, 'WO-20260508-010', NULL, 'OTHER', 'NORMAL', 'test', 18, '测试用户C', 'test_uid_003', 'NORMAL', NULL, NULL, '客户反馈APP界面卡顿问题', '技术反馈', '卡顿,APP', '客户使用APP时遇到界面卡顿问题', 3, '李四', 3, '李四', '已转交技术团队处理', 'PROCESSING', '2026-05-10 18:00:00', 2, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-08 12:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (11, 'WO-20260509-011', NULL, 'CONSULT', 'NORMAL', 'yy', 11, 'YY一哥', 'yy_uid_001', 'BRONZE', 'SOCIAL', 'PRE_SERVICE', '咨询社交陪聊服务的具体内容和时间安排', '服务咨询', '社交,陪聊', '客户对社交服务感兴趣', 4, '王五', 4, '王五', '已介绍服务内容，客户预约了明天下午时段', 'COMPLETED', '2026-05-10 18:00:00', 0, 0, '2026-05-09 16:00:00', NULL, NULL, NULL, 4, '满意', '2026-05-09 14:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (12, 'WO-20260510-012', NULL, 'BOOKING', 'NORMAL', 'kook', 7, '枪王之王', 'kook_uid_002', 'GOLD', 'ACCOMPANY', 'PRE_SERVICE', '客户通过AI自动匹配预约顶尖陪玩师', '自动预约', '自动,匹配,预约', 'AI系统自动匹配后生成预约', 3, '李四', NULL, NULL, NULL, 'NEW', '2026-05-12 18:00:00', 2, 0, NULL, NULL, 12, 1, NULL, NULL, '2026-05-10 09:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (13, 'WO-20260420-013', NULL, 'COMPLAINT', 'URGENT', 'wechat', 2, '吃鸡达人小王', 'wx_openid_002', 'GOLD', 'ACCOMPANY', 'POST_SERVICE', '对服务价格提出质疑，认为性价比不高', '价格投诉', '价格,贵,性价比', '客户认为300元/小时定价偏高', 4, '王五', 2, '张三', '已向客户解释定价逻辑并提供优惠券补偿', 'COMPLETED', '2026-04-22 18:00:00', 1, 1, '2026-04-21 15:00:00', NULL, 2, 3, 4, '得到补偿后满意', '2026-04-20 21:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (14, 'WO-20260511-014', NULL, 'REFUND', 'NORMAL', 'test', 16, '测试用户A', 'test_uid_001', 'DIAMOND', 'SOCIAL', 'PRE_SERVICE', '客户申请订单取消退款', '退款请求', '取消,退款', '订单ORD-20260510-018客户主动取消', 4, '王五', NULL, NULL, NULL, 'NEW', '2026-05-13 18:00:00', 2, 0, NULL, NULL, 18, NULL, NULL, NULL, '2026-05-11 10:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (15, 'WO-20260512-015', NULL, 'CONSULT', 'NORMAL', 'wechat', 19, '测试用户D', 'test_uid_004', 'NORMAL', 'TEACHING', 'PRE_SERVICE', '新用户首次咨询教学服务详情', '新手咨询', '新手,教学,咨询', '新注册用户对教学服务感兴趣', 3, '李四', 3, '李四', '正在进行详细沟通中', 'PROCESSING', '2026-05-14 18:00:00', 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-12 08:00:00', '2026-05-12 02:04:10', 0, NULL);
INSERT INTO `work_orders` VALUES (16, 'WO202605120004', NULL, 'BOOKING', 'NORMAL', 'SYSTEM', 1, '三角洲战神', NULL, NULL, 'ACCOMPANY_PLAY', NULL, '订单 ORD20260512717000272 自动创建的关联工单', NULL, NULL, '订单编号：ORD20260512717000272，陪玩师：技术刘老师，服务类型：ACCOMPANY_PLAY，预约时间：2026-05-14T20:00 ~ 2026-05-14T22:00', NULL, NULL, NULL, NULL, NULL, 'NEW', '2026-05-12 12:18:52', 2, 1, NULL, NULL, NULL, 9, NULL, NULL, '2026-05-12 11:48:52', '2026-05-12 11:48:52', 0, NULL);
INSERT INTO `work_orders` VALUES (17, 'WO202605120005', NULL, 'BOOKING', 'NORMAL', 'SYSTEM', 1, '三角洲战神', NULL, NULL, 'ACCOMPANY_PLAY', NULL, '订单 ORD20260512332600766 自动创建的关联工单', NULL, NULL, '订单编号：ORD20260512332600766，陪玩师：技术刘老师，服务类型：ACCOMPANY_PLAY，预约时间：2026-05-15T20:00 ~ 2026-05-15T22:00', NULL, NULL, NULL, NULL, NULL, 'NEW', '2026-05-12 12:20:34', 2, 1, NULL, NULL, NULL, 9, NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);
INSERT INTO `work_orders` VALUES (18, 'WO202605120006', NULL, 'BOOKING', 'NORMAL', 'SYSTEM', 16, '测试用户A', NULL, NULL, 'GAME_TUTORING', NULL, '订单 ORD20260512380500974 自动创建的关联工单', NULL, NULL, '订单编号：ORD20260512380500974，陪玩师：独秀同志，服务类型：GAME_TUTORING，预约时间：2026-05-16T14:00 ~ 2026-05-16T16:00', NULL, NULL, NULL, NULL, NULL, 'NEW', '2026-05-12 12:20:34', 2, 1, NULL, NULL, NULL, 12, NULL, NULL, '2026-05-12 11:50:34', '2026-05-12 11:50:34', 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;
