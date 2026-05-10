package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 系统权限实体
 * <p>
 * 对应数据库表 sys_permission，定义系统中的所有可授权操作。
 * 权限编码采用 resource:action 格式，如 customer:view、companion:edit。
 * </p>
 * <p>
 * 权限分组：
 * <ul>
 *   <li>dashboard - 工作台相关</li>
 *   <li>customer - 客户管理相关</li>
 *   <li>companion - 陪玩管理相关</li>
 *   <li>order - 订单/工单相关</li>
 *   <li>service - 服务项目/追踪相关</li>
 *   <li>message - 消息处理相关</li>
 *   <li>config - 系统配置相关</li>
 *   <li>system - 系统管理相关</li>
 *   <li>tool - 开发工具相关</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("sys_permission")
@Table(name = "sys_permission", indexes = {
        @Index(name = "idx_sp_code", columnList = "perm_code"),
        @Index(name = "idx_sp_group", columnList = "perm_group")
})
public class SysPermission {

    /** 主键 */
    @TableId
    @TableField("id")
    private Long id;

    /** 权限编码（唯一，如 customer:view、companion:edit、system:admin） */
    @TableField("perm_code")
    private String permCode;

    /** 权限名称（展示用，如查看客户、编辑陪玩师） */
    @TableField("perm_name")
    private String permName;

    /** 权限分组（对应功能模块，如 customer、companion、config） */
    @TableField("perm_group")
    private String permGroup;

    /** 操作类型：view-查看, create-新增, edit-编辑, delete-删除, export-导出, manage-管理 */
    @TableField("action_type")
    private String actionType;

    /** 权限描述 */
    @TableField("description")
    private String description;

    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：1-启用，0-禁用 */
    @TableField("status")
    private Integer status;
}
