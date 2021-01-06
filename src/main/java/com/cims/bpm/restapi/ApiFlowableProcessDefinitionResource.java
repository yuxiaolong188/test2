package com.cims.bpm.restapi;

import com.cims.bpm.support.MultiRequestBody;
import com.cims.bpm.vo.ProcessDefinitionQueryVo;
import com.cims.bpm.vo.ProcessDefinitionVo;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.dragon.tools.vo.ReturnVo;
import com.cims.bpm.service.IFlowableProcessDefinitionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.flowable.common.engine.impl.util.IoUtil;
import org.flowable.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * 流程定义controller类
 * @author hezh
 */
@Api("流程定义api")
@RestController
@RequestMapping("/rest/definition")
public class ApiFlowableProcessDefinitionResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiFlowableProcessDefinitionResource.class);
    @Autowired
    private IFlowableProcessDefinitionService flowableProcessDefinitionService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * 分页查询流程定义列表
     *
     * @param params 参数
     * @param query  分页
     * @return
     */
   // @PreAuthorize("hasAuthority('access-admin')")
    @ApiOperation(value = "分页查询流程定义列表", notes = "100%")
    @PostMapping(value = "/page-model")
    public PagerModel<ProcessDefinitionVo> pageModel(@MultiRequestBody("processDefinitionQuery") @ApiParam ProcessDefinitionQueryVo params,
                                                     @ApiParam @MultiRequestBody Query query) {
        PagerModel<ProcessDefinitionVo> pm = flowableProcessDefinitionService.getPagerModel(params, query);
        return pm;
    }

    /**
     * 删除流程定义
     *
     * @param deploymentId 部署id
     */
    @ApiOperation(value = "删除定义的流程", notes = "100%")
    @PostMapping(value = "/deleteDeployment")
    public ReturnVo<String> deleteDeployment(@ApiParam String deploymentId) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        repositoryService.deleteDeployment(deploymentId, true);
        return returnVo;
    }

    /**
     * 通过id和类型获取图片
     *
     * @param id       定义id
     * @param type     类型
     * @param response response
     */
    @ApiOperation(value = "通过id和类型获取图片", notes = "100%")
    @GetMapping(value = "/processFile/{type}/{id}")
    public void processFile(@PathVariable @ApiParam String id, @PathVariable @ApiParam String type, HttpServletResponse response) {
        try {
            byte[] b = null;
            ProcessDefinitionVo pd = flowableProcessDefinitionService.getById(id);
            if (pd != null) {
                if (type.equals("xml")) {
                    response.setHeader("Content-type", "text/xml;charset=UTF-8");
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
                    b = IoUtil.readInputStream(inputStream, "image inputStream name");
                } else {
                    response.setHeader("Content-Type", "image/png");
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDgrmResourceName());
                    b = IoUtil.readInputStream(inputStream, "image inputStream name");
                }
                response.getOutputStream().write(b);
            }
        } catch (Exception e) {
            LOGGER.error("ApiFlowableModelResource-loadXmlByModelId:" + e);
            e.printStackTrace();
        }
    }

    /**
     * 激活或者挂起流程定义
     * @param id
     * @return
     */
    @ApiOperation(value = "激活或者挂起流程定义", notes = "100%")
    @PostMapping(value = "/saDefinitionById")
    public ReturnVo<String> saDefinitionById(@ApiParam String id, @ApiParam int suspensionState) {
        ReturnVo<String> returnVo = returnVo = flowableProcessDefinitionService.suspendOrActivateProcessDefinitionById(id,suspensionState);
        return returnVo;
    }
}
