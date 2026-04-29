package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录信息视图对象")
public class LoginVO extends BaseVO {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9.xxx.xxx")
    private String token;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9.yyy.yyy")
    private String refreshToken;

    @Schema(description = "过期时间(秒)", example = "7200")
    private Long expiresIn;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "角色", example = "ADMIN", allowableValues = {"ADMIN", "CS", "VIEWER"})
    private String role;

    @Schema(description = "角色描述", example = "管理员")
    private String roleDesc;
}
