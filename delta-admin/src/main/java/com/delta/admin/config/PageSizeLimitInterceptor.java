package com.delta.admin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PageSizeLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PageSizeLimitInterceptor.class);

    private static final String PAGE_SIZE_PARAM = "pageSize";
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String pageSizeStr = request.getParameter(PAGE_SIZE_PARAM);
        if (pageSizeStr != null) {
            try {
                int pageSize = Integer.parseInt(pageSizeStr);
                if (pageSize > MAX_PAGE_SIZE) {
                    request.setAttribute(PAGE_SIZE_PARAM, String.valueOf(MAX_PAGE_SIZE));
                }
            } catch (NumberFormatException e) {
                log.debug("pageSize参数格式错误: {}", pageSizeStr);
            }
        }
        return true;
    }
}
