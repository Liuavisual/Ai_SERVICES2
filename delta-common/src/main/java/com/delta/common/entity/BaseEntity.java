package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 提供所有实体的公共字段：主键ID、创建时间、更新时间。
 * 使用MyBatis-Plus的自动填充功能维护时间戳字段。</p>
 *
 * @author delta
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间，插入时自动填充 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
