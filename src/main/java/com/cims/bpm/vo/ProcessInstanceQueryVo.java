package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 流程实例查询vo对象
 * @author hezh
 */
@ApiModel(description = "流程实例查询vo对象")
public class ProcessInstanceQueryVo implements Serializable {

    @ApiModelProperty(value = "formName，对应数据库表act_hi_procinst.NAME_")
    private String formName;

    @ApiModelProperty(value = "用户code码")
    private String userCode;

    @ApiModelProperty(value = "用户名")
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }
}
