package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hezh
 */
@ApiModel(description = "委派任务请求对象")
@Data
public class DelegateTaskVo extends BaseProcessVo {
    /**
     * 委派人
     */
    @ApiModelProperty("委派人")
    private String delegateUserCode;

    @ApiModelProperty("用户集合")
    public String[] userCodes;

    public String getDelegateUserCode() {
        return delegateUserCode;
    }

    public void setDelegateUserCode(String delegateUserCode) {
        this.delegateUserCode = delegateUserCode;
    }
}
