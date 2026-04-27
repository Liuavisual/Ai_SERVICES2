package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClubConfigVO {

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
