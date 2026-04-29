package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "陪玩师排班视图对象")
public class CompanionScheduleVO extends BaseVO {

    @Schema(description = "排班ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师姓名", example = "王小明")
    private String companionName;

    @Schema(description = "陪玩师昵称", example = "小明同学")
    private String companionNickname;

    @Schema(description = "排班日期", example = "2026-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @Schema(description = "时段", example = "MORNING", allowableValues = {"MORNING", "AFTERNOON", "EVENING", "FULL_DAY"})
    private String timeSlot;

    @Schema(description = "开始时间", example = "09:00:00")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "12:00:00")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Schema(description = "状态", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "OFF", "LEAVE"})
    private String status;

    @Schema(description = "备注", example = "上午可接单")
    private String remark;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
