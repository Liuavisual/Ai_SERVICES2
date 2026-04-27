package com.delta.common.enums;

import com.delta.common.constant.WorkOrderConstants;

public enum WorkOrderStatusEnum {

    NEW(WorkOrderConstants.STATUS_NEW, "新建"),
    PROCESSING(WorkOrderConstants.STATUS_PROCESSING, "处理中"),
    PENDING_CONFIRM(WorkOrderConstants.STATUS_PENDING_CONFIRM, "待确认"),
    COMPLETED(WorkOrderConstants.STATUS_COMPLETED, "已完成"),
    CLOSED(WorkOrderConstants.STATUS_CLOSED, "已关闭"),
    CANCELLED(WorkOrderConstants.STATUS_CANCELLED, "已取消");

    private final String code;
    private final String desc;

    WorkOrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkOrderStatusEnum fromCode(String code) {
        for (WorkOrderStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    public static boolean isValidTransition(String from, String to) {
        if (from == null || to == null) return false;
        if (from.equals(to)) return false;

        return switch (from) {
            case "NEW" -> "PROCESSING".equals(to) || "CANCELLED".equals(to);
            case "PROCESSING" -> "PENDING_CONFIRM".equals(to) || "NEW".equals(to) || "CANCELLED".equals(to);
            case "PENDING_CONFIRM" -> "COMPLETED".equals(to) || "CLOSED".equals(to) || "PROCESSING".equals(to);
            case "COMPLETED" -> "CLOSED".equals(to);
            case "CLOSED" -> "PROCESSING".equals(to);
            default -> false;
        };
    }
}
