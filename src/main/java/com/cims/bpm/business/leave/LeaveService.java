package com.cims.bpm.business.leave;

import com.cims.bpm.business.mapper.ILeaveDao;
import com.dragon.tools.common.StringTools;
import com.dragon.tools.pager.PagerModel;
import com.dragon.tools.pager.Query;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LeaveService {

	@Autowired
	private ILeaveDao LeaveDao;

	public Leave getLeaveById(String id) throws Exception {
		return StringUtils.isNotBlank(id) ? this.LeaveDao.getLeaveById(id.trim()) : null;
	}

	public List<Leave> getLeaveByIds(String ids) throws Exception {
		ids = StringTools.converString(ids);
		return StringUtils.isNotBlank(ids) ? this.LeaveDao.getLeaveByIds(ids) : null;
	}

	public List<Leave> getLeaveByIdsList(List<String> ids) throws Exception {
		return CollectionUtils.isNotEmpty(ids) ? this.LeaveDao.getLeaveByIdsList(ids) : null;
	}

	public List<Leave> getAll(Leave Leave) throws Exception {
		return null != Leave ? this.LeaveDao.getAll(Leave) : null;
	}

	public PagerModel<Leave> getPagerModelByQuery(Leave Leave, Query query)
			throws Exception {
		PageHelper.startPage(query.getPageNum(),query.getPageSize(),query.getSortOrder());
		Page<com.cims.bpm.business.leave.Leave> page = this.LeaveDao.getPagerModelByQuery(Leave);
		return new PagerModel<>(page);
	}

	public int getByPageCount(Leave Leave)throws Exception {
		return (null != Leave) ? this.LeaveDao.getByPageCount(Leave) : 0;
	}

	public Leave insertLeave(Leave Leave) throws Exception {
		if (StringUtils.isBlank(Leave.getId())){
			Leave.setId(UUID.randomUUID().toString().replace("-",""));
		}
		Leave.setCreateTime(new Date());
		Leave.setUpdateTime(new Date());
		this.LeaveDao.insertLeave(Leave);
		return Leave;
	}

	public void insertLeaveBatch(List<Leave> Leaves) throws Exception {
		for (com.cims.bpm.business.leave.Leave Leave : Leaves) {
			if (StringUtils.isBlank(Leave.getId())){
				Leave.setId(UUID.randomUUID().toString().replace("-",""));
			}
			Leave.setCreateTime(new Date());
			Leave.setUpdateTime(new Date());
		}
		this.LeaveDao.insertLeaveBatch(Leaves);
	}

	public void delLeaveById(String id) throws Exception {
		if (StringUtils.isNotBlank(id)) {
			this.LeaveDao.delLeaveById(id.trim());
		}
	}

	public void delLeaveByIds(String ids) throws Exception {
		ids = StringTools.converString(ids);
		if(StringUtils.isNotBlank(ids)){
			this.LeaveDao.delLeaveByIds(ids);
		}
	}

	public void delLeaveByIdsList(List<String> ids) throws Exception {
		if(CollectionUtils.isNotEmpty(ids)){
			this.LeaveDao.delLeaveByIdsList(ids);
		}
	}

	public int updateLeave(Leave Leave) throws Exception {
		Leave.setUpdateTime(new Date());
		return this.LeaveDao.updateLeave(Leave);
	}

	public int updateLeaveByIds(String ids,Leave Leave) throws Exception {
		Leave.setUpdateTime(new Date());
		return this.LeaveDao.updateLeaveByIds(StringTools.converString(ids), Leave);
	}

	public int updateLeaveByIdsList(List<String> ids,Leave Leave) throws Exception {
		Leave.setUpdateTime(new Date());
		return this.LeaveDao.updateLeaveByIdsList(ids, Leave);
	}

	public int updateLeaveList(List<Leave> Leaves) throws Exception {
		return this.LeaveDao.updateLeaveList(Leaves);
	}

	//------------api------------

}