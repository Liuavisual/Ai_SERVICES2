package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 陪玩师数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "陪玩师数据传输对象")
public class CompanionDTO {

    @Schema(description = "陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "真实姓名", example = "王小明")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "昵称", example = "小明同学")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "微信号", example = "wx_xiaoming")
    private String wechat;

    @Schema(description = "等级ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long levelId;

    @Schema(description = "头像URL", example = "https://example.com/avatar/001.jpg")
    private String avatar;

    @Schema(description = "擅长游戏", example = "王者荣耀,和平精英")
    private String gameType;

    @Schema(description = "个人简介", example = "5年陪玩经验，擅长MOBA类游戏")
    private String description;

    @Schema(description = "价格(元/小时)", example = "88.00")
    private BigDecimal price;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
