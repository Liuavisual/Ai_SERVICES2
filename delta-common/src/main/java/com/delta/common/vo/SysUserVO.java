package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private String roleDesc;
    private String status;
    private String statusDesc;
    @JsonIgnore
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public String getPhone() {
        return DesensitizeUtils.maskPhone(phone);
    }

    public String getEmail() {
        return DesensitizeUtils.maskEmail(email);
    }
}
