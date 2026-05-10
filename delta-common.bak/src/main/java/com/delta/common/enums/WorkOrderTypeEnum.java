package com.delta.common.enums;

import com.delta.common.constant.WorkOrderConstants;

public enum WorkOrderTypeEnum {

    CONSULT(WorkOrderConstants.TYPE_CONSULT, "咨询类"),
    BOOKING(WorkOrderConstants.TYPE_BOOKING, "预约类"),
    COMPLAINT(WorkOrderConstants.TYPE_COMPLAINT, "投诉类"),
    REFUND(WorkOrderConstants.TYPE_REFUND, "退款类"),
    SERVICE_TRACK(WorkOrderConstants.TYPE_SERVICE_TRACK, "服务追踪"),
    OTHER(WorkOrderConstants.TYPE_OTHER, "其他");

    private final String code;
    private final String desc;

    WorkOrderTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkOrderTypeEnum fromCode(String code) {
        for (WorkOrderTypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return OTHER;
    }

    public static WorkOrderTypeEnum fromInterventionType(String interventionType) {
        if (interventionType == null) return OTHER;
        return switch (interventionType) {
            case "ORDER" -> BOOKING;
            case "SCHEDULE" -> CONSULT;
            case "SPECIFIC_COMPANION" -> BOOKING;
            case "COMPLAINT" -> COMPLAINT;
            case "HUMAN_REQUEST" -> CONSULT;
            default -> OTHER;
        };
    }
}
