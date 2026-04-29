package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体
 * <p>
 * 对应数据库表 sys_user，存储系统后台用户（客服、管理员等）信息，
 * 包括用户名、角色、状态等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Table(name = "sys_user", indexes = {
        @Index(name = "idx_sys_user_role", columnList = "role"),
        @Index(name = "idx_sys_user_status", columnList = "status"),
        @Index(name = "idx_sys_user_role_status", columnList = "role,status")
})
public class SysUser extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色 */
    private String role;

    /** 状态 */
    private String status;

    /** 创建人ID */
    private Long createdBy;
}
