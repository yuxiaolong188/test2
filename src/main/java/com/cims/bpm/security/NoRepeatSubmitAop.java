package com.cims.bpm.security;

import javax.servlet.http.HttpServletRequest;

import com.cims.bpm.common.ExtException;
import com.dragon.tools.common.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.flowable.idm.api.User;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.cache.Cache;

@Aspect
@Component
@Slf4j
public class NoRepeatSubmitAop {
    public NoRepeatSubmitAop() {
    }

    @Autowired
    private Cache<String, Integer> cache;


    @Pointcut("@annotation(NoRepeatSubmit)")
    public void repeatSubmit() {
    }

    @Before("repeatSubmit()")
    public void interceptor(JoinPoint joinPoint) throws Throwable {
        //获取当前用户token，识别用户
        User user = SecurityUtils.getCurrentUserObject();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String sessionId = user.getId();
        HttpServletRequest request = attributes.getRequest();
        String key = sessionId + "-" + request.getServletPath();
        if (cache.getIfPresent(key) == null) {// 如果缓存中有这个url视为重复提交
            cache.put(key, 0);
        } else {
            log.error(sessionId+"重复提交");
            throw new ExtException(ReturnCode.FAIL,"重复提交");
        }
    }

}