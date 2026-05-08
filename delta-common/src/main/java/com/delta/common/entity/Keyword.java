package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关键词触发实体
 * <p>
 * 对应数据库表 keywords，定义AI客服的关键词匹配规则，
 * 当客户消息命中关键词时触发对应的自动回复或转人工流程。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("keywords")
public class Keyword extends BaseEntity {

    /** 关键词内容 */
    @TableField("keyword")
    private String keyword;

    /** 关键词分类：TRANSFER-转人工/COMPLAINT-投诉/ORDER-下单/EMERGENCY-紧急 */
    @TableField("category")
    private String category;

    /** 匹配方式：EXACT-精确/FUZZY-模糊/REGEX-正则 */
    @TableField("match_type")
    private String matchType;

    /** 触发动作：REPLY-自动回复/TRANSFER-转人工/TAG-标记/ESCALATE-升级 */
    @TableField("action_type")
    private String actionType;

    /** 关联的自动回复ID */
    @TableField("reply_id")
    private Long replyId;

    /** 优先级，数值越大优先级越高 */
    @TableField("priority")
    private Integer priority;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 备注说明 */
    @TableField("remark")
    private String remark;
}
