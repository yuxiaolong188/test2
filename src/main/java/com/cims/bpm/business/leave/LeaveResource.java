package com.cims.bpm.business.leave;

import com.cims.bpm.restapi.BaseResource;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import com.cims.bpm.service.impl.FlowableProcessInstanceServiceImpl;
import com.cims.bpm.vo.StartProcessInstanceVo;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.common.UUIDGenerator;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rest/leave")
public class LeaveResource extends BaseResource {
    private static Logger logger = LoggerFactory.getLogger(LeaveResource.class);

    private final String nameSpace = "leave";


    @Autowired
    private LeaveService leaveService;

    @Autowired
    private FlowableProcessInstanceServiceImpl flowableProcessInstanceService;

    @GetMapping("/list")
    public PagerModel<Leave> list(Leave Leave, Query query, String sessionId) {
        PagerModel<Leave> pm = null;
        try {
            pm = this.leaveService.getPagerModelByQuery(Leave, query);
        } catch (Exception e) {
            logger.error("LeaveController-ajaxList:", e);
            e.printStackTrace();
        }
        return pm;
    }

    //添加
    @PostMapping("/add")
    public ReturnVo add(@RequestBody Leave leave) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.FAIL, "添加失败");
        String userCode=leave.getCreator();

        //todo 业务逻辑检查 add
        if(null==userCode){
             returnVo = new ReturnVo(ReturnCode.FAIL, "用户工号为空");
             return returnVo;
        }




        try {
            String leaveId = UUIDGenerator.generate();
            leave.setId(leaveId);
            StartProcessInstanceVo startProcessInstanceVo = new StartProcessInstanceVo();
            startProcessInstanceVo.setBusinessKey(leaveId);
            //User user = SecurityUtils.getCurrentUserObject();
            startProcessInstanceVo.setCreator(userCode);
            startProcessInstanceVo.setCurrentUserCode(userCode);
            startProcessInstanceVo.setFormName("请假流程");
            //startProcessInstanceVo.setSystemSn("flow");
            startProcessInstanceVo.setProcessDefinitionKey("leave");
/*            Map<String, Object> variables = new HashMap<>();
            variables.put("days", leave.getDays());
            startProcessInstanceVo.setVariables(variables);*/
/*            //设置三个人作为多实例的人员
            List<String> userList = new ArrayList<>();
            userList.add("00000005");
            userList.add("00000006");
            variables.put("userList", userList);*/

            Map<String, Object> formProperties;
            formProperties = new HashMap<>();
            formProperties.put("businesskey", leaveId);//业务key放入启动表单，便于后续查询
            formProperties.put("userCode",userCode);//用户工号放入启动表单，便于后续查询
            startProcessInstanceVo.setVariables(formProperties);

            //identityService.setAuthenticatedUserId(userCode);

            //ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("demo")
            //        .latestVersion().singleResult();


            //String processDefinitionId, String outcome, Map<String, Object> variables, String processInstanceName


           // ProcessInstance returnStart = runtimeService.startProcessInstanceWithForm(processDefinition.getId(), "outcome", formProperties, leaveId);
            ReturnVo<ProcessInstance> returnStart = flowableProcessInstanceService.startProcessInstanceByKeyWithForm(startProcessInstanceVo);
            if (returnStart.getCode().equals(ReturnCode.SUCCESS)) {
                String processInstanceId = returnStart.getData().getProcessInstanceId();
                leave.setProcessInstanceId(processInstanceId);
                leave.setCreator(userCode);
                this.leaveService.insertLeave(leave);
                returnVo = new ReturnVo(ReturnCode.SUCCESS, "添加成功");
            } else {
                returnVo = new ReturnVo(returnStart.getCode(), returnStart.getMsg());
            }
        } catch (Exception e) {
            logger.error("LeaveController-add:", e);
            e.printStackTrace();
        }
        return returnVo;
    }

    //修改
    @PostMapping("/update")
    public ReturnVo update(Leave Leave, String sessionId) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.FAIL, "修改失败");
        try {
            this.leaveService.updateLeave(Leave);
            returnVo = new ReturnVo(ReturnCode.SUCCESS, "修改成功");
        } catch (Exception e) {
            logger.error("LeaveController-update:", e);
            e.printStackTrace();
        }
        return returnVo;
    }

    @PostMapping("/updateLeaveStatus")
    public void updateLeaveStatus(@RequestBody String json) {
        logger.error("修改状态" + json);
    }











}
