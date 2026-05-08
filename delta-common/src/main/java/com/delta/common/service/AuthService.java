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
}
