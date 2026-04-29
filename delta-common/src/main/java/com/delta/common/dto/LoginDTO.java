package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "登录数据传输对象")
public class LoginDTO {

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    /** 用户名 */    private String username;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    /** 密码 */    private String password;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    /** 客户端IP */    private String clientIp;

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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
