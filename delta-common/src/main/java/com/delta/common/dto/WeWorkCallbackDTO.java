package com.delta.common.dto;

import lombok.Data;

@Data
public class WeWorkCallbackDTO {

    private String msgType;
    private String eventType;
    private String fromUserName;
    private String content;
    private String toUserName;
    private String externalUserId;
    private String userId;
    private Long timestamp;
}
