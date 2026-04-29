package com.delta.common.constant;

/**
 * 客户生命周期常量
 * <p>
 * 定义客户生命周期阶段、阈值和标签常量，
 * 用于客户流失预警和生命周期管理。</p>
 *
 * @author 刘建国
 */
public final class CustomerLifecycleConstants {

    /** 生命周期阶段：新客户 */
    public static final String STAGE_NEW = "NEW";

    /** 生命周期阶段：活跃客户 */
    public static final String STAGE_ACTIVE = "ACTIVE";

    /** 生命周期阶段：忠实客户 */
    public static final String STAGE_LOYAL = "LOYAL";

    /** 生命周期阶段：流失风险客户 */
    public static final String STAGE_AT_RISK = "AT_RISK";

    /** 生命周期阶段：已流失客户 */
    public static final String STAGE_CHURNED = "CHURNED";

    /** 流失风险阈值（天）：超过此天数未活跃视为流失风险 */
    public static final int AT_RISK_DAYS_THRESHOLD = 7;

    /** 已流失阈值（天）：超过此天数未活跃视为已流失 */
    public static final int CHURNED_DAYS_THRESHOLD = 30;

    /** 客户标签：高价值 */
    public static final String TAG_HIGH_VALUE = "高价值";

    /** 客户标签：流失风险 */
    public static final String TAG_AT_RISK = "流失风险";

    /** 客户标签：新客户 */
    public static final String TAG_NEW = "新客户";

    /** 客户标签：忠实客户 */
    public static final String TAG_LOYAL = "忠实客户";

    /** 私有构造方法，防止实例化 */
    private CustomerLifecycleConstants() {
    }
}
