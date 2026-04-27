package com.delta.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CsUserCustomerVO {
    
    private Long id;
    
    private Long csUserId;
    
    private String csUserName;
    
    private Long customerUserId;
    
    private String customerUserName;
    
    private String assignType;
    
    private String assignTypeDesc;
    
    private LocalDateTime assignedAt;
    
    private Long assignedBy;
    
    private String assignedByName;
    
    private String status;
    
    private String statusDesc;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

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

    public String getCsUserName() {
        return csUserName;
    }

    public void setCsUserName(String csUserName) {
        this.csUserName = csUserName;
    }

    public Long getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(Long customerUserId) {
        this.customerUserId = customerUserId;
    }

    public String getCustomerUserName() {
        return customerUserName;
    }

    public void setCustomerUserName(String customerUserName) {
        this.customerUserName = customerUserName;
    }

    public String getAssignType() {
        return assignType;
    }

    public void setAssignType(String assignType) {
        this.assignType = assignType;
    }

    public String getAssignTypeDesc() {
        return assignTypeDesc;
    }

    public void setAssignTypeDesc(String assignTypeDesc) {
        this.assignTypeDesc = assignTypeDesc;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
