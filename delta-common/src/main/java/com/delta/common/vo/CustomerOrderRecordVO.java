package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerOrderRecordVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long userId;
    private String userNickname;
    @ObfuscatedId
    private Long companionId;
    private String companionName;
    private String orderType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    private BigDecimal durationHours;
    private BigDecimal amount;
    private String gameType;
    private String companionLevel;
    private String timeSlot;
    private Integer rating;
    private String reviewContent;
    private String status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
