package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询某个流程实例对应的表单信息request对象
 * @author hezh
 */
@ApiModel(description = "查询某个流程实例对应的表单信息request对象")
@Data
public class FormInfoQueryVo implements Serializable {

    /**
     * 任务id
     */
    @ApiModelProperty("任务id")
    private String taskId;
    /**
     * 流程实例id
     */
    @ApiModelProperty("流程实例id")
    private String processInstanceId;


    /**
     * 表单id
     */
    @ApiModelProperty("表单id")
    private String businessKey;


    /**
     * 表单Name
     */
    @ApiModelProperty("表单名称")
    private String businessName;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
}
