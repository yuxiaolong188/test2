package com.cims.bpm.service;

import com.cims.bpm.vo.ProcessDefinitionQueryVo;
import com.cims.bpm.vo.ProcessDefinitionVo;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;

public interface IFlowableProcessDefinitionService {

    /**
     * 通过条件查询流程定义
     *
     * @param params
     * @return
     */
    public PagerModel<ProcessDefinitionVo> getPagerModel(ProcessDefinitionQueryVo params, Query query);

    /**
     * 通过流程定义id获取流程定义的信息
     *
     * @param processDefinitionId 流程定义id
     * @return
     */
    public ProcessDefinitionVo getById(String processDefinitionId);

    /**
     * 挂起流程定义
     *
     * @param processDefinitionId 流程定义id
     * @param suspensionState     状态1挂起 2激活
     */
    public ReturnVo suspendOrActivateProcessDefinitionById(String processDefinitionId, int suspensionState);

}