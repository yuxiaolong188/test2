package com.cims.bpm.dao;

import com.cims.bpm.vo.TaskQueryVo;
import com.github.pagehelper.Page;
import com.cims.bpm.vo.TaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface IFlowableTaskDao {
    /**
     * 查询待办任务
     * @param params 参数
     * @return
     */
    public Page<TaskVo> getApplyingTasks(TaskQueryVo params) ;

    /**
     * 查询已办任务列表
     * @param params 参数
     * @return
     */
    public Page<TaskVo> getApplyedTasks(TaskQueryVo params) ;

}
