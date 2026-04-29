package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关键词触发实体
 * <p>
 * 对应数据库表 keywords，定义AI客服的关键词匹配规则，
 * 当客户消息命中关键词时触发对应的自动回复或转人工流程。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("keywords")
@Table(name = "keywords", indexes = {
        @Index(name = "idx_keywords_enabled", columnList = "enabled")
})
public class Keyword extends BaseEntity {

    /** 关键词内容 */
    private String keyword;

    /** 优先级，数值越大优先级越高 */
    private Integer priority;

    /** 是否启用 */
    private Boolean enabled;
}
