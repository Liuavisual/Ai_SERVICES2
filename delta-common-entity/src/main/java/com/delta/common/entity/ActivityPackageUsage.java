package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 活动套餐使用统计实体
 * <p>
 * 对应数据库表 activity_package_usage，统计每个活动套餐的领取和使用转化情况（P2改进）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("activity_package_usage")
@Table(name = "activity_package_usage", indexes = {
        @Index(name = "idx_apu_package_id", columnList = "package_id"),
        @Index(name = "idx_apu_user_id", columnList = "user_id")
})
public class ActivityPackageUsage {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 活动套餐ID */
    @TableField("package_id")
    private Long packageId;

    /** 领取用户ID */
    @TableField("user_id")
    private Long userId;

    /** 领取时间 */
    @TableField("claim_time")
    private java.time.LocalDateTime claimTime;

    /** 是否已使用(转化) */
    @TableField("is_converted")
    private Integer isConverted;

    /** 转化时间 */
    @TableField("converted_time")
    private java.time.LocalDateTime convertedTime;

    /** 关联订单ID */
    @TableField("order_id")
    private Long orderId;
}
