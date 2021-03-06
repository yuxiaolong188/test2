package com.cims.bpm.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface IRunFlowableActinstDao {

    /**
     * 删除节点信息
     * @param ids ids
     */
    public void deleteRunActinstsByIds(List<String> ids) ;
}
