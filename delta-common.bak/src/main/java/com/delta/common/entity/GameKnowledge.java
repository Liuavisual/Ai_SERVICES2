package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_knowledge")
public class GameKnowledge extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long gameId;

    private String category;

    private String title;

    private String content;

    private String source;

    private String tags;

    private String keywords;

    private String reliability;

    private String version;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    private Integer viewCount;

    private Integer helpfulCount;

    private Integer enabled;
}
