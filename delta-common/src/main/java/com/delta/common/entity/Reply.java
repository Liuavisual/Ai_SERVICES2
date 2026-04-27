package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 自动回复规则实体
 * <p>
 * 对应数据库表 replies，定义自动回复的触发规则和回复内容，
 * 支持按关键词（KEYWORD）和欢迎消息（WELCOME）两种触发类型。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("replies")
public class Reply extends BaseEntity {

    /** 触发类型：KEYWORD-关键词触发，WELCOME-新用户关注触发 */
    private String triggerType;

    /** 触发键，关键词触发时为具体关键词，欢迎触发时为"welcome" */
    private String triggerKey;

    /** 回复内容 */
    @NonNull
    private String content;

    /** 是否启用 */
    private Boolean enabled;
}
