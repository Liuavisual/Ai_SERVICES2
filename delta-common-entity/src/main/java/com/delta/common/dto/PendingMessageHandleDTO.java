package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 待处理消息处理数据传输对象
 * <p>
 * 用于接手/完成待办事项操作。
 * id字段使用@ObfuscatedId注解，支持前端传入混淆后的ID字符串（如"d_xxxxx"），
 * Jackson自动解码为Long类型。
 * </p>
 *
 * @author 刘建国
 */
@Data
@Schema(description = "待处理消息处理数据传输对象")
public class PendingMessageHandleDTO {

    @Schema(description = "待处理消息ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "待处理消息ID不能为空")
    @ObfuscatedId
    private Long id;

    @Schema(description = "目标状态", example = "processing", allowableValues = {"pending", "processing", "resolved"})
    @NotNull(message = "处理状态不能为空")
    private String status;

    @Schema(description = "处理备注", example = "已联系用户解决问题")
    private String remark;

    @Schema(description = "处理人ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long handledBy;
}
