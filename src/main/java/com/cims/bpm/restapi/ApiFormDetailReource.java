package com.cims.bpm.restapi;

import com.cims.bpm.service.IFlowableCommentService;
import com.cims.bpm.vo.*;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.vo.ReturnVo;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import com.cims.bpm.service.IFlowableTaskService;
import com.cims.bpm.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 获取流程详情controller类
 * @author hezh
 */
@Api("获取流程详情api接口")
@RestController
@RequestMapping("/rest/formdetail")
public class ApiFormDetailReource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiFormDetailReource.class);
    @Autowired
    private IFlowableCommentService flowableCommentService;
    @Autowired
    private IFlowableTaskService flowableTaskService;
    @Autowired
    private IFlowableProcessInstanceService flowableProcessInstanceService;

    /**
     * 通过流程实例id获取审批意见
     *
     * @param processInstanceId 流程实例id
     * @return
     */
    @ApiOperation(value = "通过流程实例id获取审批意见", notes = "")
    @GetMapping("/commentsByProcessInstanceId")
    public List<CommentVo> commentsByProcessInstanceId(@ApiParam String processInstanceId) {
        List<CommentVo> datas = flowableCommentService.getFlowCommentVosByProcessInstanceId(processInstanceId);
        return datas;
    }

    /**
     * 审批任务
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "完成审批任务", notes = "")
    @PostMapping(value = "/complete")
    public ReturnVo<String> complete(@RequestBody @ApiParam CompleteTaskVo params) {
        boolean flag = this.isSuspended(params.getProcessInstanceId());
        ReturnVo<String> returnVo = null;
        if (flag){
            params.setUserCode(params.getUserCode());
            returnVo = flowableTaskService.complete(params);
        }else{
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"流程已挂起，请联系管理员激活!");
        }
        return returnVo;
    }

    /**
     * 终止
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "结束流程", notes = "")
    @PostMapping(value = "/stopProcess")
    public ReturnVo<String> stopProcess(@ApiParam EndProcessVo params) {
        boolean flag = this.isSuspended(params.getProcessInstanceId());
        ReturnVo<String> returnVo = null;
        if (flag){
            params.setUserCode(this.getLoginUser().getId());
            returnVo = flowableProcessInstanceService.stopProcessInstanceById(params);
        }else{
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"流程已挂起，请联系管理员激活!");
        }
        return returnVo;
    }

    /**
     * 撤回
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "撤回", notes = "")
    @PostMapping(value = "/revokeProcess")
    public ReturnVo<String> revokeProcess(@RequestBody @ApiParam RevokeProcessVo params) {
        //params.setUserCode(this.getLoginUser().getId());
        ReturnVo<String> returnVo = flowableProcessInstanceService.revokeProcess(params);
        return returnVo;
    }
    /**
     * 转办
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "转办", notes = "")
    @PostMapping(value = "/turnTask")
    public ReturnVo<String> turnTask(@RequestBody @ApiParam TurnTaskVo params) {
        ReturnVo<String> returnVo = null;

        if (params.getUserCodes() != null && params.getUserCodes().length > 0) {
            //params.setUserCode(this.getLoginUser().getId());
            params.setTurnToUserId("pm");//把pm的任务交给mp转办
            params.setTurnToUserId(params.getUserCodes()[0]);
            returnVo = flowableTaskService.turnTask(params);
        }else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"请选择人员");
        }
        return returnVo;
    }

    /**
     * 委派
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "委派", notes = "")
    @PostMapping(value = "/delegateTask")
    public ReturnVo<String> delegateTask(@RequestBody @ApiParam DelegateTaskVo params) {
        ReturnVo<String> returnVo = null;
        if (params.getUserCodes()!= null && params.getUserCodes().length > 0) {
            params.setUserCode(params.getUserCode());
            params.setDelegateUserCode(params.getUserCodes()[0]);
            returnVo = flowableTaskService.delegateTask(params);
        }else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"请选择人员");
        }
        return returnVo;
    }

    /**
     * 签收
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "签收", notes = "")
    @PostMapping(value = "/claimTask")
    public ReturnVo<String> claimTask(@RequestBody @ApiParam ClaimTaskVo params) {
        //params.setUserCode(this.getLoginUser().getId());
        ReturnVo<String> returnVo = flowableTaskService.claimTask(params);
        return returnVo;
    }

    /**
     * 反签收
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "反签收", notes = "")
    @PostMapping(value = "/unClaimTask")
    public ReturnVo<String> unClaimTask(@RequestBody @ApiParam ClaimTaskVo params) {
        //params.setUserCode(this.getLoginUser().getId());
        ReturnVo<String> returnVo = flowableTaskService.unClaimTask(params);
        return returnVo;
    }

    /**
     * 向前加签
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "先前加签", notes = "")
    @PostMapping(value = "/beforeAddSignTask")
    public ReturnVo<String> beforeAddSignTask(@RequestBody @ApiParam AddSignTaskVo params) {
        ReturnVo<String> returnVo = null;
        if (params.getUserCodes() != null && params.getUserCodes().length > 0) {
           // params.setUserCode(this.getLoginUser().getId());
            params.setSignPersoneds(Arrays.asList(params.getUserCodes()));
            returnVo = flowableTaskService.beforeAddSignTask(params);
        }else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"请选择人员");
        }
        return returnVo;
    }

    /**
     * 向后加签
     *
     * @param params 参数
     * @return
     */
    @ApiOperation(value = "先后加签", notes = "")
    @PostMapping(value = "/afterAddSignTask")
    public ReturnVo<String> afterAddSignTask(@RequestBody @ApiParam AddSignTaskVo params) {
        ReturnVo<String> returnVo = null;
        if (params.getUserCodes() != null && params.getUserCodes().length > 0) {
        //    params.setUserCode(this.getLoginUser().getId());
            params.setSignPersoneds(Arrays.asList(params.getUserCodes()));
            returnVo = flowableTaskService.afterAddSignTask(params);
        }else {
            returnVo = new ReturnVo<>(ReturnCode.FAIL,"请选择人员");
        }
        return returnVo;
    }



    /**
     * 获取实例图片
     * @param processInstanceId 流程实例id
     * @return
     */
    @ApiOperation(value = "获取实例图片", notes = "")
    @GetMapping(value = "/image/{processInstanceId}/{imageType}")
    public void image(@PathVariable @ApiParam String processInstanceId, HttpServletResponse response,@PathVariable String imageType) {
        try {
            byte[] b = flowableProcessInstanceService.createImage(processInstanceId,imageType);
           // response.setHeader("Content-type", "image/png;charset=UTF-8");
           // response.setHeader("Content-type", "image/gif;charset=UTF-8");
            response.setHeader("Content-type","image/"+imageType+";charset=UTF-8");
            response.getOutputStream().write(b);
        } catch (Exception e) {
            LOGGER.error("ApiFormDetailReource-image:" + e);
            e.printStackTrace();
        }
    }

    /**
     * 获取可驳回节点列表
     * @param processInstanceId 流程实例id
     * @return
     */
    @ApiOperation(value = "获取可驳回节点列表", notes = "")
    @GetMapping(value = "/getBackNodesByProcessInstanceId/{processInstanceId}/{taskId}")
    public ReturnVo<FlowNodeVo> getBackNodesByProcessInstanceId(@PathVariable @ApiParam String processInstanceId,
                                                                @PathVariable @ApiParam String taskId) {
        List<FlowNodeVo> datas = flowableTaskService.getBackNodesByProcessInstanceId(processInstanceId,taskId);
        ReturnVo<FlowNodeVo> returnVo = new ReturnVo<>(ReturnCode.SUCCESS,"查询返回节点成功");
        returnVo.setDatas(datas);
        return returnVo;

    }

    /**
     * 驳回节点
      * @param params 参数
     * @return
     */
    @ApiOperation(value = "驳回节点", notes = "")
    @PostMapping(value = "/doBackStep")
    public ReturnVo<String> doBackStep(@RequestBody @ApiParam BackTaskVo params) {
       // params.setUserCode(this.getLoginUser().getId());
        ReturnVo<String> returnVo = flowableTaskService.backToStepTask(params);
        return returnVo;
    }
}