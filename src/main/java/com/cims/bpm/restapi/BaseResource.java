package com.cims.bpm.restapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础类接口
 * @author hezh
 */
public class BaseResource {

    @Autowired
    public  RuntimeService runtimeService;

    @Autowired
    public IdentityService identityService;

    @Autowired
    public RepositoryService repositoryService;

    @Autowired
    public ModelService modelService;

    public User getLoginUser() {
        User user = SecurityUtils.getCurrentUserObject();
        return user;
    }

    /**
     * 判断是否挂起状态
     * @param processInstanceId 流程实例id
     * @return
     */
    public boolean isSuspended(String processInstanceId) {
        boolean flag = true;
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance != null){
            flag = !processInstance.isSuspended();
        }
        return flag;
    }
}