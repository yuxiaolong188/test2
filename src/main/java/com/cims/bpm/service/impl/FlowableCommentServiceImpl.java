package com.cims.bpm.service.impl;

import com.cims.bpm.cmd.AddHisCommentCmd;
import com.cims.bpm.common.CommentTypeEnum;
import com.cims.bpm.service.IFlowableCommentService;
import com.cims.bpm.vo.CommentVo;
import com.cims.bpm.dao.IFlowableCommentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowableCommentServiceImpl extends BaseProcessService implements IFlowableCommentService {

    @Autowired
    private IFlowableCommentDao flowableCommentDao;

    @Override
    public void addComment(CommentVo comment) {
        managementService.executeCommand(new AddHisCommentCmd(comment.getTaskId(), comment.getUserId(), comment.getProcessInstanceId(),
                comment.getType(), comment.getMessage()));
    }

    @Override
    public List<CommentVo> getFlowCommentVosByProcessInstanceId(String processInstanceId) {
        List<CommentVo> datas = flowableCommentDao.getFlowCommentVosByProcessInstanceId(processInstanceId);
        datas.forEach(commentVo -> {
            commentVo.setTypeName(CommentTypeEnum.getEnumMsgByType(commentVo.getType()));
        });
        return datas;
    }
}