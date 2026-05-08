package com.delta.common.enums;

/**
 * 人工介入类型枚举
 * <p>
 * 定义待处理消息的介入类型，用于分类客户需要人工服务的原因。
 * 当系统检测到转人工关键词时，通过 {@link #fromKeyword(String)} 自动分类。
 * </p>
 *
 * @author 刘建国
 */
public enum InterventionTypeEnum {

    /** 预约/下单 - 客户要预约陪玩服务或下单 */
    ORDER("ORDER", "预约/下单"),
    /** 排班查询 - 客户询问谁在线、有没有人接单 */
    SCHEDULE("SCHEDULE", "排班查询"),
    /** 指定陪玩师 - 客户要求找特定的陪玩师 */
    SPECIFIC_COMPANION("SPECIFIC_COMPANION", "指定陪玩师"),
    /** 投诉/退款 - 客户投诉、退款或表达负面情绪 */
    COMPLAINT("COMPLAINT", "投诉/退款"),
    /** 客户要求人工 - 客户主动要求转人工或AI连续未解决 */
    HUMAN_REQUEST("HUMAN_REQUEST", "客户要求人工"),
    /** 其他 - 无法归类的转人工原因 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    InterventionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据触发关键词自动判断介入类型
     *
     * @param keyword 触发转人工的关键词
     * @return 对应的介入类型枚举，无法匹配时返回 OTHER
     */
    public static InterventionTypeEnum fromKeyword(String keyword) {
        if (keyword == null) return OTHER;

        if (keyword.contains("人工") || keyword.contains("转人工")) return HUMAN_REQUEST;
        if (keyword.contains("预约") || keyword.contains("下单") || keyword.contains("付款")) return ORDER;
        if (keyword.contains("有人接") || keyword.contains("谁在线") || keyword.contains("有没有人")
                || keyword.contains("可以接") || keyword.contains("在不在") || keyword.contains("有空没")) return SCHEDULE;
        if (keyword.contains("我要找") || keyword.contains("指定") || keyword.contains("找某某")) return SPECIFIC_COMPANION;
        if (keyword.contains("退款") || keyword.contains("投诉") || keyword.contains("不满意")
                || keyword.contains("烦死") || keyword.contains("垃圾") || keyword.contains("骗人")
                || keyword.contains("差评") || keyword.contains("举报") || keyword.contains("气死")
                || keyword.contains("什么破") || keyword.contains("太差") || keyword.contains("恶心")
                || keyword.contains("不行") || keyword.contains("太烂") || keyword.contains("坑人")
                || keyword.contains("糊弄") || keyword.contains("敷衍") || keyword.contains("不靠谱")) return COMPLAINT;
        if (keyword.contains("AI连续未解决")) return HUMAN_REQUEST;

        return OTHER;
    }
}
