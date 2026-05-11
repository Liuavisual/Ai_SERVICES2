package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "订单创建数据传输对象")
public class OrderCreateDTO {

    @Schema(description = "用户ID", example = "d_xxxxx")
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    @NotBlank(message = "陪玩师ID不能为空")
    private String companionId;

    @Schema(description = "服务类型", example = "陪玩", allowableValues = {"陪玩", "语音", "视频"})
    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    @Schema(description = "预约开始时间", example = "2026-01-01T10:00:00")
    @NotNull(message = "预约开始时间不能为空")
    private LocalDateTime scheduledStart;

    @Schema(description = "预约结束时间", example = "2026-01-01T12:00:00")
    @NotNull(message = "预约结束时间不能为空")
    private LocalDateTime scheduledEnd;

    @Schema(description = "游戏类型", example = "王者荣耀")
    private String gameType;

    @Schema(description = "备注", example = "希望选择擅长打野的陪玩师")
    private String remark;

    @Schema(description = "时间选择方式", example = "SYSTEM", allowableValues = {"SYSTEM", "CUSTOM"})
    private String timeSource;

    @Schema(description = "关联排班记录ID，选择系统推荐时间时传入")
    private Long scheduleId;

    @Schema(description = "来源", example = "微信", allowableValues = {"微信", "企微", "APP", "网页"})
    private String source;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCompanionId() { return companionId; }
    public void setCompanionId(String companionId) { this.companionId = companionId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(LocalDateTime scheduledStart) { this.scheduledStart = scheduledStart; }

    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(LocalDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getTimeSource() { return timeSource; }
    public void setTimeSource(String timeSource) { this.timeSource = timeSource; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
