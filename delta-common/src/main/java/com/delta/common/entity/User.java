package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户/终端用户实体
 * <p>
 * 对应数据库表 users，存储来自各平台（微信、测试等）的客户信息，
 * 包括平台标识、平台用户ID、昵称、AI开关和分配的客服ID。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
@Table(name = "users", indexes = {
        @Index(name = "idx_users_platform", columnList = "platform"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
})
public class User extends BaseEntity {

    /** 来源平台：wechat、test等 */
    private String platform;

    /** 平台侧用户ID，如微信的openid */
    private String platformUserId;

    /** 客户昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 是否启用AI自动回复：true-AI回复，false-仅人工 */
    private Boolean aiEnabled;

    /** 分配的专属客服ID（sys_user表），null表示未分配 */
    private Long assignedCsUserId;
}
