package com.delta.common.enums;

import lombok.Getter;

@Getter
public enum ReplyTriggerTypeEnum {

    KEYWORD("keyword", "关键词"),
    WELCOME("welcome", "欢迎语"),
    DEFAULT("default", "默认回复");

    private final String code;
    private final String desc;

    ReplyTriggerTypeEnum(String code, String desc) {
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
