package com.cims.bpm.service.impl;

import com.cims.bpm.service.IFlowableIdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.springframework.stereotype.Service;

@Service
public class FlowableIdentityServiceImpl extends BaseProcessService implements IFlowableIdentityService {

    @Override
    public void saveUser(User user) {
        identityService.saveUser(user);
    }

    @Override
    public void saveGroup(Group group) {
        identityService.saveGroup(group);
    }

}
