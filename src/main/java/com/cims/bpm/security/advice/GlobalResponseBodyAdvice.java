package com.cims.bpm.security.advice;

import com.alibaba.fastjson.JSONObject;
import com.cims.bpm.config.CimsConfigProperties;
import com.cims.bpm.security.advice.annotation.Encryptable;
import com.cims.bpm.security.advice.encrypt.Sm4Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局响应加密
 */
@ControllerAdvice(basePackages = "com.cims.bpm")
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    Sm4Encrypt sm4Encrypt;
    @Autowired
    CimsConfigProperties cimsConfigProperties;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        //return false;
        return cimsConfigProperties.getSecurity().isOpen();
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        // 根据接口上的注解判断是否加密
        if (methodParameter.getMethodAnnotation(Encryptable.class) != null) {
            if (!methodParameter.getMethodAnnotation(Encryptable.class).enable()) {
                return o;
            }
        }

        // 获取JSON
        String jsonString = JSONObject.toJSONString(o);

        // 加密
        String result = sm4Encrypt.execute(jsonString);
        Map map = new HashMap();
        map.put("body", result);

        return map;
    }
}