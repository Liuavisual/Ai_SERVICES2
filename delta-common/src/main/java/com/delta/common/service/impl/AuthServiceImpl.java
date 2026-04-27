package com.delta.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.delta.common.dto.LoginDTO;
import com.delta.common.dto.RegisterDTO;
import com.delta.common.entity.SysUser;
import com.delta.common.enums.RoleEnum;
import com.delta.common.enums.UserStatusEnum;
import com.delta.common.exception.BusinessException;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.service.AuthService;
import com.delta.common.service.RedisService;
import com.delta.common.util.JwtUtils;
import com.delta.common.vo.LoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_LOCK_DURATION_SECONDS = 15 * 60;
    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d).{8,50}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,50}$"
    );

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String clientIp = loginDTO.getClientIp();

        checkLoginAttempts(username, clientIp);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null || user.getDeleted() == 1) {
            recordLoginFailure(username, clientIp);
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFailure(username, clientIp);
            throw new BusinessException("用户名或密码错误");
        }

        if (UserStatusEnum.PENDING.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号待审核，请联系客服负责人");
        }

        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系系统管理员");
        }

        clearLoginAttempts(username, clientIp);

        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        long expiresIn = jwtUtils.getExpirationFromNow() / 1000;

        RoleEnum roleEnum = RoleEnum.fromCode(user.getRole());

        log.info("用户登录成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());

        return new LoginVO(
                accessToken,
                refreshToken,
                expiresIn,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                roleEnum != null ? roleEnum.getDesc() : user.getRole()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        if (!USERNAME_PATTERN.matcher(registerDTO.getUsername()).matches()) {
            throw new BusinessException("用户名只能包含字母、数字、下划线和中文，长度3-50位");
        }

        if (!PASSWORD_PATTERN.matcher(registerDTO.getPassword()).matches()) {
            throw new BusinessException("密码必须包含字母和数字，长度8-50位");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerDTO.getUsername());
        SysUser existUser = sysUserMapper.selectOne(wrapper);

        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUser::getPhone, registerDTO.getPhone());
            if (sysUserMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException("该手机号已被注册");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(RoleEnum.CS_STAFF.getCode());
        user.setStatus(UserStatusEnum.PENDING.getCode());

        sysUserMapper.insert(user);
        log.info("新用户注册: username={}, realName={}", registerDTO.getUsername(), registerDTO.getRealName());
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException("刷新令牌不能为空");
        }

        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }

        if (jwtUtils.isTokenExpired(refreshToken)) {
            throw new BusinessException("刷新令牌已过期，请重新登录");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        String newAccessToken = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        long expiresIn = jwtUtils.getExpirationFromNow() / 1000;

        RoleEnum roleEnum = RoleEnum.fromCode(user.getRole());

        log.info("令牌刷新成功: userId={}, username={}", userId, username);

        return new LoginVO(
                newAccessToken,
                newRefreshToken,
                expiresIn,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                roleEnum != null ? roleEnum.getDesc() : user.getRole()
        );
    }

    private void checkLoginAttempts(String username, String clientIp) {
        String lockKey = LOGIN_LOCK_PREFIX + username + ":" + (clientIp != null ? clientIp : "unknown");
        if (Boolean.TRUE.equals(redisService.hasKey(lockKey))) {
            Long ttl = redisService.getExpire(lockKey);
            long remainingMinutes = ttl != null && ttl > 0 ? (ttl / 60) + 1 : 15;
            throw new BusinessException("登录失败次数过多，请" + remainingMinutes + "分钟后再试");
        }
    }

    private void recordLoginFailure(String username, String clientIp) {
        String key = username + ":" + (clientIp != null ? clientIp : "unknown");
        String attemptKey = LOGIN_ATTEMPT_PREFIX + key;
        String lockKey = LOGIN_LOCK_PREFIX + key;

        Long attempts = redisService.increment(attemptKey);
        if (attempts != null && attempts == 1) {
            redisService.expire(attemptKey, LOGIN_LOCK_DURATION_SECONDS + 60, TimeUnit.SECONDS);
        }

        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            redisService.set(lockKey, "1", LOGIN_LOCK_DURATION_SECONDS, TimeUnit.SECONDS);
            redisService.delete(attemptKey);
        }

        log.warn("登录失败: username={}, attempts={}/{}", username, attempts, MAX_LOGIN_ATTEMPTS);
    }

    private void clearLoginAttempts(String username, String clientIp) {
        String key = username + ":" + (clientIp != null ? clientIp : "unknown");
        redisService.delete(LOGIN_ATTEMPT_PREFIX + key);
        redisService.delete(LOGIN_LOCK_PREFIX + key);
    }
}
