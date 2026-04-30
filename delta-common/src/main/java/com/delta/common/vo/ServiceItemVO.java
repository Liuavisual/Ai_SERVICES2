package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "服务项目视图对象")
public class ServiceItemVO extends BaseVO {

    @Schema(description = "服务项目ID", example = "d_xxxxx")
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

    @Schema(description = "项目名称", example = "王者荣耀陪玩")
    private String itemName;

    @Schema(description = "项目编码", example = "WZRY_PLAY")
    private String itemCode;

    @Schema(description = "分类", example = "陪玩", allowableValues = {"陪玩", "语音", "视频", "代练"})
    private String category;

    @Schema(description = "项目描述", example = "王者荣耀专业陪玩服务")
    private String description;

    @Schema(description = "基础价格", example = "88.00")
    private BigDecimal basePrice;

    @Schema(description = "价格单位", example = "元/小时", allowableValues = {"元/小时", "元/次", "元/局"})
    private String priceUnit;

    @Schema(description = "最小时长", example = "1.0")
    private BigDecimal minDuration;

    @Schema(description = "保障说明", example = "不满意全额退款")
    private String guaranteeText;

    @Schema(description = "退款政策", example = "服务开始前可全额退款")
    private String refundPolicy;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "价格规则列表")
    private List<ServicePriceRuleVO> priceRules;
}
