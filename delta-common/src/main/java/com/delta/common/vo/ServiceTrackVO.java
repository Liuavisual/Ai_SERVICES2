package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTrackVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long workOrderId;
    @ObfuscatedId
    private Long userId;
    private String trackStatus;
    private String trackStatusDesc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultStartedAt;
    private String consultContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;
    @ObfuscatedId
    private Long bookedCompanionId;
    private String bookedCompanionName;
    private String bookedServiceType;
    private String bookedTimeSlot;
    @ObfuscatedId
    private Long relatedOrderId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceStartedAt;
    @ObfuscatedId
    private Long serviceCompanionId;
    private String serviceCompanionName;
    private Integer serviceDuration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceEndedAt;
    private String serviceResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;
    private Integer customerRating;
    private String customerFeedback;
}
