package com.cims.bpm.config;

import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.job.service.SpringAsyncExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");

        engineConfiguration.setXmlEncoding("UTF-8");
        //设置自定义的uuid生成策略
        engineConfiguration.setIdGenerator(uuidGenerator());
        //启用任务关系计数
        engineConfiguration.setEnableTaskRelationshipCounts(true);

        //启动同步功能 一定要启动否则报错
        engineConfiguration.setAsyncExecutor(springAsyncExecutor());
        engineConfiguration.setAsyncExecutorActivate(true);//异步执行器启用,測試定時


       // engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE);
      //-------  engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
       // engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);

        engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }

    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public SpringAsyncExecutor springAsyncExecutor() {
        SpringAsyncExecutor springAsyncExecutor = new SpringAsyncExecutor();
        springAsyncExecutor.setTaskExecutor(processTaskExecutor());
        springAsyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(1000*500);
        springAsyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(1000*500);
        return springAsyncExecutor;
    }

    @Bean
    public TaskExecutor processTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}