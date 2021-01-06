package com.cims.bpm.security.advice.encrypt;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.cims.bpm.config.CimsConfigProperties;
import com.cims.bpm.security.advice.Decrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SM4加密
 */
@Component
@Slf4j
public class Sm4Encrypt implements Decrypt {

    @Autowired
    CimsConfigProperties cimsConfigProperties;

    @Override
    public String execute(String body) {
        byte[] sm4Key = cimsConfigProperties.getSecurity().getSm4Key().getBytes();
        SymmetricCrypto sm4 = SmUtil.sm4(sm4Key);
        String result = sm4.encryptBase64(body.trim());
        return result;
    }
}
