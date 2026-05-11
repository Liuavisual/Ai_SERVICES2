package com.delta.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    
    SYS_ADMIN("SYS_ADMIN", "超级管理员"),
    CS_LEADER("CS_LEADER", "客服主管"),
    CS_STAFF("CS_STAFF", "客服人员"),
    COMPANION("COMPANION", "陪玩师");
    
    private final String code;
    private final String desc;
    
    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
