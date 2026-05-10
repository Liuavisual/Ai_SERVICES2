package com.delta.common.util;

import com.delta.common.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class IdObfuscateUtils {

    private static final byte[] SECRET_BYTES = "DeltaAiCs2024Sec".getBytes(StandardCharsets.UTF_8);
    private static final String PREFIX = "d_";

    private IdObfuscateUtils() {
    }

    public static String encode(Long id) {
        if (id == null) {
            return null;
        }
        byte[] bytes = new byte[8];
        long value = id;
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) ((value & 0xFF) ^ SECRET_BYTES[i % SECRET_BYTES.length]);
            value >>>= 8;
        }
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static Long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        try {
            if (!encoded.startsWith(PREFIX)) {
                return tryParseLong(encoded);
            }
            byte[] bytes = Base64.getUrlDecoder().decode(encoded.substring(PREFIX.length()));
            if (bytes.length != 8) {
                return null;
            }
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | ((bytes[i] ^ SECRET_BYTES[i % SECRET_BYTES.length]) & 0xFF);
            }
            return value;
        } catch (Exception e) {
            return tryParseLong(encoded);
        }
    }

    public static Long decodeRequired(String encoded) {
        Long decoded = decode(encoded);
        if (decoded == null) {
            throw new BusinessException("无效的ID参数");
        }
        return decoded;
    }

    private static Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
