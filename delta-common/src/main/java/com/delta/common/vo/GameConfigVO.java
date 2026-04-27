package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameConfigVO {

    private Long id;
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
