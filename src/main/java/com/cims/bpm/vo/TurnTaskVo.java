package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author hezh
 */
@ApiModel(description = "转办请求对象")
@Data
public class TurnTaskVo extends BaseProcessVo {

    /**
     * 被转办人工号 必填
     */
    private String turnToUserId;
    public String getTurnToUserId() {
        return turnToUserId;
    }
    public void setTurnToUserId(String turnToUserId) {
        this.turnToUserId = turnToUserId;
    }

    public String[] userCodes;
}
