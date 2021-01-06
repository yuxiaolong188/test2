package com.cims.bpm.dao;

import com.cims.bpm.vo.ProcessDefinitionQueryVo;
import com.cims.bpm.vo.ProcessDefinitionVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface IFlowableProcessDefinitionDao {

    /**
     * 通过条件查询流程定义列表
     * @param params 参数
     * @return
     */
    public Page<ProcessDefinitionVo> getPagerModel(ProcessDefinitionQueryVo params) ;

    /**
     * 通过流程定义id获取流程定义的信息
     * @param processDefinitionId 流程定义id
     * @return
     */
    public ProcessDefinitionVo getById(String processDefinitionId) ;
}
