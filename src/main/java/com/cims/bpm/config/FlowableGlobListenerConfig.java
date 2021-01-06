package com.cims.bpm.config;

import com.cims.bpm.listener.AutoCompleteFirstTaskListener;
import com.cims.bpm.listener.GlobalTaskCreateListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class FlowableGlobListenerConfig implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private SpringProcessEngineConfiguration configuration;
    @Autowired
    private GlobalTaskCreateListener globalTaskCreateListener;

    @Autowired
    private AutoCompleteFirstTaskListener addEventListener;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        FlowableEventDispatcher dispatcher = configuration.getEventDispatcher();
        //任务创建全局监听
        dispatcher.addEventListener(globalTaskCreateListener, FlowableEngineEventType.TASK_CREATED);
        dispatcher.addEventListener(addEventListener);







    }
}