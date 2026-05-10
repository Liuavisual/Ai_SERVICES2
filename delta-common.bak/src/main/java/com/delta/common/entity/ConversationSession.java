package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation_sessions")
public class ConversationSession extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String platform;

    private String status;

    private String aiModel;

    private Integer messageCount;

    private Integer aiMessageCount;

    private Integer humanMessageCount;

    private java.time.LocalDateTime firstMessageAt;

    private java.time.LocalDateTime lastMessageAt;

    private Integer resolved;

    private String contextSummary;
}
