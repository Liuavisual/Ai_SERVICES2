package com.delta.common.enums;

import lombok.Getter;

/**
 * 待处理消息状态枚举
 * <p>
 * 定义待处理消息的生命周期状态，对应三态人工转接模型：
 * <ul>
 *   <li>PENDING（待处理）→ AI回复"正在安排客服"，等待客服接手</li>
 *   <li>PROCESSING（处理中）→ AI完全静默，客服正在服务</li>
 *   <li>RESOLVED（已解决）→ 流程结束，触发满意度评价</li>
 * </ul>
 * </p>
 *
 * @author delta
 */
@Getter
public enum PendingMessageStatusEnum {

    /** 待处理 - 等待客服接手，AI回复等待提示 */
    PENDING("pending", "待处理"),
    /** 处理中 - 客服已接手，AI完全静默 */
    PROCESSING("processing", "处理中"),
    /** 已解决 - 客服完成处理，触发满意度评价 */
    RESOLVED("resolved", "已解决");

    private final String code;
    private final String desc;

    PendingMessageStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
