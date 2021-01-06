package com.cims.bpm.restapi;

import com.cims.bpm.vo.FormInfoQueryVo;
import com.cims.bpm.vo.ProcessInstanceQueryVo;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import com.cims.bpm.vo.EndProcessVo;
import com.cims.bpm.vo.ProcessInstanceVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.form.api.FormInfo;
import org.flowable.form.model.FormField;
import org.flowable.form.model.SimpleFormModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例controller类
 * @author hezh
 */
@Api("流程实例controller类")
@RestController
@RequestMapping("/rest/processInstance")
public class ApiFlowableProcessInstanceResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiFlowableProcessInstanceResource.class);
    @Autowired
    private IFlowableProcessInstanceService flowableProcessInstanceService;


    @Autowired
    private HistoryService historyService;

    /**
     * 分页查询流程实例列表
     *
     * @param params 参数
     * @param query  分页
     * @return
     */
    @ApiOperation(value = "分页查询流程实例列表", notes = "100%")
    @PostMapping(value = "/page-model")
    public PagerModel<ProcessInstanceVo> pageModel(@ApiParam ProcessInstanceQueryVo params, @ApiParam Query query) {
        PagerModel<ProcessInstanceVo> pm = flowableProcessInstanceService.getPagerModel(params, query);
        return pm;
    }

    /**
     * 根据流程实例id删除流程实例
     *
     * @param processInstanceId 流程实例id
     * @return
     */
    @ApiOperation(value = "根据流程实例id删除流程实例", notes = "100%")
    @GetMapping(value = "/deleteProcessInstanceById/{processInstanceId}")
    public ReturnVo<String> deleteProcessInstanceById(@PathVariable @ApiParam String processInstanceId) {
        ReturnVo<String> data = flowableProcessInstanceService.deleteProcessInstanceById(processInstanceId);
        return data;
    }

    /**
     * 设置当前流程实例为指定状态，挂起/暂停/结束==
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "设置当前流程实例为指定状态", notes = "100%")
    @PostMapping(value = "/saProcessInstanceById")
    public ReturnVo<String> saProcessInstanceById(@ApiParam String id, @ApiParam int suspensionState) {
        ReturnVo<String> returnVo = flowableProcessInstanceService.suspendOrActivateProcessInstanceById(id, suspensionState);
        return returnVo;
    }

    /**
     * 终止
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "终止流程", notes = "100%")
    @PostMapping(value = "/stopProcess")
    public ReturnVo<String> stopProcess(@ApiParam EndProcessVo params) {
        boolean flag = this.isSuspended(params.getProcessInstanceId());
        ReturnVo<String> returnVo = null;
        if (flag){
            params.setMessage("后台执行终止");
            params.setUserCode(this.getLoginUser().getId());
            returnVo = flowableProcessInstanceService.stopProcessInstanceById(params);
        }else{
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"流程已挂起，请联系管理员激活!");
        }
        return returnVo;
    }


    /**
     * 查询某个流程实例对应的表单信息
     * @param params
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询某个流程实例对应的表单信息", notes = "")
    @PostMapping(value = "/find-formInfoByInstanceId")
    public ReturnVo<FormField> findFormInfoByByTaskId(@ApiParam FormInfoQueryVo params) throws Exception {
        ReturnVo<FormField> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().
                processInstanceId(params.getProcessInstanceId()).singleResult();
        String processDefinitionId = processInstance.getProcessDefinitionId();

        //查询表单信息
        FormInfo fm = runtimeService.getStartFormModel(processDefinitionId, params.getProcessInstanceId());
        List<FormField> fields = ((SimpleFormModel) fm.getFormModel()).getFields();
        returnVo.setDatas(fields);
        return returnVo;
    }

}