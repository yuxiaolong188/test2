package com.cims.bpm.service;

import com.cims.bpm.vo.ModelVo;
import com.dragon.tools.vo.ReturnVo;
import org.springframework.web.multipart.MultipartFile;

public interface IFlowableModelService {

    /**
     * 导入模型
     * @param file 文件
     * @return
     */
    public ReturnVo<String> importProcessModel(MultipartFile file);

    /**
     * 添加模型
     * @param modelVo
     * @return
     */
    public ReturnVo<String> addModel(ModelVo modelVo);
}
