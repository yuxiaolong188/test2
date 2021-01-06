package com.cims.bpm.security.advice;

import com.alibaba.fastjson.JSONObject;
import com.cims.bpm.config.CimsConfigProperties;
import com.cims.bpm.security.advice.decrypt.Sm4Decrypt;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * 全局请求解密
 */
@ControllerAdvice(basePackages = "com.cims.bpm")
public class GlobalRequestBodyAdvice implements RequestBodyAdvice {

    @Autowired
    Sm4Decrypt sm4Decrypt;
    @Autowired
    CimsConfigProperties cimsConfigProperties;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        //return false;
        return cimsConfigProperties.getSecurity().isOpen();
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {

        // 获取JSON
        String jsonString = IOUtils.toString(httpInputMessage.getBody(), "UTF-8");
        JSONObject json = JSONObject.parseObject(jsonString);

        // 解密
        if (StringUtils.isNotEmpty(json.getString("body"))) {
            String body = json.getString("body");
            String sensInf = sm4Decrypt.execute(body);
            return new HttpInputMessage() {
                @Override
                public HttpHeaders getHeaders() {
                    return httpInputMessage.getHeaders();
                }

                @Override
                public InputStream getBody() throws IOException {
                    return IOUtils.toInputStream(sensInf, "UTF-8");
                }
            };
        }

        // 强制解密
        if (cimsConfigProperties.getSecurity().isOpen()) {
            throw new RuntimeException("解密失败");
        }

        return new HttpInputMessage() {
            @Override
            public HttpHeaders getHeaders() {
                return httpInputMessage.getHeaders();
            }

            @Override
            public InputStream getBody() throws IOException {
                return IOUtils.toInputStream(jsonString, "UTF-8");
            }
        };

    }

    @Override
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }

    @Nullable
    @Override
    public Object handleEmptyBody(@Nullable Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }
}