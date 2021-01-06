package com.cims.bpm.dao;

import com.cims.bpm.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface IFlowableCommentDao {

    /**
     * 通过流程实例id获取审批意见列表
     * @param ProcessInstanceId 流程实例id
     * @return
     */
    public List<CommentVo> getFlowCommentVosByProcessInstanceId(String ProcessInstanceId);

}