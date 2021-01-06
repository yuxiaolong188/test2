package com.cims.bpm.service;

import com.cims.bpm.vo.CommentVo;

import java.util.List;

public interface IFlowableCommentService {

    /**
     * 添加备注
     * @param comment 参数
     */
    public void addComment(CommentVo comment) ;

    /**
     * 通过流程实例id获取审批意见列表
     * @param processInstanceId 流程实例id
     * @return
     */
    public List<CommentVo> getFlowCommentVosByProcessInstanceId(String processInstanceId) ;

}
