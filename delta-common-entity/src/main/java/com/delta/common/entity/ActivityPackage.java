package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动套餐实体
 * <p>
 * 对应数据库表 activity_package，存储促销活动套餐信息，
 * 包括套餐标题、类型、价格、活动时间、条款等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_package")
@Table(name = "activity_package", indexes = {
        @Index(name = "idx_activity_package_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_activity_package_enabled", columnList = "enabled")
})
public class ActivityPackage extends BaseEntity {

    /** 俱乐部配置ID */
    private Long clubConfigId;

    /** 游戏配置ID */
    private Long gameConfigId;

    /** 套餐标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 活动类型 */
    private String activityType;

    /** 包含的服务项目ID列表 */
    private String serviceItemIds;

    /** 套餐价格 */
    private BigDecimal packagePrice;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 活动开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 活动结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 横幅图片URL */
    private String bannerUrl;

    /** 条款说明 */
    private String terms;

    /** 排序序号 */
    private Integer sortOrder;

    /** 是否启用 */
    private Integer enabled;
}
