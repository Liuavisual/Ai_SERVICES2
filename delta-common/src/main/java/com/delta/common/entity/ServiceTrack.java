package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 服务追踪实体
 * <p>
 * 对应数据库表 service_tracks，记录客户从咨询到服务的完整追踪链路，
 * 包括咨询内容、预约信息、服务执行、客户反馈等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_tracks")
@Table(name = "service_tracks", indexes = {
        @Index(name = "idx_service_tracks_user_id", columnList = "user_id"),
        @Index(name = "idx_service_tracks_booked_companion_id", columnList = "booked_companion_id"),
        @Index(name = "idx_service_tracks_work_order_id", columnList = "work_order_id"),
        @Index(name = "idx_service_tracks_track_status", columnList = "track_status"),
        @Index(name = "idx_service_tracks_created_at", columnList = "created_at")
})
public class ServiceTrack extends BaseEntity {

    /** 关联工单ID */
    private Long workOrderId;

    /** 客户ID */
    private Long userId;

    /** 追踪状态 */
    private String trackStatus;

    /** 咨询开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultStartedAt;

    /** 咨询内容 */
    @TableField(value = "consult_content", jdbcType = JdbcType.LONGVARCHAR)
    private String consultContent;

    /** 预约时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;

    /** 预约陪玩师ID */
    private Long bookedCompanionId;

    /** 预约陪玩师名称 */
    private String bookedCompanionName;

    /** 预约服务类型 */
    private String bookedServiceType;

    /** 预约时段 */
    private String bookedTimeSlot;

    /** 关联订单ID */
    private Long relatedOrderId;

    /** 服务开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceStartedAt;

    /** 服务陪玩师ID */
    private Long serviceCompanionId;

    /** 服务陪玩师名称 */
    private String serviceCompanionName;

    /** 服务时长（分钟） */
    private Integer serviceDuration;

    /** 服务结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceEndedAt;

    /** 服务结果 */
    private String serviceResult;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;

    /** 客户评分 */
    private Integer customerRating;

    /** 客户反馈 */
    @TableField(value = "customer_feedback", jdbcType = JdbcType.LONGVARCHAR)
    private String customerFeedback;
}
