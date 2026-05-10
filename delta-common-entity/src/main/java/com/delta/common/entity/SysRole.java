package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色实体
 * <p>
 * 对应数据库表 sys_role，定义可自定义的系统角色。
 * 区别于原有的硬编码字段 role（SYS_ADMIN/CS_LEADER/CS_STAFF），
 * 本表支持管理员自行创建和管理任意角色。
 * </p>
 * <p>
 * 权限控制优先级：系统内置角色 > 自定义角色。当用户同时拥有内置角色和自定义角色时，
 * 取权限并集（最大权限原则）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Table(name = "sys_role", indexes = {
        @Index(name = "idx_sr_code", columnList = "role_code"),
        @Index(name = "idx_sr_status", columnList = "status")
})
public class SysRole extends BaseEntity {

    /** 角色编码（唯一标识，如CS_MANAGER、FINANCE、OPERATOR） */
    @TableField("role_code")
    private String roleCode;

    /** 角色名称（展示用，如客服主管、财务专员、运营经理） */
    @TableField("role_name")
    private String roleName;

    /** 角色描述 */
    @TableField("description")
    private String description;

    /** 是否为系统内置角色（1-是，0-否，内置角色不可删除） */
    @TableField("is_system")
    private Integer isSystem;

    /** 状态：1-启用，0-禁用 */
    @TableField("status")
    private Integer status;

    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;
}
