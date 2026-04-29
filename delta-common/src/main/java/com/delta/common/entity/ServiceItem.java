package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 服务项目实体
 * <p>
 * 对应数据库表 service_item，存储可购买的服务项目信息，
 * 包括项目名称、编码、分类、价格、时长、保障条款等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
@Table(name = "service_item", indexes = {
        @Index(name = "idx_service_item_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_service_item_enabled", columnList = "enabled")
})
public class ServiceItem extends BaseEntity {

    /** 俱乐部配置ID */
    private Long clubConfigId;

    /** 游戏配置ID */
    private Long gameConfigId;

    /** 项目名称 */
    private String itemName;

    /** 项目编码 */
    private String itemCode;

    /** 分类 */
    private String category;

    /** 描述 */
    private String description;

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

    /** 排序序号 */
    private Integer sortOrder;

    /** 是否启用 */
    private Integer enabled;
}
