package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 服务质量检测记录实体
 * <p>
 * 对应数据库表 quality_check_record，记录AI全流程质检结果。
 * 支持实时违规内容识别、服务态度评分、客诉自动取证。
 * 源自报告第四节合规管控刚需分析。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quality_check_record")
@Table(name = "quality_check_record", indexes = {
        @Index(name = "idx_qcr_order_id", columnList = "order_id"),
        @Index(name = "idx_qcr_companion_id", columnList = "companion_id"),
        @Index(name = "idx_qcr_check_time", columnList = "check_time"),
        @Index(name = "idx_qcr_risk_level", columnList = "risk_level")
})
public class QualityCheckRecord extends BaseEntity {

    /** 关联订单ID */
    @TableField("order_id")
    private Long orderId;

    /** 关联陪玩师ID */
    @TableField("companion_id")
    private Long companionId;

    /** 关联客户ID */
    @TableField("user_id")
    private Long userId;

    /** 检测时间 */
    @TableField("check_time")
    private LocalDateTime checkTime;

    /** 检测类型：SERVICE-服务质量, CONTENT-内容合规, ATTITUDE-服务态度, SPEED-响应速度 */
    @TableField("check_type")
    private String checkType;

    /** 风险等级：SAFE-安全, LOW-低风险, MEDIUM-中风险, HIGH-高风险, CRITICAL-严重违规 */
    @TableField("risk_level")
    private String riskLevel;

    /** 检测得分(1-100) */
    @TableField("score")
    private Integer score;

    /** 违规类型：SEXUAL-涉黄, GAMBLING-涉赌, CHEAT-外挂, ABUSE-辱骂, REPLACE-代打, OTHER-其他 */
    @TableField("violation_type")
    private String violationType;

    /** 违规内容摘要 */
    @TableField("violation_summary")
    private String violationSummary;

    /** 证据截图/录音URL */
    @TableField("evidence_url")
    private String evidenceUrl;

    /** 处理建议 */
    @TableField("action_suggestion")
    private String actionSuggestion;

    /** 处理状态：PENDING-待处理, REVIEWED-已审核, RESOLVED-已处理, IGNORED-已忽略 */
    @TableField("handle_status")
    private String handleStatus;

    /** 处理人ID */
    @TableField("handler_id")
    private Long handlerId;

    /** 处理备注 */
    @TableField("handle_remark")
    private String handleRemark;
}
