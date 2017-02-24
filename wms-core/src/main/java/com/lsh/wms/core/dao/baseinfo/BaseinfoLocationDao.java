package com.lsh.wms.core.dao.baseinfo;

import com.lsh.wms.core.dao.MyBatisRepository;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@MyBatisRepository
public interface BaseinfoLocationDao{

	void insert(BaseinfoLocation baseinfoLocation);

	void update(BaseinfoLocation baseinfoLocation);

	BaseinfoLocation getBaseinfoLocationById(Long id);

    Integer countBaseinfoLocation(Map<String, Object> params);

    List<BaseinfoLocation> getBaseinfoLocationList(Map<String, Object> params);
	//按照code精确查找location
	List<BaseinfoLocation> getLocationbyCode(Map<String, Object> params);
	//设置dock的服务,处理出入码头的关联查询
	List<BaseinfoLocation> getDockList(Map<String, Object> params);
	// 获取子节点列表
	List<BaseinfoLocation> getChildrenLocationList(Map<String, Object> params);
	//码头dock的计数
	Integer countDockList(Map<String, Object> params);

	List<BaseinfoLocation> lock(Long id);
	/**
	 * 传入left和right列查询 固定列区间的库位
	 * @param params
	 * @return
	 */
	List<BaseinfoLocation> getRangeLocationList(Map<String, Object> params);

	List<BaseinfoLocation> getBaseinfoLocationListPC(Map<String, Object> params);

	List<BaseinfoLocation> getSortLocationsByFatherId(Map<String, Object> params);

	List<Long> getLocationBinByType(Map<String, Object> params);


}