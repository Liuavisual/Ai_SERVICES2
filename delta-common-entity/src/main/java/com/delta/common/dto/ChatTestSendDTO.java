package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天测试发送数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "聊天测试发送数据传输对象")
public class ChatTestSendDTO {

    @Schema(description = "客户昵称", example = "测试用户")
    @NotBlank(message = "客户昵称不能为空")
    private String customerNickname;

    @Schema(description = "来源平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "分配的客服ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long csUserId;

    @Schema(description = "消息内容", example = "你好，我想咨询陪玩服务")
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
