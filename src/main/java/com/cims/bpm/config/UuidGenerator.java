package com.cims.bpm.config;

import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;


/**
 * @author hezh
 */
public class UuidGenerator extends StrongUuidGenerator {

    @Override
    public String getNextId() {
        String uuid = super.getNextId();
        uuid = uuid.replaceAll("-", "");
        return uuid;
    }

}
