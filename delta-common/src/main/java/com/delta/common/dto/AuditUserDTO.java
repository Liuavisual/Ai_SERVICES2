package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户审核数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "用户审核数据传输对象")
public class AuditUserDTO {

    @Schema(description = "待审核的用户ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "用户ID不能为空")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "审核结果状态", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"})
    @NotBlank(message = "审核状态不能为空")
    private String status;

    @Schema(description = "审核备注", example = "资料审核通过")
    private String remark;
}
