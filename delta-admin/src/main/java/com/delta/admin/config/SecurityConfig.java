package com.delta.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.delta.common.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Spring Security 安全配置
 * <p>
 * 安全策略：
 * <ul>
 *   <li>JWT无状态认证</li>
 *   <li>CSRF禁用（前后端分离架构）</li>
 *   <li>方法级权限控制：@PreAuthorize</li>
 *   <li>角色权限分层：SYS_ADMIN > CS_LEADER > CS_STAFF</li>
 * </ul>
 * </p>
 *
 * @author delta
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!jwtConfig.isEnabled()) {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/admin/**").hasRole("SYS_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
