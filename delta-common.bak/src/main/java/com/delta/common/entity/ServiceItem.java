package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 服务项目实体类
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
public class ServiceItem extends BaseEntity {

    /** 俱乐部配置ID */
    private Long clubConfigId;

    /** 游戏配置ID */
    private Long gameConfigId;

    /** 项目名称 */
    @TableField("service_name")
    private String serviceName;

    /** 项目编码 */
    @TableField("service_code")
    private String serviceCode;

    /** 描述 */
    @TableField("service_desc")
    private String serviceDesc;

    /** 图标 */
    @TableField("service_icon")
    private String serviceIcon;

    /** 分类 */
    private String category;

    /** 基础价格 */
    private BigDecimal basePrice;

    /** 价格单位 */
    private String priceUnit;

    /** 最短时长 */
    private BigDecimal minDuration;

    /** 保障说明 */
    private String guaranteeText;

    /** 退款政策 */
    private String refundPolicy;

    /** 是否启用 */
    private Integer enabled;

    /** 排序序号 */
    private Integer sortOrder;
}
