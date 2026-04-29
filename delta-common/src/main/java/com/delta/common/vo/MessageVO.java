package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息视图对象")
public class MessageVO extends BaseVO {

    @Schema(description = "消息ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "消息方向", example = "IN", allowableValues = {"IN", "OUT"})
    private String direction;

    @Schema(description = "消息内容", example = "你好，我想咨询陪玩服务")
    private String content;

    @Schema(description = "是否AI回复", example = "true")
    private Boolean ai;

    @Schema(description = "是否关键词触发", example = "false")
    private Boolean keywordTriggered;

    @Schema(description = "用户昵称", example = "小明")
    private String userNickname;

    @Schema(description = "用户平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String userPlatform;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
