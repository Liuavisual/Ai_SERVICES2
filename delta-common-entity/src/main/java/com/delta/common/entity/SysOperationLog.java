package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作审计日志实体
 * <p>
 * 对应数据库表 sys_operation_log，记录管理员对权限和角色的所有操作。
 * 支持按操作人、模块、时间等维度查询和筛选。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("sys_operation_log")
@Table(name = "sys_operation_log", indexes = {
        @Index(name = "idx_sol_operator", columnList = "operator_id"),
        @Index(name = "idx_sol_module", columnList = "module"),
        @Index(name = "idx_sol_operate_time", columnList = "operate_time")
})
public class SysOperationLog {

    /** 主键 */
    @TableId
    @TableField("id")
    private Long id;

    /** 操作人ID */
    @TableField("operator_id")
    private Long operatorId;

    /** 操作人名称 */
    @TableField("operator_name")
    private String operatorName;

    /** 操作模块 */
    @TableField("module")
    private String module;

    /** 操作类型 */
    @TableField("action")
    private String action;

    /** 操作对象类型 */
    @TableField("target_type")
    private String targetType;

    /** 操作对象ID */
    @TableField("target_id")
    private Long targetId;

    /** 操作内容描述 */
    @TableField("content")
    private String content;

    /** 变更前数据(JSON) */
    @TableField("before_data")
    private String beforeData;

    /** 变更后数据(JSON) */
    @TableField("after_data")
    private String afterData;

    /** 操作IP */
    @TableField("ip_address")
    private String ipAddress;

    /** 浏览器UA */
    @TableField("user_agent")
    private String userAgent;

    /** 操作时间 */
    @TableField("operate_time")
    private LocalDateTime operateTime;

    /** 操作状态：1-成功，0-失败 */
    @TableField("status")
    private Integer status;

    /** 错误信息 */
    @TableField("error_msg")
    private String errorMsg;
}