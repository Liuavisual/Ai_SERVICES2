package com.delta.common.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    
    PENDING("PENDING", "待审核"),
    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "禁用");
    
    private final String code;
    private final String desc;
    
    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static UserStatusEnum fromCode(String code) {
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
