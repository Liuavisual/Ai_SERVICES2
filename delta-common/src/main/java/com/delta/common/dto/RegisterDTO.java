package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "用户注册数据传输对象")
public class RegisterDTO {

    @Schema(description = "用户名", example = "newuser")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    /** 用户名 */
    private String username;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50之间")
    /** 密码 */
    private String password;

    @Schema(description = "真实姓名", example = "李四")
    @NotBlank(message = "真实姓名不能为空")
    /** 真实姓名 */
    private String realName;

    @Schema(description = "手机号", example = "13900139000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    /** 手机号 */
    private String phone;

    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    /** 邮箱 */
    private String email;
}
