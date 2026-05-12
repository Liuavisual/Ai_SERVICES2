package com.delta.common.resolver;

import com.delta.common.annotation.DecodeId;
import com.delta.common.util.IdObfuscateUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@SuppressWarnings("null")
public class DecodeIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DecodeId.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        DecodeId annotation = parameter.getParameterAnnotation(DecodeId.class);
        if (annotation == null) {
            return null;
        }
        String paramName = resolveParamName(parameter);
        String rawValue = resolveRawValue(parameter, webRequest, paramName);

        if (rawValue == null || rawValue.trim().isEmpty()) {
            if (annotation.required()) {
                throw new IllegalArgumentException("参数 '" + paramName + "' 不能为空");
            }
            return null;
        }

        return IdObfuscateUtils.decodeRequired(rawValue);
    }

    private String resolveParamName(MethodParameter parameter) {
        PathVariable pathVar = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVar != null && !pathVar.value().isEmpty()) {
            return pathVar.value();
        }

        RequestParam reqParam = parameter.getParameterAnnotation(RequestParam.class);
        if (reqParam != null) {
            if (!reqParam.value().isEmpty()) {
                return reqParam.value();
            }
            if (!reqParam.name().isEmpty()) {
                return reqParam.name();
            }
        }

        return parameter.getParameterName();
    }

    @SuppressWarnings("unchecked")
    private String resolveRawValue(MethodParameter parameter, NativeWebRequest webRequest, String paramName) {
        if (paramName == null) {
            return null;
        }
        PathVariable pathVar = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVar != null) {
            Map<String, String> uriVars = (Map<String, String>) webRequest.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if (uriVars != null) {
                return uriVars.get(paramName);
            }
        }

        return webRequest.getParameter(paramName);
    }
}