package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录数据传输对象
 *
 * @author delta
 */
@Data
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    /** 用户名 */    private String username;
    
    @NotBlank(message = "密码不能为空")
    /** 密码 */    private String password;

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
