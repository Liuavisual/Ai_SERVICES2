package com.delta.common.enums;

import lombok.Getter;

@Getter
public enum PlatformEnum {

    WECHAT("wechat", "微信"),
    WEWORK("wework", "企业微信"),
    KOOK("kook", "KOOK"),
    YY("yy", "YY");

    private final String code;
    private final String desc;

    PlatformEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PlatformEnum fromCode(String code) {
        for (PlatformEnum platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        return null;
    }
}
