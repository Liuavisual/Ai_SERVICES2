package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "活动套餐视图对象")
public class ActivityPackageVO extends BaseVO {

    @Schema(description = "活动套餐ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "游戏配置ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long gameConfigId;

    @Schema(description = "游戏名称", example = "王者荣耀")
    private String gameName;

    @Schema(description = "活动标题", example = "春节特惠陪玩套餐")
    private String title;

    @Schema(description = "活动描述", example = "春节期间限时优惠，全场8折起")
    private String description;

    @Schema(description = "活动类型", example = "DISCOUNT", allowableValues = {"DISCOUNT", "GIFT", "BUNDLE", "LIMITED"})
    private String activityType;

    @Schema(description = "包含服务项目ID列表(逗号分隔)", example = "1,2,3")
    private String serviceItemIds;

    @Schema(description = "包含服务项目名称(逗号分隔)", example = "王者荣耀陪玩,和平精英陪玩")
    private String serviceItemNames;

    @Schema(description = "套餐价格", example = "168.00")
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

    @Schema(description = "活动状态", example = "ACTIVE", allowableValues = {"NOT_STARTED", "ACTIVE", "EXPIRED"})
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
