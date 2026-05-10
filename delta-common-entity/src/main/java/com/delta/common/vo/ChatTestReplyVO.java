package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天测试回复视图对象")
public class ChatTestReplyVO extends BaseVO {

    @Schema(description = "回复内容", example = "您好，有什么可以帮您？")
    private String replyContent;

    @Schema(description = "是否AI回复", example = "true")
    private Boolean aiReply;

    @Schema(description = "是否关键词触发", example = "false")
    private Boolean keywordTriggered;

    @Schema(description = "匹配的关键词", example = "退款")
    private String matchedKeyword;

    @Schema(description = "消息ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long messageId;

    @Schema(description = "响应来源", example = "AI", allowableValues = {"AI", "KEYWORD", "MANUAL"})
    private String responseSource;

    @Schema(description = "是否创建了待处理消息", example = "true")
    private Boolean pendingMessageCreated;

    @Schema(description = "待处理消息创建失败原因", example = "未匹配到干预规则")
    private String pendingFailureReason;
}
