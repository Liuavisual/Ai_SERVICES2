package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_config")
public class GameConfig extends BaseEntity {

    private Long clubConfigId;
    private String gameName;
    private String gameCode;
    private String gameType;
    private Integer enabled;
    private Integer sortOrder;
    private String iconUrl;
    private String description;
    private String customSettings;
}
