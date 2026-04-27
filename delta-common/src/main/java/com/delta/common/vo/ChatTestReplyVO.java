package com.delta.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天测试回复视图对象
 *
 * @author delta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatTestReplyVO {

    /** 回复内容 */    private String replyContent;
    /** 是否AI回复 */
    private Boolean aiReply;
    /** 是否关键词触发 */    private Boolean keywordTriggered;
    /** 匹配的关键词 */    private String matchedKeyword;
    /** 关联的消息ID */    private Long messageId;

    /** 回复来源标识 */    private String responseSource;

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Boolean getAiReply() {
        return aiReply;
    }

    public void setAiReply(Boolean aiReply) {
        this.aiReply = aiReply;
    }

    public Boolean getKeywordTriggered() {
        return keywordTriggered;
    }

    public void setKeywordTriggered(Boolean keywordTriggered) {
        this.keywordTriggered = keywordTriggered;
    }

    public String getMatchedKeyword() {
        return matchedKeyword;
    }

    public void setMatchedKeyword(String matchedKeyword) {
        this.matchedKeyword = matchedKeyword;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
