package com.cims.bpm.security.advice.decrypt;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.cims.bpm.config.CimsConfigProperties;
import com.cims.bpm.security.advice.Decrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SM4解密
 */
@Component
@Slf4j
public class Sm4Decrypt implements Decrypt {

    @Autowired
    CimsConfigProperties cimsConfigProperties;

    @Override
    public String execute(String body) {
        byte[] sm4Key = cimsConfigProperties.getSecurity().getSm4Key().getBytes();
        SymmetricCrypto sm4 = SmUtil.sm4(sm4Key);
        byte[] bodyByte = java.util.Base64.getDecoder().decode(body);
        String result = sm4.decryptStr(bodyByte);
        return result;
    }
}
