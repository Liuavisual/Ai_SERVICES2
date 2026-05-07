package com.delta.common.vo;

import lombok.Data;

/**
 * 系统权限视图对象
 *
 * @author 刘建国
 */
@Data
public class SysPermissionVO {
    private Long id;
    private String permCode;
    private String permName;
    private String permGroup;
    private String actionType;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
