package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("messages")
public class Message extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long sessionId;

    private Long userId;

    private String direction;

    @TableField(value = "content", jdbcType = JdbcType.LONGVARCHAR)
    private String content;

    private String contentType;

    @TableField("is_ai")
    private Boolean ai;

    private String aiModel;

    private Integer aiTokenCount;

    private Integer aiResponseTimeMs;

    @TableField("keyword_triggered")
    private Boolean keywordTriggered;

    private String triggeredKeyword;

    private Long csUserId;

    private String emotionTag;

    private String intentTag;

    private String readStatus;
}
