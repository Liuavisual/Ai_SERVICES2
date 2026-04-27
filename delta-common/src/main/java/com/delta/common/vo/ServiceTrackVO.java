package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceTrackVO {

    private Long id;
    private Long workOrderId;
    private Long userId;
    private String trackStatus;
    private String trackStatusDesc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultStartedAt;
    private String consultContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;
    private Long bookedCompanionId;
    private String bookedCompanionName;
    private String bookedServiceType;
    private String bookedTimeSlot;
    private Long relatedOrderId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceStartedAt;
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
