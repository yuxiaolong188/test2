package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 添加bpmn流程定义model
 * @author hezh
 */
@ApiModel(description = "添加bpmn流程定义model")
public class ModelVo implements Serializable {
    /**
     * 流程id
     */
    @ApiModelProperty("流程id")
    private String processId;

    /**
     * 流程名称
     */
    @ApiModelProperty("流程名称")
    private String processName;

    /**
     * 分类Id
     */
    @ApiModelProperty("流程分类id")
    private String categoryId;

    /**
     * 流程的xml
     */
    @ApiModelProperty("流程xml格式的字符串")
    private String xml;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}