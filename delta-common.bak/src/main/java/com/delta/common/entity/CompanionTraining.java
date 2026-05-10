package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 陪玩师培训记录实体
 * <p>
 * 对应数据库表 companion_training，记录陪玩师的标准化培训学习进度。
 * 培训内容基于AI沉淀的服务数据，帮助陪玩师提升服务水平。
 * 源自报告第五节增值服务变现中的"陪玩师培训增值服务"。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companion_training")
@Table(name = "companion_training", indexes = {
        @Index(name = "idx_ct_companion_id", columnList = "companion_id"),
        @Index(name = "idx_ct_status", columnList = "training_status")
})
public class CompanionTraining extends BaseEntity {

    /** 陪玩师ID */
    @TableField("companion_id")
    private Long companionId;

    /** 培训课程名称 */
    @TableField("course_name")
    private String courseName;

    /** 培训类型：SERVICE_STANDARD-服务规范, SCRIPT_TEMPLATE-话术模板, COMPLIANCE-合规培训, GAME_SKILL-游戏技能 */
    @TableField("course_type")
    private String courseType;

    /** 培训内容（文本/Markdown） */
    @TableField("course_content")
    private String courseContent;

    /** 培训状态：NOT_STARTED-未开始, IN_PROGRESS-进行中, COMPLETED-已完成 */
    @TableField("training_status")
    private String trainingStatus;

    /** 开始学习时间 */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /** 完成学习时间 */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /** 考核得分(0-100) */
    @TableField("exam_score")
    private Integer examScore;

    /** 培训备注 */
    @TableField("remark")
    private String remark;
}
