package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long userId;
    private String direction;
    private String content;
    private Boolean ai;
    private Boolean keywordTriggered;
    private String userNickname;
    private String userPlatform;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
