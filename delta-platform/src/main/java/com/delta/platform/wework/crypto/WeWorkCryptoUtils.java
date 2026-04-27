package com.delta.platform.wework.crypto;

import com.delta.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

@Slf4j
public class WeWorkCryptoUtils {

    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final String AES_KEY_ALGORITHM = "AES";

    private final String token;
    private final String encodingAESKey;

    public WeWorkCryptoUtils(String token, String encodingAESKey) {
        this.token = token;
        this.encodingAESKey = encodingAESKey;
    }

    public String verifySignature(String msgSignature, String timestamp, String nonce, String echostr) {
        if (!verifySignatureInternal(msgSignature, timestamp, nonce, echostr)) {
            throw new BusinessException("企业微信签名验证失败");
        }
        return echostr;
    }

    public String decryptMessage(String msgSignature, String timestamp, String nonce, String encryptXml) {
        if (!verifySignatureInternal(msgSignature, timestamp, nonce, encryptXml)) {
            throw new BusinessException("企业微信消息签名验证失败");
        }
        return decrypt(encryptXml);
    }

    private boolean verifySignatureInternal(String msgSignature, String timestamp, String nonce, String content) {
        if (token == null || token.isEmpty() || msgSignature == null) {
            return false;
        }
        try {
            String[] arr = new String[]{token, timestamp, nonce, content};
            Arrays.sort(arr);
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().equals(msgSignature);
        } catch (Exception e) {
            log.warn("签名验证异常: {}", e.getMessage());
            return false;
        }
    }

    private String decrypt(String encrypted) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(encodingAESKey + "=");
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, AES_KEY_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOf(aesKey, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            byte[] content = new byte[decrypted.length - 20];
            System.arraycopy(decrypted, 20, content, 0, content.length);
            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("企业微信消息解密失败: " + e.getMessage());
        }
    }

    public static String generateNonce() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
