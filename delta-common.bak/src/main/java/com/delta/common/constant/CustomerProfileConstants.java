package com.delta.common.constant;

import java.math.BigDecimal;

public final class CustomerProfileConstants {

    public static final String RFM_SEGMENT_CHAMPION = "CHAMPION";
    public static final String RFM_SEGMENT_LOYAL = "LOYAL";
    public static final String RFM_SEGMENT_POTENTIAL = "POTENTIAL";
    public static final String RFM_SEGMENT_NEW = "NEW";
    public static final String RFM_SEGMENT_AT_RISK = "AT_RISK";
    public static final String RFM_SEGMENT_HIBERNATE = "HIBERNATE";
    public static final String RFM_SEGMENT_LOST = "LOST";

    public static final int RFM_RECENCY_DAYS_TIER5 = 7;
    public static final int RFM_RECENCY_DAYS_TIER4 = 14;
    public static final int RFM_RECENCY_DAYS_TIER3 = 30;
    public static final int RFM_RECENCY_DAYS_TIER2 = 60;

    public static final double RFM_FREQUENCY_TIER5 = 3.0;
    public static final double RFM_FREQUENCY_TIER4 = 2.0;
    public static final double RFM_FREQUENCY_TIER3 = 1.0;
    public static final double RFM_FREQUENCY_TIER2 = 0.5;

    public static final BigDecimal RFM_MONETARY_TIER5 = BigDecimal.valueOf(5000);
    public static final BigDecimal RFM_MONETARY_TIER4 = BigDecimal.valueOf(2000);
    public static final BigDecimal RFM_MONETARY_TIER3 = BigDecimal.valueOf(500);
    public static final BigDecimal RFM_MONETARY_TIER2 = BigDecimal.valueOf(100);

    public static final String LIFECYCLE_NEW = "NEW";
    public static final String LIFECYCLE_ACTIVE = "ACTIVE";
    public static final String LIFECYCLE_SILENT = "SILENT";
    public static final String LIFECYCLE_CHURNED = "CHURNED";
    public static final String LIFECYCLE_REACTIVATED = "REACTIVATED";

    public static final long LIFECYCLE_NEW_MAX_DAYS = 7;
    public static final long LIFECYCLE_NEW_MAX_ORDERS = 1;
    public static final long LIFECYCLE_ACTIVE_MAX_INACTIVE_DAYS = 14;
    public static final long LIFECYCLE_SILENT_MAX_INACTIVE_DAYS = 30;

    public static final String MEMBER_LEVEL_NORMAL = "NORMAL";
    public static final String MEMBER_LEVEL_BRONZE = "BRONZE";
    public static final String MEMBER_LEVEL_SILVER = "SILVER";
    public static final String MEMBER_LEVEL_GOLD = "GOLD";
    public static final String MEMBER_LEVEL_PLATINUM = "PLATINUM";
    public static final String MEMBER_LEVEL_DIAMOND = "DIAMOND";

    public static final BigDecimal MEMBER_BRONZE_SPENT = BigDecimal.valueOf(100);
    public static final int MEMBER_BRONZE_ORDERS = 2;
    public static final BigDecimal MEMBER_SILVER_SPENT = BigDecimal.valueOf(500);
    public static final int MEMBER_SILVER_ORDERS = 5;
    public static final BigDecimal MEMBER_GOLD_SPENT = BigDecimal.valueOf(2000);
    public static final int MEMBER_GOLD_ORDERS = 15;
    public static final BigDecimal MEMBER_PLATINUM_SPENT = BigDecimal.valueOf(5000);
    public static final int MEMBER_PLATINUM_ORDERS = 30;
    public static final BigDecimal MEMBER_DIAMOND_SPENT = BigDecimal.valueOf(10000);
    public static final int MEMBER_DIAMOND_ORDERS = 50;

    public static final String RISK_LEVEL_LOW = "LOW";
    public static final String RISK_LEVEL_MEDIUM = "MEDIUM";
    public static final String RISK_LEVEL_HIGH = "HIGH";

    public static final BigDecimal RISK_THRESHOLD_HIGH = BigDecimal.valueOf(7);
    public static final BigDecimal RISK_THRESHOLD_MEDIUM = BigDecimal.valueOf(4);

    public static final String SPENDING_TREND_INCREASING = "INCREASING";
    public static final String SPENDING_TREND_STABLE = "STABLE";
    public static final String SPENDING_TREND_DECREASING = "DECREASING";

    public static final BigDecimal SPENDING_TREND_THRESHOLD = BigDecimal.valueOf(0.2);

    public static final String SATISFACTION_TREND_IMPROVING = "IMPROVING";
    public static final String SATISFACTION_TREND_STABLE = "STABLE";
    public static final String SATISFACTION_TREND_DECLINING = "DECLINING";

    public static final double SATISFACTION_TREND_THRESHOLD = 0.3;

    public static final String NEED_TYPE_EMOTIONAL = "EMOTIONAL";
    public static final String NEED_TYPE_SKILL = "SKILL";
    public static final String NEED_TYPE_SOCIAL = "SOCIAL";
    public static final String NEED_TYPE_ENTERTAINMENT = "ENTERTAINMENT";

    public static final int NEED_EMOTIONAL_TRIGGER_THRESHOLD = 2;
    public static final int NEED_EMOTIONAL_HANDOFF_THRESHOLD = 3;
    public static final int NEED_SKILL_INTENT_THRESHOLD = 2;
    public static final int NEED_SOCIAL_DIVERSITY_THRESHOLD = 3;

    public static final BigDecimal CHURN_RISK_AI_RATIO_THRESHOLD = BigDecimal.valueOf(0.8);
    public static final BigDecimal CHURN_RISK_SATISFACTION_LOW = BigDecimal.valueOf(3.0);
    public static final BigDecimal CHURN_RISK_SATISFACTION_MEDIUM = BigDecimal.valueOf(4.0);
    public static final double CHURN_RISK_MAX_SCORE = 10.0;

    public static final long SPENDING_TREND_RECENT_DAYS = 30;
    public static final long SPENDING_TREND_PREVIOUS_DAYS = 60;

    public static final long REPURCHASE_WINDOW_DAYS = 30;
    public static final long REPURCHASE_MIN_ORDERS = 2;

    public static final long LTV_ESTIMATED_RETENTION_MONTHS = 12;

    public static final int SATISFACTION_TREND_MIN_RATINGS = 4;
    public static final int SATISFACTION_TREND_QUERY_LIMIT = 10;

    public static final BigDecimal REPURCHASE_RATE_FULL = BigDecimal.ONE;
    public static final BigDecimal REPURCHASE_RATE_HALF = BigDecimal.valueOf(0.5);

    public static final String TIME_SLOT_EVENING = "晚上";
    public static final String TIME_SLOT_ALL_NIGHT = "通宵";

    public static final String ORDER_TYPE_SPECIFIC_GAME = "SPECIFIC_GAME";
    public static final String ORDER_TYPE_NIGHT_PACKAGE = "NIGHT_PACKAGE";

    public static final String NEED_TAG_EMOTIONAL = "情感陪伴";
    public static final String NEED_TAG_SKILL = "技能提升";
    public static final String NEED_TAG_SOCIAL = "社交拓展";
    public static final String NEED_TAG_ENTERTAINMENT = "娱乐消遣";
    public static final String NEED_TAG_SKILL_EMOTIONAL = "上分指导";
    public static final String NEED_TAG_SOCIAL_EMOTIONAL = "社交破圈";
    public static final String NEED_TAG_HIGH_FREQUENCY = "高频客户";
    public static final int NEED_TAG_HIGH_FREQUENCY_ORDERS = 10;
    public static final BigDecimal NEED_TAG_HIGH_SPENDING = BigDecimal.valueOf(2000);
    public static final int NEED_TAG_HIGH_SPENDING_THRESHOLD = 2000;
    public static final String NEED_TAG_NIGHT_PREFERENCE = "包夜偏好";
    public static final BigDecimal NEED_TAG_AI_DEPENDENCY_RATIO = BigDecimal.valueOf(0.7);
    public static final BigDecimal NEED_TAG_MANUAL_DEPENDENCY_AI_RATIO = BigDecimal.valueOf(0.7);
    public static final String NEED_TAG_MANUAL_DEPENDENCY = "人工依赖";

    private CustomerProfileConstants() {
    }
}
