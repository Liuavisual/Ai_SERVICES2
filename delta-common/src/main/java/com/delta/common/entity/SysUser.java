package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体（管理员/客服人员）
 * <p>
 * 对应数据库表 sys_user，存储后台管理系统的用户账号信息，
 * 角色包括 SYS_ADMIN（系统管理员）、CS_LEADER（客服负责人）、CS_STAFF（客服人员）。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录用户名 */
    private String username;

    /** 登录密码（BCrypt加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色：SYS_ADMIN-系统管理员，CS_LEADER-客服负责人，CS_STAFF-客服人员 */
    private String role;

    /** 状态：ACTIVE-启用，INACTIVE-禁用 */
    private String status;

    /** 创建该账号的管理员ID */
    private Long createdBy;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
