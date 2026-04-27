package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameConfigDTO {

    private Long id;

    @NotNull(message = "俱乐部ID不能为空")
    private Long clubConfigId;

    @NotBlank(message = "游戏名称不能为空")
    private String gameName;

    @NotBlank(message = "游戏编码不能为空")
    private String gameCode;

    private String gameType;

    private Integer enabled;

    private Integer sortOrder;

    private String iconUrl;

    private String description;

    private String customSettings;
}
