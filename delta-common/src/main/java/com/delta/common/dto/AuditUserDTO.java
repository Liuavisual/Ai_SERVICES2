package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户审核数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "用户审核数据传输对象")
public class AuditUserDTO {

    @Schema(description = "待审核的用户ID", example = "1001")
    @NotNull(message = "用户ID不能为空")
    /** 待审核的用户ID */    private Long userId;

    @Schema(description = "审核结果状态", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"})
    @NotBlank(message = "审核状态不能为空")
    /** 审核结果状态 */    private String status;

    @Schema(description = "审核备注", example = "资料审核通过")
    /** 审核备注 */    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
