package com.cims.bpm.restapi;

import com.cims.bpm.security.NoRepeatSubmit;
import com.cims.bpm.service.IFlowableIdentityService;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.vo.ReturnVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.idm.api.*;
import org.flowable.idm.engine.impl.persistence.entity.GroupEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dragon.tools.pager.Query;

import java.util.List;

/**
 * 用户相关controller
 * @author hezh
 */
@RestController
@RequestMapping("/rest/group")
@Api("用户相关的api")
public class ApiFlowableGroupResource extends BaseResource {

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
    @ApiOperation(value = "分页查询用户组", notes = "获得用户组信息")
    @GetMapping("/getPagerModel")
    public PagerModel<Group> getPagerModel(@ApiParam String name, @ApiParam Query query) {
        GroupQuery groupQuery = idmIdentityService.createGroupQuery().groupNameLike(name);
        long count = groupQuery.count();
        int firstResult = (query.getPageNum() - 1) * query.getPageSize();
        List<Group> datas = groupQuery.orderByGroupName().listPage(firstResult, query.getPageSize());
        return new PagerModel<>(count, datas);
    }

    @ApiOperation(value = "分页查询用户", notes = "获得用户信息")
    @GetMapping("/getAll")
    @NoRepeatSubmit
    public PagerModel<User> getPagerModel(@ApiParam Query query) {
        UserQuery userQuery = idmIdentityService.createUserQuery();
        long count = userQuery.count();
        int firstResult = (query.getPageNum() - 1) * query.getPageSize();
        List<User> datas = userQuery.orderByUserId().asc().listPage(firstResult, query.getPageSize());
        return new PagerModel<User>(count, datas);
    }

    /**
     * 添加修改组
     * @param group  组
     * @return
     */
    @ApiOperation(value = "保存用户", notes = "修改或新增保存")
    @PostMapping("/save")
    public ReturnVo<String> save(@ApiParam GroupEntityImpl group) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "添加成功");
        flowableIdentityService.saveGroup(group);
        return returnVo;
    }

    /**
     * 删除组
     * @param groupId
     * @return
     */
    @ApiOperation(value = "删除用户组", notes = "删除用户组100%")
    @PostMapping("/delete")
    public ReturnVo<String> delete(@ApiParam String groupId) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "删除成功");
        idmIdentityService.deleteGroup(groupId);
        return returnVo;
    }

    /**
     * 添加组成员
     * @param groupId 组的id
     * @param userIds 用户的ids
     * @return
     */
    @ApiOperation(value = "添加用户组成员", notes = "100%")
    @PostMapping("/addGroupUser")
    public ReturnVo<String> addUserGroup(@ApiParam String groupId, @ApiParam List<String> userIds) {
        ReturnVo returnVo = new ReturnVo(ReturnCode.SUCCESS, "删除成功");
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.forEach(userId -> {
                idmIdentityService.createMembership(userId, groupId);
            });
        }
        return returnVo;
    }
}