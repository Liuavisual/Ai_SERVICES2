package com.delta.common.util;

public class DesensitizeUtils {

    private static final String DEFAULT_MASK = "****";

    private DesensitizeUtils() {
    }

    public static String maskSecret(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 8) {
            return DEFAULT_MASK;
        }
        String prefix = value.substring(0, 4);
        String suffix = value.substring(value.length() - 4);
        return prefix + DEFAULT_MASK + suffix;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() >= 7) {
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
        return DEFAULT_MASK;
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return DEFAULT_MASK;
    }

    public static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        return lowerKey.equals("password") || lowerKey.equals("secret")
                || lowerKey.equals("token") || lowerKey.endsWith("key")
                || lowerKey.endsWith("secret") || lowerKey.endsWith("password")
                || lowerKey.equals("apikey") || lowerKey.equals("api_key")
                || lowerKey.equals("appid") || lowerKey.equals("app_id")
                || lowerKey.equals("aeskey") || lowerKey.equals("aes_key");
    }
}
