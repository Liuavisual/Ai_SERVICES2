package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "陪玩师培训视图对象")
public class CompanionTrainingVO extends BaseVO {

    @Schema(description = "培训记录ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "陪玩师ID")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师昵称")
    private String companionNickname;

    @Schema(description = "培训课程名称")
    private String courseName;

    @Schema(description = "培训类型")
    private String courseType;

    @Schema(description = "培训内容")
    private String courseContent;

    @Schema(description = "培训状态")
    private String trainingStatus;

    @Schema(description = "开始学习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @Schema(description = "完成学习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    @Schema(description = "考核得分")
    private Integer examScore;

    @Schema(description = "培训备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
