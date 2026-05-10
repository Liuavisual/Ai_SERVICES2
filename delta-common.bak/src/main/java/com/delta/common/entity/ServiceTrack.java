package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_tracks")
public class ServiceTrack extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String trackType;

    private Long relatedId;

    private String trackStatus;

    private String currentStep;

    @TableField(value = "track_data", jdbcType = JdbcType.VARCHAR)
    private String trackData;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    private Integer durationSeconds;

    private String result;

    @TableField(exist = false)
    private Long workOrderId;

    @TableField(exist = false)
    private LocalDateTime consultStartedAt;

    @TableField(exist = false)
    private String consultContent;

    @TableField(exist = false)
    private LocalDateTime bookedAt;

    @TableField(exist = false)
    private Long bookedCompanionId;

    @TableField(exist = false)
    private String bookedCompanionName;

    @TableField(exist = false)
    private String bookedServiceType;

    @TableField(exist = false)
    private String bookedTimeSlot;

    @TableField(exist = false)
    private Long relatedOrderId;

    @TableField(exist = false)
    private LocalDateTime serviceStartedAt;

    @TableField(exist = false)
    private Long serviceCompanionId;

    @TableField(exist = false)
    private String serviceCompanionName;

    @TableField(exist = false)
    private Integer serviceDuration;

    @TableField(exist = false)
    private LocalDateTime serviceEndedAt;

    @TableField(exist = false)
    private String serviceResult;

    @TableField(exist = false)
    private LocalDateTime confirmedAt;

    @TableField(exist = false)
    private Integer customerRating;

    @TableField(exist = false)
    private String customerFeedback;
}
