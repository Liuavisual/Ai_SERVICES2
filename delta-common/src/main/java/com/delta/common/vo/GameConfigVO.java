package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "游戏配置视图对象")
public class GameConfigVO extends BaseVO {

    @Schema(description = "游戏配置ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @JsonIgnore
    private Long clubConfigId;

    @Schema(description = "游戏名称", example = "王者荣耀")
    private String gameName;

    @Schema(description = "游戏编码", example = "WZRY")
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

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
