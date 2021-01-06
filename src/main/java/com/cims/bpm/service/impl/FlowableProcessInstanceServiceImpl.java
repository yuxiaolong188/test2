package com.cims.bpm.service.impl;

import com.cims.bpm.cmd.DeleteFlowableProcessInstanceCmd;
import com.cims.bpm.common.CommentTypeEnum;
import com.cims.bpm.common.FlowConstant;
import com.cims.bpm.dao.IFlowableProcessInstanceDao;
import com.cims.bpm.vo.*;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cims.bpm.service.FlowProcessDiagramGenerator;
import com.cims.bpm.service.IFlowableBpmnModelService;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import com.cims.bpm.service.IFlowableTaskService;
import com.cims.bpm.vo.*;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.common.engine.impl.util.IoUtil;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlowableProcessInstanceServiceImpl extends BaseProcessService implements IFlowableProcessInstanceService {

    @Autowired
    private IFlowableBpmnModelService flowableBpmnModelService;
    @Autowired
    private IFlowableProcessInstanceDao flowableProcessInstanceDao;
    @Autowired
    private FlowProcessDiagramGenerator flowProcessDiagramGenerator;
    @Autowired
    private IFlowableTaskService flowableTaskService;

    @Override
    public PagerModel<ProcessInstanceVo> getPagerModel(ProcessInstanceQueryVo params, Query query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<ProcessInstanceVo> page = flowableProcessInstanceDao.getPagerModel(params);
        page.forEach(processInstanceVo -> {
            this.setStateApprover(processInstanceVo);
        });
        return new PagerModel<>(page);
    }

    /**
     * 设置状态和审批人
     *
     * @param processInstanceVo 参数
     */
    private void setStateApprover(ProcessInstanceVo processInstanceVo) {
        if (processInstanceVo.getEndTime() == null) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceVo.getProcessInstanceId())
                    .singleResult();
            if (processInstance.isSuspended()) {
                processInstanceVo.setSuspensionState(FlowConstant.SUSPENSION_STATE);
            } else {
                processInstanceVo.setSuspensionState(FlowConstant.ACTIVATE_STATE);
            }
        }
        List<User> approvers = flowableTaskService.getApprovers(processInstanceVo.getProcessInstanceId());
        String userNames = this.createApprovers(approvers);
        processInstanceVo.setApprover(userNames);
    }

    /**
     * 组合审批人显示名称
     *
     * @param approvers 审批人列表
     * @return
     */
    private String createApprovers(List<User> approvers) {
        if (CollectionUtils.isNotEmpty(approvers)) {
            StringBuffer approverstr = new StringBuffer();
            StringBuffer finalApproverstr = approverstr;
            approvers.forEach(user -> {
                finalApproverstr.append(user.getDisplayName()).append(";");
            });
            if (approverstr.length() > 0) {
                approverstr = approverstr.deleteCharAt(approverstr.length() - 1);
            }
            return approverstr.toString();
        }
        return null;
    }

    @Override
    public ReturnVo<ProcessInstance> startProcessInstanceByKey(StartProcessInstanceVo params) {
        ReturnVo<ProcessInstance> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "启动成功");
        if (StringUtils.isNotBlank(params.getProcessDefinitionKey())
                && StringUtils.isNotBlank(params.getBusinessKey())
                && StringUtils.isNotBlank(params.getSystemSn())) {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(params.getProcessDefinitionKey())
                    .latestVersion().singleResult();
            if (processDefinition != null && processDefinition.isSuspended()) {
                returnVo = new ReturnVo<>(ReturnCode.FAIL, "此流程已经挂起,请联系系统管理员!");
                return returnVo;
            }
            /**
             * 1、设置变量
             * 1.1、设置提交人字段为空字符串让其自动跳过
             * 1.2、设置可以自动跳过
             * 1.3、汇报线的参数设置
             */
            //1.1、设置提交人字段为空字符串让其自动跳过
            params.getVariables().put(FlowConstant.FLOW_SUBMITTER_VAR, "");
            //1.2、设置可以自动跳过
            params.getVariables().put(FlowConstant.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);
            // TODO 1.3、汇报线的参数设置
            //2、当我们流程创建人和发起人
            String creator = params.getCreator();
            if (StringUtils.isBlank(creator)) {
                creator = params.getCurrentUserCode();
                params.setCreator(creator);
            }
            //3.启动流程
            identityService.setAuthenticatedUserId(creator);
            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey(params.getProcessDefinitionKey().trim())
                    .name(params.getFormName().trim())
                    .businessKey(params.getBusinessKey().trim())
                    .variables(params.getVariables())
                    //.tenantId(params.getSystemSn().trim())
                    .start();
            returnVo.setData(processInstance);
            //4.添加审批记录
            this.addComment(params.getCurrentUserCode(), processInstance.getProcessInstanceId(),
                    CommentTypeEnum.TJ.toString(), params.getFormName() + "提交");
            //5.TODO 推送消息数据
        } else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL, "请填写 这三个字段 ProcessDefinitionKey,BusinessKey,SystemSn");
        }
        return returnVo;
    }


    public ReturnVo<ProcessInstance> startProcessInstanceByKeyWithForm(StartProcessInstanceVo params) {
        ReturnVo<ProcessInstance> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "启动成功");
        if (StringUtils.isNotBlank(params.getProcessDefinitionKey())
                && StringUtils.isNotBlank(params.getBusinessKey())
                ) {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(params.getProcessDefinitionKey())
                    .latestVersion().singleResult();
            if (processDefinition != null && processDefinition.isSuspended()) {
                returnVo = new ReturnVo<>(ReturnCode.FAIL, "此流程已经挂起,请联系系统管理员!");
                return returnVo;
            }
            /**
             * 1、设置变量
             * 1.1、设置提交人字段为空字符串让其自动跳过
             * 1.2、设置可以自动跳过
             * 1.3、汇报线的参数设置
             */
            //1.1、设置提交人字段为空字符串让其自动跳过
           // params.getVariables().put(FlowConstant.FLOW_SUBMITTER_VAR, "");
            //1.2、设置可以自动跳过
           // params.getVariables().put(FlowConstant.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);
            // TODO 1.3、汇报线的参数设置
            //2、当我们流程创建人和发起人
            String creator = params.getCreator();
            if (StringUtils.isBlank(creator)) {
                creator = params.getCurrentUserCode();
                params.setCreator(creator);
            }
            //3.启动流程
            identityService.setAuthenticatedUserId(creator);
/*            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey(params.getProcessDefinitionKey().trim())
                    .name(params.getFormName().trim())
                    .businessKey(params.getBusinessKey().trim())
                    .variables(params.getVariables())
                    //.tenantId(params.getSystemSn().trim())
                    .start();*/
           // ProcessInstance processInstance =runtimeService.startProcessInstanceWithForm(processDefinition.getId(), "outcome", params.getVariables(), params.getBusinessKey());

            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(processDefinition.getId())
                    .name(params.getBusinessKey())
                    .businessKey(params.getBusinessKey())
                    .startFormVariables(params.getVariables())
                    .variables(params.getVariables())
                    .start();




            returnVo.setData(processInstance);
            //4.添加审批记录
            this.addComment(params.getCurrentUserCode(), processInstance.getProcessInstanceId(),
                    CommentTypeEnum.TJ.toString(), params.getFormName() + "提交");
            //5.TODO 推送消息数据
        } else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL, "请填写 这三个字段 ProcessDefinitionKey,BusinessKey,SystemSn");
        }
        return returnVo;
    }






    @Override
    public PagerModel<ProcessInstanceVo> getMyProcessInstances(ProcessInstanceQueryVo params, Query query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        if (StringUtils.isNotBlank(params.getUserCode())) {
            Page<ProcessInstanceVo> myProcesses = flowableProcessInstanceDao.getPagerModel(params);
            myProcesses.forEach(processInstanceVo -> {
                this.setStateApprover(processInstanceVo);
            });
            return new PagerModel<>(myProcesses);
        }
        return null;
    }


    public byte[] createImage(String processInstanceId,String imageType) {
        //1.获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = null;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        //2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> historicSquenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        historicSquenceFlows.forEach(historicActivityInstance -> highLightedFlows.add(historicActivityInstance.getActivityId()));
        //3. 获取流程定义id和高亮的节点id
        if (processInstance != null) {
            //3.1. 正在运行的流程实例
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        } else {
            //3.2. 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            //3.3. 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId).activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            List<String> finalActiveActivityIds = activeActivityIds;
            historicEnds.forEach(historicActivityInstance -> finalActiveActivityIds.add(historicActivityInstance.getActivityId()));
        }
        //4. 获取bpmnModel对象
        BpmnModel bpmnModel = flowableBpmnModelService.getBpmnModelByProcessDefId(processDefinitionId);
        //5. 生成图片流
        InputStream inputStream;
        //如流程实例挂起，设置背景色为红色  add by huxipi
        if(null!=processInstance && processInstance.isSuspended()){
            inputStream = flowProcessDiagramGenerator.generateDiagram(bpmnModel, activeActivityIds, highLightedFlows,imageType+"x");
        }else{
            inputStream = flowProcessDiagramGenerator.generateDiagram(bpmnModel, activeActivityIds, highLightedFlows,imageType);
        }

        //6. 转化成byte便于网络传输
        byte[] datas = IoUtil.readInputStream(inputStream, "image inputStream name");
        return datas;
    }


    @Override
    public byte[] createImage(String processInstanceId) {
        //1.获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = null;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        //2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> historicSquenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        historicSquenceFlows.forEach(historicActivityInstance -> highLightedFlows.add(historicActivityInstance.getActivityId()));
        //3. 获取流程定义id和高亮的节点id
        if (processInstance != null) {
            //3.1. 正在运行的流程实例
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        } else {
            //3.2. 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            //3.3. 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId).activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            List<String> finalActiveActivityIds = activeActivityIds;
            historicEnds.forEach(historicActivityInstance -> finalActiveActivityIds.add(historicActivityInstance.getActivityId()));
        }
        //4. 获取bpmnModel对象
        BpmnModel bpmnModel = flowableBpmnModelService.getBpmnModelByProcessDefId(processDefinitionId);
        //5. 生成图片流
        InputStream inputStream= flowProcessDiagramGenerator.generateDiagram(bpmnModel, activeActivityIds, highLightedFlows,"png");

        //6. 转化成byte便于网络传输
        byte[] datas = IoUtil.readInputStream(inputStream, "image inputStream name");
        return datas;
    }

    @Override
    public ReturnVo<String> deleteProcessInstanceById(String processInstanceId) {
        ReturnVo<String> returnVo = null;
        long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count();
        if (count > 0) {
            DeleteFlowableProcessInstanceCmd cmd = new DeleteFlowableProcessInstanceCmd(processInstanceId, "删除流程实例", true);
            managementService.executeCommand(cmd);
            returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "删除成功");
        } else {
            historyService.deleteHistoricProcessInstance(processInstanceId);
            returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "删除成功");
        }
        return returnVo;
    }

    @Override
    public ReturnVo<String> suspendOrActivateProcessInstanceById(String processInstanceId, Integer suspensionState) {
        ReturnVo<String> returnVo = null;
        if (suspensionState == 1) {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "挂起成功");
        } else {
            runtimeService.activateProcessInstanceById(processInstanceId);
            returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "激活成功");
        }
        return returnVo;
    }

    @Override
    public ReturnVo<String> stopProcessInstanceById(EndProcessVo endVo) {
        ReturnVo<String> returnVo = null;
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(endVo.getProcessInstanceId()).singleResult();
        if (processInstance != null) {
            //1、添加审批记录
            this.addComment(endVo.getUserCode(), endVo.getProcessInstanceId(), CommentTypeEnum.LCZZ.toString(),
                    endVo.getMessage());
            List<EndEvent> endNodes = flowableBpmnModelService.findEndFlowElement(processInstance.getProcessDefinitionId());
            String endId = endNodes.get(0).getId();
            String processInstanceId = endVo.getProcessInstanceId();
            //2、执行终止
            List<Execution> executions = runtimeService.createExecutionQuery().parentId(processInstanceId).list();
            List<String> executionIds = new ArrayList<>();
            executions.forEach(execution -> executionIds.add(execution.getId()));
            this.moveExecutionsToSingleActivityId(executionIds, endId);
            returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "终止成功");
        }else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL, "不存在运行的流程实例,请确认!");
        }
        return returnVo;
    }

    @Override
    public ReturnVo<String> revokeProcess(RevokeProcessVo revokeVo) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.FAIL, "撤回失败!");
        if (StringUtils.isNotBlank(revokeVo.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(revokeVo.getProcessInstanceId()).singleResult();
            if (processInstance != null) {
                //1.添加撤回意见
                this.addComment(revokeVo.getUserCode(), revokeVo.getProcessInstanceId(), CommentTypeEnum.CH.toString(), revokeVo.getMessage());
                //2.设置提交人
                runtimeService.setVariable(revokeVo.getProcessInstanceId(), FlowConstant.FLOW_SUBMITTER_VAR, processInstance.getStartUserId());
                runtimeService.setVariable(revokeVo.getProcessInstanceId(),"skip","no");
                //3.执行撤回

/*                HistoricActivityInstanceEntity historicActivityInstance = Context.getCommandContext().getHistoryManager().findActivityInstance(execution);
                if (historicActivityInstance != null) {
                    this.activityInstanceId = historicActivityInstance.getId();
                }*/

                List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(revokeVo.getProcessInstanceId()).activityType("userTask").orderByHistoricActivityInstanceStartTime().asc().list();
                HistoricActivityInstance targetActivity= activityInstances.get(activityInstances.size()-2);

               // Activity disActivity = flowableBpmnModelService.findActivityByName(processInstance.getProcessDefinitionId(), FlowConstant.FLOW_SUBMITTER);
                Activity disActivity = flowableBpmnModelService.findActivityByName(processInstance.getProcessDefinitionId(), targetActivity.getActivityName());
                //4.删除运行和历史的节点信息
                this.deleteActivity(disActivity.getId(), revokeVo.getProcessInstanceId());//???
                //5.执行跳转
                List<Execution> executions = runtimeService.createExecutionQuery().parentId(revokeVo.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                //this.moveExecutionsToSingleActivityId(executionIds, disActivity.getId());
                this.moveExecutionsToSingleActivityId(executionIds, disActivity.getId(),revokeVo);


                returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "撤回成功!");
            }else {
                returnVo = new ReturnVo<>(ReturnCode.FAIL, "流程已结束或流程实例不存在!");
            }
        } else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL, "流程实例不能为空!");
        }
        return returnVo;
    }
}