package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_package")
public class ActivityPackage extends BaseEntity {

    private Long clubConfigId;
    private Long gameConfigId;
    private String title;
    private String description;
    private String activityType;
    private String serviceItemIds;
    private BigDecimal packagePrice;
    private BigDecimal originalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bannerUrl;
    private String terms;
    private Integer sortOrder;
    private Integer enabled;
}
