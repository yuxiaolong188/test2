package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * 流转任务请求id
 * @author hezh
 */
@ApiModel(description = "流转任务请求id")
public class CompleteTaskVo extends BaseProcessVo {
    /**
     * 任务参数 选填
     */
    @ApiModelProperty("任务参数map")
    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
