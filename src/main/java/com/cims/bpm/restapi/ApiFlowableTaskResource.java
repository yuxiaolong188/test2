package com.cims.bpm.restapi;

import com.cims.bpm.business.leave.Leave;
import com.cims.bpm.business.leave.LeaveService;
import com.cims.bpm.business.leave.aleave.ITblFlowLeaveService;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import com.cims.bpm.service.IFlowableTaskService;
import com.cims.bpm.vo.*;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.flowable.engine.FormService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * flowable 任务controller类
 * @author hezh
 */
@RestController
@RequestMapping("/rest/task")
public class ApiFlowableTaskResource extends BaseResource {

    @Autowired
    private IFlowableTaskService flowableTaskService;
    @Autowired
    private IFlowableProcessInstanceService flowableProcessInstanceService;
    @Autowired
    private LeaveService leaveService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private FormService formService;


    /**
     * 获取待办任务列表
     *
     * @param params 参数
     * @param query  查询条件
     * @return
     */
    @ApiOperation(value = "获取代办任务列表", notes = "")
    @GetMapping(value = "/get-applying-tasks")
    public PagerModel<TaskVo> getApplyingTasks(@ApiParam TaskQueryVo params, @ApiParam Query query) {
      //  params.setUserCode(this.getLoginUser().getId());
        PagerModel<TaskVo> pm = flowableTaskService.getApplyingTasks(params, query);
        return pm;
    }

    /**
     * 获取已办任务列表
     *
     * @param params 参数
     * @param query  查询条件
     * @return
     */
    @ApiOperation(value = "获取已办任务列表", notes = "")
    @GetMapping(value = "/get-applyed-tasks")
    public PagerModel<TaskVo> getApplyedTasks(@ApiParam TaskQueryVo params, @ApiParam Query query) {
       // params.setUserCode(this.getLoginUser().getId());
        PagerModel<TaskVo> pm = flowableTaskService.getApplyedTasks(params, query);
        return pm;
    }

    /**
     * 获取我发起的流程
     *
     * @param params 参数
     * @param query  查询条件
     * @return
     */
    @ApiOperation(value = "获取我发起的流程", notes = "")
    @GetMapping(value = "/my-processInstances")
    public PagerModel<ProcessInstanceVo> myProcessInstances(@ApiParam ProcessInstanceQueryVo params, @ApiParam Query query) {
        //params.setUserCode(this.getLoginUser().getId());
        PagerModel<ProcessInstanceVo> pm = flowableProcessInstanceService.getMyProcessInstances(params, query);
        return pm;
    }



    /**
     * 查询表单详情
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "查询表单详情", notes = "")
    @PostMapping(value = "/find-formInfo")
    public ReturnVo<FormInfoVo> findFormInfoByFormInfoQueryVo(@RequestBody @ApiParam FormInfoQueryVo params) throws Exception{
        ReturnVo<FormInfoVo> returnVo = new ReturnVo<>(ReturnCode.SUCCESS,"OK");
        FormInfoVo<Leave> formInfoVo = new FormInfoVo(params.getTaskId(),params.getProcessInstanceId());
        String processInstanceId = params.getProcessInstanceId();
        String businessKey = null;
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            businessKey = historicProcessInstance.getBusinessKey();
        }else {
            businessKey = processInstance.getBusinessKey();
        }

        if(null==params.getBusinessName()){
            formInfoVo.setBusinessKey(businessKey);
        }else{
            if("leave".equals(params.getBusinessName())){
                Leave leave = leaveService.getLeaveById(businessKey);

                formInfoVo.setFormInfo(leave);
            }
        }
        returnVo.setData(formInfoVo);
        return returnVo;
    }


    /**
     * 通过taskId查询对应表单信息
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/find-formInfoByTaskId")
    public ReturnVo<FormProperty> findFormInfoByByTaskId(FormInfoQueryVo params) throws Exception{
        ReturnVo<FormProperty> returnVo = new ReturnVo<>(ReturnCode.SUCCESS,"OK");
        //查询表单信息
        TaskFormData taskFormData = formService.getTaskFormData(params.getTaskId());
        returnVo.setDatas(taskFormData.getFormProperties());
        return returnVo;

    }













}