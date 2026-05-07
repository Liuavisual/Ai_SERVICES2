package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 角色-权限关联实体
 * <p>
 * 对应数据库表 sys_role_permission，定义角色与权限的多对多关联关系。
 * 通过 (role_id, perm_id) 唯一约束保证关联不重复。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("sys_role_permission")
@Table(name = "sys_role_permission", indexes = {
        @Index(name = "idx_srp_role_id", columnList = "role_id"),
        @Index(name = "idx_srp_perm_id", columnList = "perm_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "perm_id"})
})
public class SysRolePermission {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 角色ID */
    @TableField("role_id")
    private Long roleId;

    /** 权限ID */
    @TableField("perm_id")
    private Long permId;
}
