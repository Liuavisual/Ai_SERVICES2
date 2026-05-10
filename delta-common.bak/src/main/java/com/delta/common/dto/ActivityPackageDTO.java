package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动套餐数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "活动套餐数据传输对象")
public class ActivityPackageDTO {

    @Schema(description = "活动套餐ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "俱乐部ID不能为空")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "游戏配置ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long gameConfigId;

    @Schema(description = "活动标题", example = "春节特惠陪玩套餐")
    @NotBlank(message = "活动标题不能为空")
    private String title;

    @Schema(description = "活动描述", example = "春节期间限时优惠，全场8折起")
    private String description;

    @Schema(description = "活动类型", example = "DISCOUNT", allowableValues = {"DISCOUNT", "GIFT", "BUNDLE", "LIMITED"})
    @NotBlank(message = "活动类型不能为空")
    private String activityType;

    @Schema(description = "包含服务项目ID列表(逗号分隔)", example = "1,2,3")
    private String serviceItemIds;

    @Schema(description = "套餐价格", example = "168.00")
    @NotNull(message = "套餐价格不能为空")
    private BigDecimal packagePrice;

    @Schema(description = "原价", example = "256.00")
    private BigDecimal originalPrice;

    @Schema(description = "活动开始时间", example = "2026-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", example = "2026-01-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "横幅图片URL", example = "https://example.com/banner/spring.jpg")
    private String bannerUrl;

    @Schema(description = "条款说明", example = "每个用户限购一次，不可与其他优惠叠加使用")
    private String terms;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;
}
