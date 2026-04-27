package com.delta.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web全局配置 - 分页参数限制
 *
 * @author delta
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PageSizeLimitInterceptor pageSizeLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pageSizeLimitInterceptor);
    }
}
