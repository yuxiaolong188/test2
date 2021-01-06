package com.cims.bpm.vo;

import java.io.Serializable;

public class ProcessDefinitionQueryVo implements Serializable {

    private String name;
    private String modelKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelKey() {
        return modelKey;
    }

    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }
}
