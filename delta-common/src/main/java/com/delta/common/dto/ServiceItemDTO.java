package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "服务项目数据传输对象")
public class ServiceItemDTO {

    @Schema(description = "服务项目ID", example = "1")
    private Long id;

    @Schema(description = "俱乐部ID", example = "1001")
    @NotNull(message = "俱乐部ID不能为空")
    private Long clubConfigId;

    @Schema(description = "游戏配置ID", example = "2001")
    private Long gameConfigId;

    @Schema(description = "项目名称", example = "王者荣耀陪玩")
    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @Schema(description = "项目编码", example = "WZRY_PLAY")
    @NotBlank(message = "项目编码不能为空")
    private String itemCode;

    @Schema(description = "分类", example = "陪玩", allowableValues = {"陪玩", "语音", "视频", "代练"})
    @NotBlank(message = "分类不能为空")
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
}
