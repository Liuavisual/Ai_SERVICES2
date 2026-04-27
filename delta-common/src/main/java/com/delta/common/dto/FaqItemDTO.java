package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FaqItemDTO {

    private Long id;

    @NotBlank(message = "问题分类不能为空")
    private String category;

    @NotBlank(message = "问题内容不能为空")
    private String question;

    @NotBlank(message = "答案内容不能为空")
    private String answer;

    private Integer sortOrder;

    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
