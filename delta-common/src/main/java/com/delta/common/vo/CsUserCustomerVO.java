package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客服-客户关联视图对象")
public class CsUserCustomerVO extends BaseVO {

    @Schema(description = "关联ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "客服ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long csUserId;

    @Schema(description = "客服名称", example = "客服小李")
    private String csUserName;

    @Schema(description = "客户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long customerUserId;

    @Schema(description = "客户名称", example = "小明")
    private String customerUserName;

    @Schema(description = "分配方式", example = "AUTO", allowableValues = {"AUTO", "MANUAL"})
    private String assignType;

    @Schema(description = "分配方式描述", example = "自动分配")
    private String assignTypeDesc;

    @Schema(description = "分配时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    @Schema(description = "分配操作人ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedBy;

    @Schema(description = "分配操作人名称", example = "管理员")
    private String assignedByName;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(description = "状态描述", example = "有效")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
