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

    /** 唤醒动作：通知客服关注 */
    public static final String ACTION_NOTIFY_CS = "NOTIFY_CS";

    /** 唤醒动作：发送优惠券 */
    public static final String ACTION_SEND_COUPON = "SEND_COUPON";

    /** 唤醒动作：标记VIP关怀 */
    public static final String ACTION_MARK_VIP = "MARK_VIP";

    /** 唤醒Redis Key前缀 */
    public static final String WAKEUP_KEY_PREFIX = "customer:wakeup:";

    /** 唤醒冷却时间（天），同一客户同一天不重复唤醒 */
    public static final int WAKEUP_COOLDOWN_DAYS = 1;

    /** 唤醒任务每次批量处理的最大客户数 */
    public static final int WAKEUP_BATCH_SIZE = 200;

    /** RFM综合得分高价值阈值 */
    public static final int RFM_HIGH_VALUE_THRESHOLD = 10;

    /** 忠实客户最小订单数 */
    public static final int LOYAL_MIN_ORDERS = 3;

    /** 活跃客户最小订单数 */
    public static final int ACTIVE_MIN_ORDERS = 1;

    /** 活跃客户最小消息数 */
    public static final int ACTIVE_MIN_MESSAGES = 5;

    /** 忠实客户最小消息数 */
    public static final int LOYAL_MIN_MESSAGES = 50;

    /** 私有构造方法，防止实例化 */
    private CustomerLifecycleConstants() {
    }
}
