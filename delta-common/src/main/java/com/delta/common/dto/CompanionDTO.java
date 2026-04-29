package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 陪玩师数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "陪玩师数据传输对象")
public class CompanionDTO {

    @Schema(description = "陪玩师ID", example = "1")
    private Long id;

    @Schema(description = "真实姓名", example = "王小明")
    @NotBlank(message = "真实姓名不能为空")
    /** 真实姓名 */    private String realName;

    @Schema(description = "昵称", example = "小明同学")
    @NotBlank(message = "昵称不能为空")
    /** 昵称 */    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    /** 手机号 */    private String phone;

    @Schema(description = "微信号", example = "wx_xiaoming")
    /** 微信号 */    private String wechat;

    @Schema(description = "等级ID", example = "3")
    /** 等级ID */    private Long levelId;

    @Schema(description = "头像URL", example = "https://example.com/avatar/001.jpg")
    /** 头像URL */    private String avatar;

    @Schema(description = "擅长游戏", example = "王者荣耀,和平精英")
    /** 擅长游戏 */    private String gameType;

    @Schema(description = "个人简介", example = "5年陪玩经验，擅长MOBA类游戏")
    /** 个人简介 */    private String description;

    @Schema(description = "价格(元/小时)", example = "88.00")
    /** 价格(元/小时) */    private BigDecimal price;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
