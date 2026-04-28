package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatTestReplyVO extends BaseVO {

    private String replyContent;
    private Boolean aiReply;
    private Boolean keywordTriggered;
    private String matchedKeyword;
    @ObfuscatedId
    private Long messageId;
    private String responseSource;
    private Boolean pendingMessageCreated;
    private String pendingFailureReason;
}
