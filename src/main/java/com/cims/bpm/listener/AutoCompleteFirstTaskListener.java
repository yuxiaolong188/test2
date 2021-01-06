package com.cims.bpm.listener;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.context.Context;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hezh
 */
@Component
public class AutoCompleteFirstTaskListener implements FlowableEventListener {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void onEvent(FlowableEvent event) {
        if (!(event instanceof FlowableEntityEventImpl)) {
            return;
        }

        FlowableEntityEventImpl entityEvent = (FlowableEntityEventImpl) event;

        Object entity = entityEvent.getEntity();

        //是否是任务实体类
        if (!(entity instanceof TaskEntity)) {
            return;
        }

        TaskEntity taskEntity = (TaskEntity) entity;

        //是否是在任务节点创建时
        if (FlowableEngineEventType.TASK_CREATED.equals(event.getType())) {
            //找到流程第一个userTask节点
            FlowElement firstElement = this.findFirstFlowElement(taskEntity);

            //对比节点是否相同,因为有可能有子流程的节点进来
            if (firstElement != null && taskEntity.getTaskDefinitionKey().equals(firstElement.getId())) {

                if(null==((TaskEntity) entity).getVariable("skip") || "yes".equals(((TaskEntity) entity).getVariable("skip"))){
                    //测试多实例
                    List<String> assigneeList = Arrays.asList("pm1","pm2","pm3");
                    Map<String, Object> variables =new HashMap<String, Object>();
                     variables.put("assigneeList", assigneeList);
                    Context.getProcessEngineConfiguration().getTaskService().complete(taskEntity.getId(),variables);
                }else{
                    //不自动跳转
                }


            }
        }

    }

    /**
     * 查找流程第一个userTask
     */
    private FlowElement findFirstFlowElement(TaskEntity taskEntity) {
        //RepositoryService repositoryService = SpringContextListener.getBean(RepositoryService.class);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(taskEntity.getProcessDefinitionId());
        for (FlowElement flowElement : bpmnModel.getProcesses().get(0).getFlowElements()) {
            if (flowElement instanceof StartEvent) {
                return bpmnModel.getFlowElement(((StartEvent) flowElement).getOutgoingFlows().get(0).getTargetRef());
            }
        }
        return null;
    }


    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

}