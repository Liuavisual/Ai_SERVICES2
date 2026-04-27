package com.delta.common.dto;

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
public class CompanionDTO {

    private Long id;

    @NotBlank(message = "真实姓名不能为空")
    /** 真实姓名 */    private String realName;

    @NotBlank(message = "昵称不能为空")
    /** 昵称 */    private String nickname;

    /** 手机号 */    private String phone;

    /** 微信号 */    private String wechat;

    /** 等级ID */    private Long levelId;

    /** 头像URL */    private String avatar;

    /** 擅长游戏 */    private String gameType;

    /** 个人简介 */    private String description;

    /** 价格(元/小时) */    private BigDecimal price;

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
