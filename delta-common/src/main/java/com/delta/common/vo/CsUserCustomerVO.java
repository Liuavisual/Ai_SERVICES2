package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CsUserCustomerVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long csUserId;
    private String csUserName;
    @ObfuscatedId
    private Long customerUserId;
    private String customerUserName;
    private String assignType;
    private String assignTypeDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    @ObfuscatedId
    private Long assignedBy;
    private String assignedByName;
    private String status;
    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
