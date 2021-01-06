import com.cims.bpm.SpringBootApplication;
import com.cims.bpm.business.leave.LeaveService;
import com.cims.bpm.service.IFlowableProcessInstanceService;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApplication.class)
public class ProcessInstanceTest {

    @Autowired
    private IFlowableProcessInstanceService flowableProcessInstanceService;
    @Autowired
    private LeaveService leaveService;

    @Autowired
    private ModelService modelService;


    @Autowired
    FormRepositoryService formRepositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    FormService formService;

    @Autowired
    HistoryService historyService;

    @Autowired
    TaskService taskService;

    @Autowired
    IdentityService identityService;


    @Test
    public void formTest() {
        //部署表单
        Model model = modelService.getModel("717e1171-8ae6-11ea-97ce-0036761e2c53");
        String form_json = model.getModelEditorJson();
        formRepositoryService.createDeployment().parentDeploymentId("abb100508ae111ea97ce0036761e2c53")
                .name("请假表单")
                .addFormDefinition("qjbd_rs", form_json)
                .deploy();
    }

    @Test
    public void formTestStart() {
        //流程实例的启动

        Map<String, Object> formProperties;
        formProperties = new HashMap<>();
        formProperties.put("qjts", "360天");




        identityService.setAuthenticatedUserId("2756");

        runtimeService.startProcessInstanceWithForm("demo:22:b2b63c6a8c2411ea83b60036761e2c53", "5s51", formProperties, "ssd16");



        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId("demo:10:93ce04268c1711ea83b60036761e2c53")
                .name("11111222222")
                .businessKey("333333333333333222")
                .variables(formProperties)
                .start();


      //  runtimeService.createProcessInstanceBuilder().startFormVariables()


/*        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId("demo:10:93ce04268c1711ea83b60036761e2c53")
                .name("11111222222")
                .businessKey("333333333333333222")
                .startFormVariables(formProperties)
                .start();*/




        /*
            启动方式一：  这种可以设置模型自动跳过首节点，但是这种启动无法查询表单值

                    // formProperties.put(FlowConstant.FLOW_SUBMITTER_VAR, "");
        //1.2、设置可以自动跳过
        // formProperties.put(FlowConstant.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);


                Map<String, Object> params=new HashMap<>();
        params.put(FlowConstant.FLOW_SUBMITTER_VAR, "");
        //1.2、设置可以自动跳过
        params.put(FlowConstant.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);
        params.put("qjts", "361天");

        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId("demo:10:93ce04268c1711ea83b60036761e2c53")
                .name("11111222222")
                .businessKey("333333333333333222")
                .variables(formProperties)
                .start();

         runtimeService.startProcessInstanceWithForm方法启动带表单的流程，
         runtimeService.getStartFormModel查询流程启动时的表单信息;
         taskService.completeTaskWithForm填充表单完成任务，
         taskService.getTaskFormModel   查询任务表单信息


         */












/*












        //查询表单信息
        FormInfo fm = runtimeService.getStartFormModel("demo:22:b2b63c6a8c2411ea83b60036761e2c53", "372472318c2711ea848a0036761e2c53");
        System.out.println(fm.getId());
        System.out.println(fm.getKey());
        System.out.println(fm.getName());
//        System.out.println(fm.get);
        System.err.println(fm.getVersion());
        List<FormField> fields =((SimpleFormModel) fm.getFormModel()).getFields();
        for (FormField ff : fields) {
            System.out.println("######################");
            System.out.println(ff.getId());
            System.out.println(ff.getName());
            System.out.println(ff.getType());
            System.out.println(ff.getPlaceholder());
            System.out.println(ff.getValue());
            System.out.println("######################");

    }




        //  StartFormData form = formService.getStartFormData("demo:10:93ce04268c1711ea83b60036761e2c53");

        //  formService.getStartFormData()


/*

        FormInfo fi = runtimeService.getStartFormModel("demo:10:93ce04268c1711ea83b60036761e2c53", "ad4878308c1911eaad760036761e2c53");

        List<FormField> fields = ((SimpleFormModel)fi.getFormModel()).getFields();

        for (FormField ff : fields) {

            System.out.println("######################");

            System.out.println(ff.getId());

            System.out.println(ff.getName());

            System.out.println(ff.getType());

            System.out.println(ff.getPlaceholder());

            System.out.println(ff.getValue());

            System.out.println("######################");

        }
*/











/*


    @Test
    public void testStartProcess() throws Exception{
        Leave leave = new Leave();
        String leaveId = UUIDGenerator.generate();
        leave.setId(leaveId);
        leave.setDays(2);
        leave.setName("huxipi");
        Date date = new Date();
        leave.setStartTime(date);
        leave.setEndTime(DateUtil.addDate(date,1));
        StartProcessInstanceVo startProcessInstanceVo = new StartProcessInstanceVo();
        startProcessInstanceVo.setBusinessKey(leaveId);
        startProcessInstanceVo.setCreator("00000001");
        startProcessInstanceVo.setCurrentUserCode("00000001");
        startProcessInstanceVo.setFormName("请假流程");
        startProcessInstanceVo.setSystemSn("flow");
        startProcessInstanceVo.setProcessDefinitionKey("qingjia");
        Map<String,Object> variables = new HashMap<>();
        variables.put("days",leave.getDays());
        startProcessInstanceVo.setVariables(variables);
        ReturnVo<ProcessInstance> returnStart = flowableProcessInstanceService.startProcessInstanceByKey(startProcessInstanceVo);
        String processInstanceId = returnStart.getData().getProcessInstanceId();
        leave.setProcessInstanceId(processInstanceId);
        this.leaveService.insertLeave(leave);
    }
    @Test
    public void testRevokeProcess() {
        RevokeProcessVo revokeVo = new RevokeProcessVo();
        revokeVo.setProcessInstanceId("021d89c116a011ea89b4dc8b287b3603");
        flowableProcessInstanceService.revokeProcess(revokeVo);
    }

    */

    }
}