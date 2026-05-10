package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "FAQ项视图对象")
public class FaqItemVO extends BaseVO {

    @Schema(description = "FAQ项ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "问题分类", example = "服务相关")
    private String category;

    @Schema(description = "问题内容", example = "如何预约陪玩师？")
    private String question;

    @Schema(description = "答案内容", example = "您可以在首页选择心仪的陪玩师，点击预约按钮即可。")
    private String answer;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
