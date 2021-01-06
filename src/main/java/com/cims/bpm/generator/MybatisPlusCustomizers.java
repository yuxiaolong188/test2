package com.cims.bpm.generator;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;


public class MybatisPlusCustomizers implements ConfigurationCustomizer  {

    @Override
    public void customize(Configuration configuration) {
        configuration.setJdbcTypeForNull(JdbcType.NULL);
    }
}
