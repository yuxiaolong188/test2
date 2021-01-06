package com.cims.bpm.listener;

import org.flowable.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hezh
 */
@Scope
@Component(value = "taskBusinessCallListener")
public class TaskBusinessCallListener extends BusinessCallListener implements TaskListener {
    /**
     * rest接口
     */
    private FixedValue restUrl;
    /**
     * 参数 多个的话用分号隔开 实例 userCode:00004737;status:1
     */
    private FixedValue params;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String restUrlStr = null, paramsStr = null;
        if (restUrl != null) {
            restUrlStr = restUrl.getExpressionText();
        }
        if (params != null) {
            paramsStr = params.getExpressionText();
        }
        //执行回调
        //TODO 临时处理
        restUrlStr = "http://127.0.0.1:8989/rest/leave/updateLeaveStatus";
        paramsStr = "status:1";
        this.callBack(processInstanceId, restUrlStr, paramsStr);
    }
}
