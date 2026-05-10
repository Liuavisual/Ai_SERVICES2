package com.delta.common.enums;

import lombok.Getter;

@Getter
public enum MessageDirectionEnum {

    IN("in", "接收"),
    OUT("out", "发送");

    private final String code;
    private final String desc;

    MessageDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
