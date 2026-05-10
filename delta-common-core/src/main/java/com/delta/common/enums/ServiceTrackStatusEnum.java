package com.delta.common.enums;

import com.delta.common.constant.WorkOrderConstants;

public enum ServiceTrackStatusEnum {

    CONSULTING(WorkOrderConstants.TRACK_STATUS_CONSULTING, "咨询中"),
    BOOKED(WorkOrderConstants.TRACK_STATUS_BOOKED, "已预约"),
    SERVICING(WorkOrderConstants.TRACK_STATUS_SERVICING, "服务中"),
    SERVICE_DONE(WorkOrderConstants.TRACK_STATUS_SERVICE_DONE, "服务完成"),
    CONFIRMED(WorkOrderConstants.TRACK_STATUS_CONFIRMED, "已确认");

    private final String code;
    private final String desc;

    ServiceTrackStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ServiceTrackStatusEnum fromCode(String code) {
        for (ServiceTrackStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return CONSULTING;
    }

    public static boolean isValidTransition(String from, String to) {
        if (from == null || to == null || from.equals(to)) return false;
        return switch (from) {
            case "CONSULTING" -> "BOOKED".equals(to);
            case "BOOKED" -> "SERVICING".equals(to);
            case "SERVICING" -> "SERVICE_DONE".equals(to);
            case "SERVICE_DONE" -> "CONFIRMED".equals(to);
            default -> false;
        };
    }
}
