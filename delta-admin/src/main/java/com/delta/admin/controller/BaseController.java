package com.delta.admin.controller;

import com.delta.common.util.IdObfuscateUtils;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    protected Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    protected String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role != null ? role.toString() : null;
    }

    protected String getCurrentUserName(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "";
    }

    protected Long decodeId(String obfuscatedId) {
        return IdObfuscateUtils.decodeRequired(obfuscatedId);
    }
}
