package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 平台配置数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "平台配置数据传输对象")
public class PlatformConfigDTO {

    @Schema(description = "配置ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "平台配置项(JSON格式)", example = "{\"appId\":\"wx1234\",\"appSecret\":\"****\"}")
    private Map<String, Object> config;
}
