package com.delta.admin.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 全局XSS过滤器
 * <p>
 * 对请求参数、请求头和请求体(JSON)进行XSS过滤，防止存储型XSS攻击。
 * </p>
 *
 * @author 刘建国
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<embed[^>]*>.*?</embed>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<svg[^>]*>.*?</svg>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<img[^>]+on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on(error|load|click|mouseover|mouseout|focus|blur|submit|input|change|keydown|keyup)\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("data\\s*:\\s*text/html", Pattern.CASE_INSENSITIVE),
            Pattern.compile("&#(106|74);", Pattern.CASE_INSENSITIVE),
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }

        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            chain.doFilter(new XssJsonRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(new XssParamRequestWrapper(httpRequest), response);
        }
    }

    static String stripXss(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        for (Pattern pattern : XSS_PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }
        return value;
    }

    static class XssParamRequestWrapper extends HttpServletRequestWrapper {
        XssParamRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return stripXss(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = stripXss(values[i]);
            }
            return cleaned;
        }

        @Override
        public String getHeader(String name) {
            return stripXss(super.getHeader(name));
        }
    }

    static class XssJsonRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        XssJsonRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonBody = sb.toString();
            body = stripXss(jsonBody).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() { return bais.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(ReadListener listener) {}
                @Override
                public int read() { return bais.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
