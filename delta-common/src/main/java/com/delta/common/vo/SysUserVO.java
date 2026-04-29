package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户视图对象")
public class SysUserVO extends BaseVO {

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号(脱敏)", example = "138****8000")
    private String phone;

    @Schema(description = "邮箱(脱敏)", example = "a***@example.com")
    private String email;

    @Schema(description = "角色", example = "ADMIN", allowableValues = {"ADMIN", "CS", "VIEWER"})
    private String role;

    @Schema(description = "角色描述", example = "管理员")
    private String roleDesc;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private String status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @JsonIgnore
    private Long createdBy;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public String getPhone() {
        return DesensitizeUtils.maskPhone(phone);
    }

    public String getEmail() {
        return DesensitizeUtils.maskEmail(email);
    }
}
