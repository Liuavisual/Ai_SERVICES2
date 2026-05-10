package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户生命周期预警规则实体
 * <p>
 * 对应数据库表 customer_warning_rule，定义客户生命周期自动预警规则。
 * 当客户满足规则条件时，系统自动触发预警通知和挽回策略（P1-2改进）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("customer_warning_rule")
public class CustomerWarningRule {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 规则名称 */
    @TableField("rule_name")
    private String ruleName;

    /** 监控阶段：AT_RISK / CHURNED */
    @TableField("monitor_stage")
    private String monitorStage;

    /** 触发条件：NO_ACTIVITY_DAYS-连续N天无活跃, NEGATIVE_FEEDBACK-收到负面评价, LOW_SPEND-消费下降 */
    @TableField("trigger_condition")
    private String triggerCondition;

    /** 条件阈值 */
    @TableField("threshold_value")
    private Integer thresholdValue;

    /** 处理动作：NOTIFY_CS-通知客服, SEND_COUPON-发送优惠券, MARK_VIP-标记VIP关怀 */
    @TableField("action_type")
    private String actionType;

    /** 动作参数（JSON格式，如优惠券ID等） */
    @TableField("action_params")
    private String actionParams;

    /** 是否启用 */
    @TableField("enabled")
    private Integer enabled;

    /** 优先级 */
    @TableField("priority")
    private Integer priority;
}
