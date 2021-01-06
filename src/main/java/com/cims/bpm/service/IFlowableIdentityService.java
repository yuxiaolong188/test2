package com.cims.bpm.service;

import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;

public interface IFlowableIdentityService {
    /**
     * 添加用户
      * @param user
     */
    public void saveUser(User user) ;

    /**
     * 添加组
     * @param group
     */
    public void saveGroup(Group group) ;



}