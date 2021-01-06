package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hezh
 */
@ApiModel(description = "加签对象")
@Data
public class AddSignTaskVo extends BaseProcessVo {
    /**
     * 被加签人
     */
    @ApiModelProperty("被加签人列表")
    private List<String> signPersoneds;

    public List<String> getSignPersoneds() {
        return signPersoneds;
    }

    public void setSignPersoneds(List<String> signPersoneds) {
        this.signPersoneds = signPersoneds;
    }


    public String[] userCodes;
}
