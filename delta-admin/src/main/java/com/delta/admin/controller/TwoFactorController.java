package com.delta.admin.controller;

import com.delta.common.entity.SysUser;
import com.delta.common.mapper.SysUserMapper;
import com.delta.common.service.RedisService;
import com.delta.common.util.TotpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 双因素认证（2FA）控制器
 * <p>
 * 提供 TOTP 密钥的生成、启用、禁用以及登录过程中的2FA验证功能。
 * 使用标准 TOTP (RFC 6238) 算法，支持 Google Authenticator、Authy 等主流认证器应用。
 * </p>
 *
 * @author 刘建国
 */
@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "双因素认证", description = "TOTP双重认证管理接口")
public class TwoFactorController {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorController.class);

    /** 系统用户Mapper */
    private final SysUserMapper sysUserMapper;

    /** Redis缓存服务，用于临时Token管理 */
    private final RedisService redisService;

    /** 2FA密钥申请临时缓存前缀 */
    private static final String TWO_FACTOR_SETUP_PREFIX = "2fa:setup:";

    /** 2FA密钥申请有效期（分钟） */
    private static final long TWO_FACTOR_SETUP_TTL_MINUTES = 10;

    /**
     * 申请开启2FA，生成TOTP密钥
     * <p>
     * 返回密钥和URI，前端可将URI转为QR码供用户扫描。
     * 密钥暂存Redis，需在10分钟内完成验证激活。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return 包含 secret 和 otpauthUri 的响应
     */
    @GetMapping("/setup")
    @Operation(summary = "申请开启2FA，生成TOTP密钥")
    public ResponseEntity<Map<String, String>> setupTwoFactor(@RequestAttribute Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        String secret = TotpUtils.generateSecret();
        String otpauthUri = String.format("otpauth://totp/DeltaAI:%s?secret=%s&issuer=DeltaAI&algorithm=SHA1&digits=6&period=30",
                user.getUsername(), secret);

        String setupKey = TWO_FACTOR_SETUP_PREFIX + userId;
        redisService.set(setupKey, secret, TWO_FACTOR_SETUP_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("【2FA】用户 {} 申请开启2FA，密钥已暂存", user.getUsername());

        Map<String, String> result = new HashMap<>(4);
        result.put("secret", secret);
        result.put("otpauthUri", otpauthUri);
        return ResponseEntity.ok(result);
    }

    /**
     * 验证并启用2FA
     * <p>
     * 用户需先用认证器扫描QR码获取密钥，然后输入当前验证码完成验证。
     * 验证通过后启用2FA并持久化密钥。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param params 包含 code 字段的请求体
     * @return 操作结果
     */
    @PostMapping("/enable")
    @Operation(summary = "验证并启用2FA")
    public ResponseEntity<Map<String, Object>> enableTwoFactor(
            @RequestAttribute Long userId,
            @RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "验证码不能为空"));
        }

        String setupKey = TWO_FACTOR_SETUP_PREFIX + userId;
        String secret = (String) redisService.get(setupKey);
        if (secret == null || secret.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "密钥已过期，请重新申请"));
        }

        if (!TotpUtils.verifyCode(secret, code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "验证码错误"));
        }

        SysUser user = sysUserMapper.selectById(userId);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        sysUserMapper.updateById(user);

        redisService.delete(setupKey);

        log.info("【2FA】用户 {} 已启用2FA", user.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "message", "双重认证已启用"));
    }

    /**
     * 禁用2FA
     *
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @PostMapping("/disable")
    @Operation(summary = "禁用2FA")
    public ResponseEntity<Map<String, Object>> disableTwoFactor(@RequestAttribute Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        sysUserMapper.updateById(user);

        log.info("【2FA】用户 {} 已禁用2FA", user.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "message", "双重认证已禁用"));
    }

    /**
     * 获取当前用户2FA启用状态
     *
     * @param userId 当前登录用户ID
     * @return 2FA启用状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取2FA启用状态")
    public ResponseEntity<Map<String, Object>> getTwoFactorStatus(@RequestAttribute Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        return ResponseEntity.ok(Map.of(
                "enabled", user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled(),
                "userId", userId
        ));
    }
}