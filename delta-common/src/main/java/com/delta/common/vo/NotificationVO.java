package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationVO extends BaseVO {

    private String type;
    @ObfuscatedId
    private Long pendingMessageId;
    @ObfuscatedId
    private Long userId;
    private String userNickname;
    private String keyword;
    private String interventionType;
    private String platform;
    private String messageContent;
    private String deadline;
    private String contextSummary;
    private Long timestamp;
}
