package com.delta.common.dto;

import lombok.Data;

@Data
public class WeWorkSendMessageDTO {

    private String externalUserId;
    private String content;
    private String msgType;

    public static WeWorkSendMessageDTO text(String externalUserId, String content) {
        WeWorkSendMessageDTO dto = new WeWorkSendMessageDTO();
        dto.setExternalUserId(externalUserId);
        dto.setContent(content);
        dto.setMsgType("text");
        return dto;
    }
}
