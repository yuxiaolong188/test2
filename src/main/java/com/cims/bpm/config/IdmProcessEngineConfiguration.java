package com.cims.bpm.config;

import org.flowable.idm.engine.IdmEngineConfiguration;
import org.flowable.idm.spring.SpringIdmEngineConfiguration;
import org.flowable.idm.spring.authentication.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author hezh
 */
@Configuration
public class IdmProcessEngineConfiguration extends SpringIdmEngineConfiguration {

    /**
     * spring security中的BCryptPasswordEncoder方法采用SHA-256 +随机盐+密钥对密码进行加密
     * 加密的hashPass中，前部分已经包含了盐信息
     * */
    @Bean
    public PasswordEncoder bCryptEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringEncoder passwordEncoder(){
        return new SpringEncoder(bCryptEncoder());
    }

    @Override
    public IdmEngineConfiguration setPasswordEncoder(org.flowable.idm.api.PasswordEncoder passwordEncoder) {
        return super.setPasswordEncoder(passwordEncoder());
    }
}
