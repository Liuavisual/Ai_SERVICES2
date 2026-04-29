package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 陪玩师排班实体
 * <p>
 * 对应数据库表 companion_schedules，记录陪玩师每日的可用时间段，
 * 支持按日期和时段排班，状态包括 AVAILABLE（可预约）、BOOKED（已预约）、UNAVAILABLE（不可用）。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companion_schedules")
@Table(name = "companion_schedules", indexes = {
        @Index(name = "idx_companion_schedules_companion_id", columnList = "companion_id"),
        @Index(name = "idx_companion_schedules_status", columnList = "status"),
        @Index(name = "idx_companion_schedules_companion_start", columnList = "companion_id,start_time")
})
public class CompanionSchedule extends BaseEntity {

    /** 关联的陪玩师ID */
    private Long companionId;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 时段标识，如 "上午"、"下午"、"晚上" */
    private String timeSlot;

    /** 开始时间 */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    /** 状态：AVAILABLE-可预约，BOOKED-已预约，UNAVAILABLE-不可用 */
    private String status;

    /** 备注信息 */
    private String remark;
}
