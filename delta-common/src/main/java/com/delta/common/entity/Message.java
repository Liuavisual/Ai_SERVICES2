package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息实体
 * <p>
 * 对应数据库表 messages，记录所有客户与AI/人工客服之间的对话消息，
 * 包括消息方向（IN/OUT）、内容、是否AI回复、是否触发关键词等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("messages")
public class Message extends BaseEntity {

    /** 关联的客户ID（users表） */
    private Long userId;

    /** 消息方向：IN-客户发送，OUT-系统/AI/客服回复 */
    private String direction;

    /** 消息内容 */
    private String content;

    /** 是否AI自动回复 */
    @TableField("is_ai")
    private Boolean ai;

    /** 是否由关键词触发 */
    private Boolean keywordTriggered;
}
