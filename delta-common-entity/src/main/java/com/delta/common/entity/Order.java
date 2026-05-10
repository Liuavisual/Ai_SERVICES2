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
 * 订单实体
 * <p>
 * 对应数据库表 orders，记录客户与陪玩师之间的服务订单，
 * 包括预约时间、实际时间、金额、支付状态等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_companion_id", columnList = "companion_id"),
        @Index(name = "idx_orders_order_status", columnList = "order_status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at"),
        @Index(name = "idx_orders_status_created", columnList = "order_status,created_at")
})
public class Order extends BaseEntity {

    /** 订单编号 */
    private String orderNo;

    /** 下单客户ID */
    private Long userId;

    /** 陪玩师ID */
    private Long companionId;

    /** 陪玩师名称 */
    private String companionName;

    /** 服务类型 */
    private String serviceType;

    /** 游戏类型 */
    private String gameType;

    /** 价格规则ID */
    private Long priceRuleId;

    /** 订单状态 */
    private String orderStatus;

    /** 预约开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStart;

    /** 预约结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEnd;

    /** 实际开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStart;

    /** 实际结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEnd;

    /** 服务时长（分钟） */
    private Integer durationMinutes;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal paidAmount;

    /** 支付状态 */
    private String paymentStatus;

    /** 支付方式 */
    private String paymentMethod;

    /** 第三方交易流水号 */
    private String transactionId;

    /** 支付完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /** 关联工单ID */
    private Long workOrderId;

    /** 备注 */
    private String remark;
}
