package com.cims.bpm.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

/**
 * @author hezh
 */
@Component
@Slf4j
public class GlobalTaskCreateListener extends AbstractFlowableEngineEventListener {


    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        if (event instanceof FlowableEntityEventImpl){
            //得到流程定义id
            String processDefinitionId = event.getProcessDefinitionId();
            //得到流程实例id
            String processInstanceId = event.getProcessInstanceId();
            FlowableEntityEventImpl eventImpl = (FlowableEntityEventImpl) event;
            //得到任务实例
            TaskEntity entity = (TaskEntity)eventImpl.getEntity();
            //1、授权

            //2、相邻节点自动跳过

            //3、发送消息
        }

    }

}