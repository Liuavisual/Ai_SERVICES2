package com.delta.admin.config;

import com.delta.common.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web全局配置 - 拦截器注册
 *
 * @author 刘建国
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PageSizeLimitInterceptor pageSizeLimitInterceptor;

    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pageSizeLimitInterceptor);
        // 权限校验拦截器 - 检查 @RequirePermission 注解
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/auth/**", "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**", "/v1/ws/**");
    }
}
