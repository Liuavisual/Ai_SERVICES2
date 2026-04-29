package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO extends BaseVO {

    private String token;
    private String refreshToken;
    private Long expiresIn;
    @ObfuscatedId
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String roleDesc;
}
