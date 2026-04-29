package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companions")
public class Companion extends BaseEntity {

    private String realName;

    private String nickname;

    private String phone;

    private String wechat;

    private Long levelId;

    private String avatar;

    private String gameType;

    private String description;

    private BigDecimal price;

    private Integer enabled;

    private String serviceTags;

    private String supportedGames;

    private BigDecimal kdRatio;

    private String rankLevel;

    private String voiceSampleUrl;

    private BigDecimal ratingAvg;

    private Integer orderCount;
}
