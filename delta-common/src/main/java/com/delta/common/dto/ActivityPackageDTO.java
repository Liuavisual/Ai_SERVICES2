package com.delta.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityPackageDTO {

    private Long id;

    @NotNull(message = "俱乐部ID不能为空")
    private Long clubConfigId;

    private Long gameConfigId;

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "活动类型不能为空")
    private String activityType;

    private String serviceItemIds;

    @NotNull(message = "套餐价格不能为空")
    private BigDecimal packagePrice;

    private BigDecimal originalPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String bannerUrl;

    private String terms;

    private Integer sortOrder;

    private Integer enabled;
}
