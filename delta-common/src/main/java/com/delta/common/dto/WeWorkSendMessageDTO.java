package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微发送消息数据传输对象")
public class WeWorkSendMessageDTO {

    @Schema(description = "外部用户ID", example = "wmXXXXXX")
    private String externalUserId;

    @Schema(description = "消息内容", example = "您好，有什么可以帮您？")
    private String content;

    @Schema(description = "消息类型", example = "text", allowableValues = {"text", "image", "link", "miniprogram"})
    private String msgType;

    public static WeWorkSendMessageDTO text(String externalUserId, String content) {
        WeWorkSendMessageDTO dto = new WeWorkSendMessageDTO();
        dto.setExternalUserId(externalUserId);
        dto.setContent(content);
        dto.setMsgType("text");
        return dto;
    }
}
