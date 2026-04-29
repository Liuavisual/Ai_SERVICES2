package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微回调数据传输对象")
public class WeWorkCallbackDTO {

    @Schema(description = "消息类型", example = "text", allowableValues = {"text", "image", "event"})
    private String msgType;

    @Schema(description = "事件类型", example = "msg_audit", allowableValues = {"msg_audit", "change_contact", "change_external_contact"})
    private String eventType;

    @Schema(description = "发送者名称", example = "user001")
    private String fromUserName;

    @Schema(description = "消息内容", example = "你好，我想咨询陪玩服务")
    private String content;

    @Schema(description = "接收者名称", example = "system")
    private String toUserName;

    @Schema(description = "外部用户ID", example = "wmXXXXXX")
    private String externalUserId;

    @Schema(description = "企微用户ID", example = "user001")
    private String userId;

    @Schema(description = "时间戳", example = "1704067200000")
    private Long timestamp;
}
