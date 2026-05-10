package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户视图对象")
public class CustomerVO extends BaseVO {

    @Schema(description = "客户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "平台用户ID", example = "wx_user_001")
    private String platformUserId;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar/001.jpg")
    private String avatar;

    @Schema(description = "是否启用AI回复", example = "true")
    private Boolean aiEnabled;

    @Schema(description = "分配的客服ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedCsUserId;

    @Schema(description = "分配的客服名称", example = "客服小李")
    private String assignedCsUserName;

    @Schema(description = "消息数量", example = "56")
    private Integer messageCount;

    @Schema(description = "最后活跃时间", example = "2026-01-01 10:00:00")
    private LocalDateTime lastActiveAt;

    @Schema(description = "生命周期阶段", example = "ACTIVE", allowableValues = {"NEW", "ACTIVE", "LOYAL", "AT_RISK", "CHURNED"})
    private String lifecycleStage;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
