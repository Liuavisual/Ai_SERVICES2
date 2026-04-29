package com.delta.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 陪玩师排班数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "陪玩师排班数据传输对象")
public class CompanionScheduleDTO {

    @Schema(description = "排班ID", example = "1")
    private Long id;

    @Schema(description = "陪玩师ID", example = "2001")
    @NotNull(message = "陪玩师ID不能为空")
    /** 陪玩师ID */    private Long companionId;

    @Schema(description = "排班日期", example = "2026-01-15")
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    /** 排班日期 */    private LocalDate scheduleDate;

    @Schema(description = "时段", example = "MORNING", allowableValues = {"MORNING", "AFTERNOON", "EVENING", "FULL_DAY"})
    @NotBlank(message = "时间段不能为空")
    /** 时段 */    private String timeSlot;

    @Schema(description = "开始时间", example = "09:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    /** 开始时间 */    private LocalTime startTime;

    @Schema(description = "结束时间", example = "12:00:00")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    /** 结束时间 */    private LocalTime endTime;

    @Schema(description = "状态", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "OFF", "LEAVE"})
    /** 状态 */    private String status;

    @Schema(description = "备注", example = "上午可接单")
    /** 备注 */    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public void setCompanionId(Long companionId) {
        this.companionId = companionId;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
