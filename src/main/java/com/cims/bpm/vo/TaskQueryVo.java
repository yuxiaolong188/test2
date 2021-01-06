package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 任务查询vo对象
 * @author hezh
 */
@ApiModel(description = "任务请求vo对象")
public class TaskQueryVo implements Serializable {

    /**
     * 用户工号
     */
    @ApiModelProperty("用户工号")
    private String userCode;

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
