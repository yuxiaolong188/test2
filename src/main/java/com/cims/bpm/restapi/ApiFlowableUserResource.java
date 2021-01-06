package com.cims.bpm.restapi;

import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import com.cims.bpm.service.IFlowableIdentityService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.idm.api.Privilege;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * flowable 用户相关接口
 * @author hezh
 */
@RestController
@RequestMapping("/rest/user")
public class ApiFlowableUserResource extends BaseResource {

    @Autowired
    private IdmIdentityService idmIdentityService;
    @Autowired
    private IFlowableIdentityService flowableIdentityService;

    /**
     * 查询用户列表
     *
     * @param name 姓名
     * @return
     */
    @ApiOperation(value = "查询用户列表", notes = "")
    @GetMapping("/getPagerModel")
    public PagerModel<User> getPagerModel(@ApiParam String name, @ApiParam Query query) {
        UserQuery userQuery = idmIdentityService.createUserQuery();
        if (StringUtils.isNotBlank(name)){
            userQuery.userFirstNameLike(name);
        }
        long count = userQuery.count();

        int firstResult = (query.getPageNum() - 1) * query.getPageSize();
        List<User> datas = userQuery.orderByUserFirstName().desc().listPage(firstResult, query.getPageSize());
        return new PagerModel<>(count, datas);
    }

    /**
     * 添加修改用户
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "添加修改用户", notes = "")
    @PostMapping("/save")
    public ReturnVo<String> save(@ApiParam UserEntityImpl user) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "添加成功");
        long count = idmIdentityService.createUserQuery().userId(user.getId()).count();
        flowableIdentityService.saveUser(user);
        if (count == 0) {
            Privilege privilege = idmIdentityService.createPrivilegeQuery().privilegeName("access-idm").singleResult();
            idmIdentityService.addUserPrivilegeMapping(privilege.getId(), user.getId());
        }
        return returnVo;
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @ApiOperation(value = "删除用户", notes = "")
    @PostMapping("/delete")
    public ReturnVo<String> delete(@ApiParam String userId) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "删除成功");
        idmIdentityService.deleteUser(userId);
        return returnVo;
    }

    /**
     * 添加用户组
     *
     * @param userId   用户id
     * @param groupIds 组ids
     * @return
     */
    @ApiOperation(value = "添加用户组", notes = "")
    @PostMapping("/addUserGroup")
    public ReturnVo<String> addUserGroup(@ApiParam String userId, @ApiParam List<String> groupIds) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "删除成功");
        if (CollectionUtils.isNotEmpty(groupIds)) {
            groupIds.forEach(groupId -> idmIdentityService.createMembership(userId, groupId));
        }
        return returnVo;
    }
}
