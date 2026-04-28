package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanionVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String realName;
    private String nickname;
    private String phone;
    private String wechat;
    @ObfuscatedId
    private Long levelId;
    private String levelName;
    private BigDecimal levelBasePrice;
    private String avatar;
    private String gameType;
    private String description;
    private BigDecimal price;
    private BigDecimal displayPrice;
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
