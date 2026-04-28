package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameConfigVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @JsonIgnore
    private Long clubConfigId;
    private String gameName;
    private String gameCode;
    private String gameType;
    private Integer enabled;
    private Integer sortOrder;
    private String iconUrl;
    private String description;
    private String customSettings;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
