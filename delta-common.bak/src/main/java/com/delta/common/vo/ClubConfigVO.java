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
@Schema(description = "俱乐部配置视图对象")
public class ClubConfigVO extends BaseVO {

    @Schema(description = "俱乐部ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部名称", example = "星辰俱乐部")
    private String clubName;

    @Schema(description = "俱乐部Logo URL", example = "https://example.com/logo/star.png")
    private String clubLogo;

    @Schema(description = "主营游戏", example = "王者荣耀,和平精英,原神")
    private String mainGames;

    @Schema(description = "服务口号", example = "专业陪玩，快乐无限")
    private String serviceSlogan;

    @Schema(description = "欢迎语", example = "欢迎来到星辰俱乐部！")
    private String welcomeMessage;

    @Schema(description = "联系方式", example = "微信:star_club")
    private String contactInfo;

    @Schema(description = "俱乐部特色", example = "24小时在线,专业陪玩师团队")
    private String clubFeatures;

    @Schema(description = "等级价格列表")
    private List<ClubLevelPriceVO> levelPrices;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Deprecated
    @Schema(description = "二级价格(已废弃)", example = "68.00", deprecated = true)
    private BigDecimal priceLevelTwo;

    @Deprecated
    @Schema(description = "一级价格(已废弃)", example = "88.00", deprecated = true)
    private BigDecimal priceLevelOne;

    @Deprecated
    @Schema(description = "顶级价格(已废弃)", example = "128.00", deprecated = true)
    private BigDecimal priceTop;

    @Deprecated
    @Schema(description = "星级价格(已废弃)", example = "168.00", deprecated = true)
    private BigDecimal priceStar;
}
