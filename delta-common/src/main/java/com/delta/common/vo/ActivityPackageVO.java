package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityPackageVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @JsonIgnore
    private Long clubConfigId;
    @ObfuscatedId
    private Long gameConfigId;
    private String gameName;
    private String title;
    private String description;
    private String activityType;
    private String serviceItemIds;
    private String serviceItemNames;
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
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
