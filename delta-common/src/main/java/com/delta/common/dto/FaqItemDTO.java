package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "FAQ项数据传输对象")
public class FaqItemDTO {

    @Schema(description = "FAQ项ID", example = "1")
    private Long id;

    @Schema(description = "问题分类", example = "服务相关")
    @NotBlank(message = "问题分类不能为空")
    private String category;

    @Schema(description = "问题内容", example = "如何预约陪玩师？")
    @NotBlank(message = "问题内容不能为空")
    private String question;

    @Schema(description = "答案内容", example = "您可以在首页选择心仪的陪玩师，点击预约按钮即可。")
    @NotBlank(message = "答案内容不能为空")
    private String answer;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
