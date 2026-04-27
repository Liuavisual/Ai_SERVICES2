package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 平台配置实体
 * <p>
 * 对应数据库表 platform_configs，存储各接入平台（如微信公众号）的集成配置，
 * 包括平台标识、启用状态和JSON格式的平台特定配置参数。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "platform_configs", autoResultMap = true)
public class PlatformConfig extends BaseEntity {

    /** 平台标识，如 wechat */
    private String platform;

    /** 是否启用该平台接入 */
    private Boolean enabled;

    /** 平台特定配置，JSON格式存储，如微信的appId、appSecret、token等 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
}
