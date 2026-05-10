package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 营收日报实体
 * <p>
 * 对应数据库表 revenue_daily_report，按日期和游戏类型统计俱乐部营收数据。
 * 支持营收趋势分析、同比环比增长、分游戏收入占比。
 * 源自报告对"数据分析/商业智能"的要求。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("revenue_daily_report")
@Table(name = "revenue_daily_report", indexes = {
        @Index(name = "idx_rdr_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_rdr_report_date", columnList = "report_date"),
        @Index(name = "idx_rdr_game_type", columnList = "game_type")
})
public class RevenueDailyReport extends BaseEntity {

    /** 俱乐部配置ID */
    @TableField("club_config_id")
    private Long clubConfigId;

    /** 报表日期 */
    @TableField("report_date")
    private LocalDate reportDate;

    /** 游戏类型（null=全游戏合计） */
    @TableField("game_type")
    private String gameType;

    /** 订单总数 */
    @TableField("total_orders")
    private Integer totalOrders;

    /** 已完成订单数 */
    @TableField("completed_orders")
    private Integer completedOrders;

    /** 退款订单数 */
    @TableField("refund_orders")
    private Integer refundOrders;

    /** 订单总收入 */
    @TableField("total_revenue")
    private BigDecimal totalRevenue;

    /** 平台分成收入 */
    @TableField("platform_income")
    private BigDecimal platformIncome;

    /** AI会话总数 */
    @TableField("ai_conversations")
    private Integer aiConversations;

    /** AI处理率(%) */
    @TableField("ai_handle_rate")
    private BigDecimal aiHandleRate;

    /** 客户满意度均分 */
    @TableField("avg_satisfaction")
    private BigDecimal avgSatisfaction;

    /** 新客户数 */
    @TableField("new_customers")
    private Integer newCustomers;

    /** 老客户复购数 */
    @TableField("repeat_customers")
    private Integer repeatCustomers;

    /** 活跃陪玩师数 */
    @TableField("active_companions")
    private Integer activeCompanions;
}
