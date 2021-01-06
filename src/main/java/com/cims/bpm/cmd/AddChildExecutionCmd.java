package com.cims.bpm.cmd;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;

public class AddChildExecutionCmd implements Command<String> {

    private ExecutionEntity pexecution;

    public AddChildExecutionCmd(ExecutionEntity pexecution) {
        this.pexecution = pexecution;
    }

    @Override
    public String execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        ExecutionEntity childExecution = executionEntityManager.createChildExecution(pexecution);
        return childExecution.getId();
    }
}
