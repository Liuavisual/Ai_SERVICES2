package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客服-客户关联数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "客服-客户关联数据传输对象")
public class CsUserCustomerDTO {

    @Schema(description = "关联ID", example = "1")
    private Long id;

    @Schema(description = "客服ID", example = "3001")
    @NotNull(message = "客服用户ID不能为空")
    /** 客服ID */    private Long csUserId;

    @Schema(description = "客户ID", example = "1001")
    @NotNull(message = "客户用户ID不能为空")
    /** 客户ID */    private Long customerUserId;

    @Schema(description = "分配方式", example = "AUTO", allowableValues = {"AUTO", "MANUAL"})
    /** 分配方式 */    private String assignType;

    @Schema(description = "分配操作人ID", example = "1")
    /** 分配操作人ID */    private Long assignedBy;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    /** 状态 */    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCsUserId() {
        return csUserId;
    }

    public void setCsUserId(Long csUserId) {
        this.csUserId = csUserId;
    }

    public Long getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(Long customerUserId) {
        this.customerUserId = customerUserId;
    }

    public String getAssignType() {
        return assignType;
    }

    public void setAssignType(String assignType) {
        this.assignType = assignType;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
