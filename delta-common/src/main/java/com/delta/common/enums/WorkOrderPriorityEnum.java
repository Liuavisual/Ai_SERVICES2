package com.delta.common.enums;

import com.delta.common.constant.WorkOrderConstants;

public enum WorkOrderPriorityEnum {

    NORMAL(WorkOrderConstants.PRIORITY_NORMAL, "普通", WorkOrderConstants.PRIORITY_NORMAL_TIMEOUT_MINUTES, WorkOrderConstants.PRIORITY_NORMAL_CONFIRM_DAYS),
    URGENT(WorkOrderConstants.PRIORITY_URGENT, "紧急", WorkOrderConstants.PRIORITY_URGENT_TIMEOUT_MINUTES, WorkOrderConstants.PRIORITY_URGENT_CONFIRM_DAYS),
    CRITICAL(WorkOrderConstants.PRIORITY_CRITICAL, "特急", WorkOrderConstants.PRIORITY_CRITICAL_TIMEOUT_MINUTES, WorkOrderConstants.PRIORITY_CRITICAL_CONFIRM_DAYS);

    private final String code;
    private final String desc;
    private final int timeoutMinutes;
    private final int confirmDays;

    WorkOrderPriorityEnum(String code, String desc, int timeoutMinutes, int confirmDays) {
        this.code = code;
        this.desc = desc;
        this.timeoutMinutes = timeoutMinutes;
        this.confirmDays = confirmDays;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public int getConfirmDays() {
        return confirmDays;
    }

    public static WorkOrderPriorityEnum fromCode(String code) {
        for (WorkOrderPriorityEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return NORMAL;
    }

    public static WorkOrderPriorityEnum fromInterventionType(String interventionType) {
        if (interventionType == null) return NORMAL;
        return switch (interventionType) {
            case "COMPLAINT" -> URGENT;
            default -> NORMAL;
        };
    }
}
