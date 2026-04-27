package com.delta.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private String refreshToken;

    private Long expiresIn;

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private String roleDesc;
}
