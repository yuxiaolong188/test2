package com.cims.bpm.restapi;

import com.cims.bpm.security.advice.annotation.Encryptable;
import com.cims.bpm.vo.ModelVo;
import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.vo.ReturnVo;
import com.cims.bpm.service.FlowProcessDiagramGenerator;
import com.cims.bpm.service.IFlowableModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.domain.ModelRelation;
import org.flowable.ui.modeler.domain.ModelRelationTypes;
import org.flowable.ui.modeler.repository.ModelRelationRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * 上传xml形式的流程到系统
 * @author hezh
 */
@Api("上传xml形式的流程到系统")
@RestController
@RequestMapping("/rest/model")
public class ApiFlowableModelResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiFlowableModelResource.class);
    @Autowired
    private IFlowableModelService flowableModelService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FlowProcessDiagramGenerator flowProcessDiagramGenerator;

    @Autowired
    private IdentityService identityService;

    @Autowired
    ModelRelationRepository modelRelationRepository;

    @Autowired
    FormRepositoryService formRepositoryService;


    @ApiOperation("分页查询bpmn流程定义")
    @GetMapping(value = "/page-model")
    public ReturnVo<PagerModel<AbstractModel>> pageModel() {
        ReturnVo<PagerModel<AbstractModel>> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        List<AbstractModel> datas = modelService.getModelsByModelType(AbstractModel.MODEL_TYPE_BPMN);
        PagerModel<AbstractModel> pm = new PagerModel<>(datas.size(), datas);
       /* pm.getData().forEach(abstractModel -> {
            User user = identityService.createUserQuery().userId(abstractModel.getCreatedBy()).singleResult();
            abstractModel.setCreatedBy(user.getFirstName());
        });*/
        returnVo.setData(pm);
        return returnVo;
    }

    @ApiOperation(value = "添加bpmn流程定义", tags = "100%")
    @PostMapping(value = "/addModel")
    public ReturnVo<String> addModel(@RequestBody @ApiParam ModelVo params) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        try {
            flowableModelService.addModel(params);
        }catch (BadRequestException e){
            returnVo = new ReturnVo<>(ReturnCode.FAIL, e.getMessage());
        }

        return returnVo;
    }

    @ApiOperation(value = "导入流程模板")
    @PostMapping(value = "/import-process-model")
    public ReturnVo<String> importProcessModel(@RequestParam("file") @ApiParam MultipartFile file) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        try {
            flowableModelService.importProcessModel(file);
        }catch (BadRequestException e){
            returnVo = new ReturnVo<>(ReturnCode.FAIL, e.getMessage());
        }
        return returnVo;
    }

    @ApiOperation(value = "部署")
    @PostMapping(value = "/deploy")
    public ReturnVo<String> deploy(@RequestParam("modelId") @ApiParam String modelId) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.FAIL, "部署流程失败！");
        if (StringUtils.isBlank(modelId)) {
            returnVo.setMsg("模板ID不能为空！");
            return returnVo;
        }
        try {
            Model model = modelService.getModel(modelId.trim());


            //到时候需要添加分类
            String categoryCode = "1000";
            BpmnModel bpmnModel = modelService.getBpmnModel(model);
            //添加隔离信息
           // String tenantId = "flow";//不添加隔离信息
            //必须指定文件后缀名否则部署不成功
            Deployment deploy = repositoryService.createDeployment()
                    .name(model.getName())
                    .key(model.getKey())
                    .category(categoryCode)
                  //  .tenantId(tenantId)
                    .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                    .deploy();

            //add by huxipi  增加其对应表单的部署  ,还有子流程的部署todo
            List<ModelRelation> modelRelations=modelRelationRepository.findByParentModelIdAndType(model.getId(), ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
            for(ModelRelation modelRelation:modelRelations){
                Model form_model = modelService.getModel(modelRelation.getModelId());

                formRepositoryService.createDeployment().parentDeploymentId(deploy.getId())
                        .name(form_model.getName())
                        .addFormDefinition(form_model.getName()+".form",form_model.getModelEditorJson())//必须以form结尾否则不插入表单定义
               //         .tenantId(tenantId)
                        .deploy();
            }


            returnVo.setData(deploy.getId());
            returnVo.setMsg("部署流程成功！");
            returnVo.setCode(ReturnCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            returnVo.setMsg(String.format("部署流程异常！- %s", e.getMessage()));
        }
        return returnVo;
    }
    /**
     * 显示xml
     *
     * @param modelId
     * @return
     */
    @ApiOperation(value = "显示xml文本")
    @GetMapping(value = "/loadXmlByModelId/{modelId}")
    public void loadXmlByModelId(@PathVariable @ApiParam String modelId, HttpServletResponse response) {
        try {
            Model model = modelService.getModel(modelId);
            byte[] b = modelService.getBpmnXML(model);
            response.setHeader("Content-type", "text/xml;charset=UTF-8");
            response.getOutputStream().write(b);
        } catch (Exception e) {
            LOGGER.error("ApiFlowableModelResource-loadXmlByModelId:" + e);
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "以图片形式显示xml")
    @GetMapping(value = "/loadPngByModelId/{modelId}")
    @Encryptable(enable = false)
    public String loadPngByModelId(@PathVariable @ApiParam String modelId, HttpServletResponse response) {
        Model model = modelService.getModel(modelId);
        BpmnModel bpmnModel = modelService.getBpmnModel(model, new HashMap<>(), new HashMap<>());
        InputStream is = flowProcessDiagramGenerator.generateDiagram(bpmnModel);
        String img="";
        try {
            img= Base64.getEncoder().encodeToString(IOUtils.toByteArray(is));

          /*  response.setHeader("Content-Type", "image/png");
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
           */

        } catch (Exception e) {
            LOGGER.error("ApiFlowableModelResource-loadPngByModelId:" + e);
            e.printStackTrace();
        }
        return img;
    }


}
