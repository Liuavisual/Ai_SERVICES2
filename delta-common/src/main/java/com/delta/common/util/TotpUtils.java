package com.delta.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * TOTP (基于时间的一次性密码) 工具类
 * <p>
 * 实现 RFC 6238 标准的 TOTP 算法，用于双因素认证（2FA）。
 * 使用 HMAC-SHA1 算法，生成6位数字验证码，默认30秒刷新。
 * 支持密钥生成、验证码生成、验证码校验等完整流程。
 * </p>
 *
 * @author 刘建国
 */
public final class TotpUtils {

    private static final Logger log = LoggerFactory.getLogger(TotpUtils.class);

    /** TOTP时间步长（秒） */
    private static final int TIME_STEP_SECONDS = 30;

    /** 验证码位数 */
    private static final int CODE_DIGITS = 6;

    /** HMAC算法 */
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /** 密钥字节长度 */
    private static final int SECRET_BYTE_LENGTH = 20;

    /** 允许的时间偏移窗口数（前后各1个窗口，共3个窗口，90秒容错） */
    private static final int TIME_WINDOW_TOLERANCE = 1;

    /** Base32字符集 */
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private TotpUtils() {
    }

    /**
     * 生成随机的TOTP密钥（Base32编码）
     *
     * @return Base32编码的密钥字符串，长度为32字符
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_BYTE_LENGTH];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * 生成当前时间的TOTP验证码
     *
     * @param base32Secret Base32编码的密钥
     * @return 6位数字验证码
     */
    public static String generateCode(String base32Secret) {
        byte[] key = base32Decode(base32Secret);
        long counter = getCurrentCounter();
        return generateCode(key, counter);
    }

    /**
     * 验证TOTP验证码是否有效
     * <p>
     * 允许时间窗口前后各 TIME_WINDOW_TOLERANCE 个窗口的偏差（共容错90秒），
     * 解决客户端和服务端时间不同步的问题。
     * </p>
     *
     * @param base32Secret Base32编码的密钥
     * @param code         待验证的6位数字验证码
     * @return true-验证通过，false-验证失败
     */
    public static boolean verifyCode(String base32Secret, String code) {
        if (base32Secret == null || code == null) {
            return false;
        }

        byte[] key = base32Decode(base32Secret);
        long currentCounter = getCurrentCounter();

        for (int i = -TIME_WINDOW_TOLERANCE; i <= TIME_WINDOW_TOLERANCE; i++) {
            String expectedCode = generateCode(key, currentCounter + i);
            if (expectedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据计数器和密钥生成验证码
     *
     * @param key     密钥字节数组
     * @param counter 时间计数器
     * @return 6位数字验证码
     */
    private static String generateCode(byte[] key, long counter) {
        byte[] counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, "RAW");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("TOTP验证码生成异常", e);
            throw new RuntimeException("TOTP验证码生成失败", e);
        }
    }

    /**
     * 获取当前时间计数器（从Unix纪元开始的30秒步数）
     *
     * @return 时间计数器值
     */
    private static long getCurrentCounter() {
        return System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
    }

    /**
     * Base32编码
     *
     * @param data 原始字节数组
     * @return Base32编码字符串
     */
    private static String base32Encode(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;

            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                result.append(BASE32_ALPHABET.charAt(index));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32_ALPHABET.charAt(index));
        }

        return result.toString();
    }

    /**
     * Base32解码
     *
     * @param base32Str Base32编码字符串
     * @return 原始字节数组
     */
    private static byte[] base32Decode(String base32Str) {
        String normalized = base32Str.toUpperCase().trim();
        int byteLength = normalized.length() * 5 / 8;
        byte[] result = new byte[byteLength];

        int buffer = 0;
        int bitsLeft = 0;
        int resultIndex = 0;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int value = BASE32_ALPHABET.indexOf(c);
            if (value == -1) {
                continue;
            }

            buffer = (buffer << 5) | value;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                result[resultIndex++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }

        return result;
    }
}