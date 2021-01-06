package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author hezh
 */
@ApiModel(description = "流程基础VO对象")
public class BaseProcessVo implements Serializable {
    /**********************任务相关的参数**********************/
    /**
     * 任务id 必填
     */
    @ApiModelProperty("任务id")
    private String taskId;
    /**********************审批意见的参数**********************/
    /**
     * 操作人code 必填
     */
    @ApiModelProperty("操作人code")
    private String userCode;
    /**
     * 审批意见 必填
     */
    @ApiModelProperty("审批意见")
    private String message;
    /**
     * 流程实例的id 必填
     */
    @ApiModelProperty("流程实例id")
    private String processInstanceId;
    /**
     * 审批类型 必填
     */
    @ApiModelProperty("审批类型")
    private String type;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
