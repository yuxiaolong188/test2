package com.cims.bpm.dao;

import com.cims.bpm.vo.ProcessInstanceQueryVo;
import com.github.pagehelper.Page;
import com.cims.bpm.vo.ProcessInstanceVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface IFlowableProcessInstanceDao {

    /**
     * 通过条件查询流程实例VO对象列表
     * @param params 参数
     * @return
     */
    public Page<ProcessInstanceVo> getPagerModel(ProcessInstanceQueryVo params);
}
