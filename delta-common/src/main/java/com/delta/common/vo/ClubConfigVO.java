package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClubConfigVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String clubName;
    private String clubLogo;
    private String mainGames;
    private String serviceSlogan;
    private String welcomeMessage;
    private String contactInfo;
    private String clubFeatures;
    private List<ClubLevelPriceVO> levelPrices;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Deprecated
    private BigDecimal priceLevelTwo;
    @Deprecated
    private BigDecimal priceLevelOne;
    @Deprecated
    private BigDecimal priceTop;
    @Deprecated
    private BigDecimal priceStar;
}
