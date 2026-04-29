package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统用户数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "系统用户数据传输对象")
public class SysUserDTO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    /** 用户名 */    private String username;

    @Schema(description = "密码", example = "12345678")
    /** 密码 */    private String password;

    @Schema(description = "真实姓名", example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    /** 真实姓名 */    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    /** 手机号 */    private String phone;

    @Schema(description = "邮箱", example = "admin@example.com")
    @Email(message = "邮箱格式不正确")
    /** 邮箱 */    private String email;

    @Schema(description = "角色", example = "ADMIN", allowableValues = {"ADMIN", "CS", "VIEWER"})
    @NotBlank(message = "角色不能为空")
    /** 角色 */    private String role;

    @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    /** 状态 */    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
