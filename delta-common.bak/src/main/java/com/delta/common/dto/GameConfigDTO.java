package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 游戏配置数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "游戏配置数据传输对象")
public class GameConfigDTO {

    @Schema(description = "游戏配置ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "俱乐部ID不能为空")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "游戏名称", example = "王者荣耀")
    @NotBlank(message = "游戏名称不能为空")
    private String gameName;

    @Schema(description = "游戏编码", example = "WZRY")
    @NotBlank(message = "游戏编码不能为空")
    private String gameCode;

    @Schema(description = "游戏类型", example = "MOBA", allowableValues = {"MOBA", "FPS", "RPG", "卡牌", "休闲"})
    private String gameType;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "图标URL", example = "https://example.com/icon/wzry.png")
    private String iconUrl;

    @Schema(description = "游戏描述", example = "5V5团队竞技手游")
    private String description;

    @Schema(description = "自定义设置(JSON格式)", example = "{\"maxPlayers\":5}")
    private String customSettings;
}
