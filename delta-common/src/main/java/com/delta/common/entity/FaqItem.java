package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FAQ知识库条目实体
 * <p>
 * 对应数据库表 faq_items，存储常见问题与答案，
 * 用于AI客服回复时的知识检索和自动问答。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("faq_items")
@Table(name = "faq_items", indexes = {
        @Index(name = "idx_faq_items_category", columnList = "category"),
        @Index(name = "idx_faq_items_enabled", columnList = "enabled")
})
public class FaqItem extends BaseEntity {

    /** 问题分类，如"服务流程"、"价格说明" */
    private String category;

    /** 问题内容 */
    private String question;

    /** 答案内容 */
    private String answer;

    /** 排序序号，数值越小越靠前 */
    private Integer sortOrder;

    /** 是否启用：1-启用，0-禁用 */
    private Integer enabled;
}
