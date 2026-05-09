package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "登录数据传输对象")
public class LoginDTO {

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    /** 用户名 */
    private String username;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50之间")
    /** 密码 */
    private String password;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    /** 客户端IP */
    private String clientIp;
}
