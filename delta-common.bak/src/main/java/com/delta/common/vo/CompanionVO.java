package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "陪玩师视图对象")
public class CompanionVO extends BaseVO {

    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "真实姓名", example = "王小明")
    private String realName;

    @Schema(description = "昵称", example = "小明同学")
    private String nickname;

    @Schema(description = "手机号", example = "138****8000")
    private String phone;

    @Schema(description = "微信号", example = "wx_xiaoming")
    private String wechat;

    @Schema(description = "等级ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long levelId;

    @Schema(description = "等级名称", example = "金牌陪玩")
    private String levelName;

    @Schema(description = "等级基础价格", example = "88.00")
    private BigDecimal levelBasePrice;

    @Schema(description = "头像URL", example = "https://example.com/avatar/001.jpg")
    private String avatar;

    @Schema(description = "擅长游戏", example = "王者荣耀,和平精英")
    private String gameType;

    @Schema(description = "个人简介", example = "5年陪玩经验，擅长MOBA类游戏")
    private String description;

    @Schema(description = "价格(元/小时)", example = "88.00")
    private BigDecimal price;

    @Schema(description = "展示价格", example = "88.00")
    private BigDecimal displayPrice;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
