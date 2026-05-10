package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
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
 * @author 刘建国
 */
@Data
@Schema(description = "陪玩师排班数据传输对象")
public class CompanionScheduleDTO {

    @Schema(description = "排班ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "陪玩师ID不能为空")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "排班日期", example = "2026-01-15")
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @Schema(description = "时段", example = "MORNING", allowableValues = {"MORNING", "AFTERNOON", "EVENING", "FULL_DAY"})
    @NotBlank(message = "时间段不能为空")
    private String timeSlot;

    @Schema(description = "开始时间", example = "09:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "12:00:00")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Schema(description = "状态", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "OFF", "LEAVE"})
    private String status;

    @Schema(description = "备注", example = "上午可接单")
    private String remark;
}
