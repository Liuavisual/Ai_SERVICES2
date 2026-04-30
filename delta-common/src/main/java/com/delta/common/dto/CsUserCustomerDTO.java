package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客服-客户关联数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "客服-客户关联数据传输对象")
public class CsUserCustomerDTO {

    @Schema(description = "关联ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "客服ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "客服用户ID不能为空")
    @ObfuscatedId
    private Long csUserId;

    @Schema(description = "客户ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "客户用户ID不能为空")
    @ObfuscatedId
    private Long customerUserId;

    @Schema(description = "分配方式", example = "AUTO", allowableValues = {"AUTO", "MANUAL"})
    private String assignType;

    @Schema(description = "分配操作人ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedBy;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;
}
