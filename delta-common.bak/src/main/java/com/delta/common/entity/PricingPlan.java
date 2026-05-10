package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 定价方案实体
 * <p>
 * 对应数据库表 pricing_plan，定义系统的三层订阅定价：
 * BASIC(基础版99元/月)、PRO(专业版399元/月)、ENTERPRISE(企业版3000元+/月)。
 * 源自《2026年5月游戏陪玩行业客服系统市场调研报告》第五节变现模式。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pricing_plan")
@Table(name = "pricing_plan", indexes = {
        @Index(name = "idx_pp_plan_code", columnList = "plan_code"),
        @Index(name = "idx_pp_status", columnList = "status")
})
public class PricingPlan extends BaseEntity {

    /** 方案编码：BASIC / PRO / ENTERPRISE */
    @TableField("plan_code")
    private String planCode;

    /** 方案名称：基础版 / 专业版 / 企业版 */
    @TableField("plan_name")
    private String planName;

    /** 月费价格 */
    @TableField("monthly_price")
    private BigDecimal monthlyPrice;

    /** 年费价格（购买年费享折扣） */
    @TableField("yearly_price")
    private BigDecimal yearlyPrice;

    /** 按次付费单价（元/次AI对话） */
    @TableField("per_call_price")
    private BigDecimal perCallPrice;

    /** 日会员价格 */
    @TableField("daily_price")
    private BigDecimal dailyPrice;

    /** 陪玩师数量上限 */
    @TableField("max_companions")
    private Integer maxCompanions;

    /** 月消息量上限（0=无限制） */
    @TableField("max_monthly_messages")
    private Integer maxMonthlyMessages;

    /** AI人格模板数量上限 */
    @TableField("max_personality_templates")
    private Integer maxPersonalityTemplates;

    /** 情绪智能等级：BASIC / ADVANCED / PREMIUM */
    @TableField("emotion_intelligence_level")
    private String emotionIntelligenceLevel;

    /** 是否包含智能派单 */
    @TableField("include_smart_dispatch")
    private Boolean includeSmartDispatch;

    /** 是否包含全流程质检 */
    @TableField("include_full_quality_check")
    private Boolean includeFullQualityCheck;

    /** 是否包含数据分析 */
    @TableField("include_analytics")
    private Boolean includeAnalytics;

    /** 是否支持自定义品牌 */
    @TableField("include_brand_custom")
    private Boolean includeBrandCustom;

    /** 是否支持API接入 */
    @TableField("include_api_access")
    private Boolean includeApiAccess;

    /** 功能描述（Markdown格式） */
    @TableField("features")
    private String features;

    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：1-启用，0-禁用 */
    @TableField("status")
    private Integer status;
}
