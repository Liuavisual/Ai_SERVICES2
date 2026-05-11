package com.delta.common.vo;

import lombok.Data;
import java.util.List;

/**
 * 系统角色视图对象
 *
 * @author 刘建国
 */
@Data
public class SysRoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer isSystem;
    private Integer status;
    private Integer sortOrder;
    private List<SysPermissionVO> permissions;
    /** 权限数量（非持久化字段，仅前端展示用） */
    private Integer permissionCount;
}
