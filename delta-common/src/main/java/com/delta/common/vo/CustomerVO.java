package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String platform;
    private String platformUserId;
    private String nickname;
    private String avatar;
    private Boolean aiEnabled;
    @ObfuscatedId
    private Long assignedCsUserId;
    private String assignedCsUserName;
    private Integer messageCount;
    private LocalDateTime lastActiveAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
