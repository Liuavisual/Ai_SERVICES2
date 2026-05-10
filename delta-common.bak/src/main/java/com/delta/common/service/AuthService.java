package com.delta.common.service;

import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.vo.LoginVO;

/**
 * 认证服务接口，提供登录、注册、Token刷新等认证功能
 *
 * @author 刘建国
 */
public interface AuthService {
    
    LoginVO login(LoginDTO loginDTO);
    
    void register(RegisterDTO registerDTO);

    LoginVO refreshToken(String refreshToken);

    /**
     * 验证2FA验证码并签发正式JWT令牌
     *
     * @param twoFactorToken 2FA临时令牌（从登录响应中获取）
     * @param code           TOTP验证码（6位数字）
     * @return 登录信息（包含accessToken和refreshToken）
     */
    LoginVO verifyTwoFactor(String twoFactorToken, String code);
}
