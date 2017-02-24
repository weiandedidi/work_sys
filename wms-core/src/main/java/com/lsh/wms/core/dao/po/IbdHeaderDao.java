package com.lsh.wms.core.dao.po;

import com.lsh.wms.core.dao.MyBatisRepository;
import com.lsh.wms.model.po.IbdHeader;

import java.util.List;
import java.util.Map;

@MyBatisRepository
public interface IbdHeaderDao {

	void insert(IbdHeader ibdHeader);
	
	void update(IbdHeader ibdHeader);

	void updateByOrderOtherIdOrOrderId(IbdHeader ibdHeader);

	void batchUpdateIbdHeaderByOrderId(List<IbdHeader> ibdHeaders);

	IbdHeader getIbdHeaderById(Long id);

	Integer countIbdHeader(Map<String, Object> params);

	List<IbdHeader> getIbdHeaderList(Map<String, Object> params);

	List<Map<String, Object>> getPoKanBanCount(Long orderType);

	List<IbdHeader> getPoDayCount(Long orderType);

	List<IbdHeader> getIbdListOrderByDate(String orderIds);

	void updateStatusTOthrow(Map<String, Object> params);

	IbdHeader lockIbdHeaderByOrderId(Long orderId);
	
}