package com.cims.bpm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author hezh
 */
@ApiModel(description = "驳回请求对象")
public class BackTaskVo extends BaseProcessVo {

    /**
     * 需要驳回的节点id 必填
     */
    @ApiModelProperty(value = "需要驳回的节点id")
    private String distFlowElementId;

    public String getDistFlowElementId() {
        return distFlowElementId;
    }

    public void setDistFlowElementId(String distFlowElementId) {
        this.distFlowElementId = distFlowElementId;
    }
}
