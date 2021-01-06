package com.cims.bpm.interceptor;

import org.flowable.common.engine.impl.interceptor.AbstractCommandInterceptor;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.engine.impl.cmd.CompleteTaskCmd;

public class FlowablePostCommandInterceptor extends AbstractCommandInterceptor {

    @Override
    public <T> T execute(CommandConfig commandConfig, Command<T> command) {
        if (command instanceof CompleteTaskCmd){
            CompleteTaskCmd cmd = (CompleteTaskCmd)command;
        }
        return next.execute(commandConfig, command);
    }
}
