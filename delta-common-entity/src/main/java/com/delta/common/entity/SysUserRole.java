package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 用户-角色关联实体
 * <p>
 * 对应数据库表 sys_user_role，定义用户与自定义角色的多对多关联关系。
 * 注意：系统内置角色（SYS_ADMIN/CS_LEADER/CS_STAFF）仍通过 SysUser.role 字段管理，
 * 本表仅用于自定义角色的分配。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("sys_user_role")
@Table(name = "sys_user_role", indexes = {
        @Index(name = "idx_sur_user_id", columnList = "user_id"),
        @Index(name = "idx_sur_role_id", columnList = "role_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
public class SysUserRole {

    /** 主键 */
    @TableId
    @TableField("id")
    private Long id;

    /** 系统用户ID */
    @TableField("user_id")
    private Long userId;

    /** 角色ID */
    @TableField("role_id")
    private Long roleId;
}
