package com.lsh.wms.core.service.location;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.core.constant.LocationConstant;
import com.lsh.wms.core.dao.baseinfo.BaseinfoLocationWarehouseDao;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.baseinfo.BaseinfoLocationBin;
import com.lsh.wms.model.baseinfo.BaseinfoLocationWarehouse;
import com.lsh.wms.model.baseinfo.IBaseinfoLocaltionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 马启迪 maqidi@lsh123.com
 * @Date 16/7/23 下午8:29
 */
@Component
@Transactional(readOnly = true)
public class BaseinfoLocationWarehouseService implements IStrategy {

    @Autowired
    private BaseinfoLocationWarehouseDao baseinfoLocationWarehouseDao;


    @Transactional(readOnly = false)
    public void insert(IBaseinfoLocaltionModel iBaseinfoLocaltionModel) {
        baseinfoLocationWarehouseDao.insert((BaseinfoLocationWarehouse) iBaseinfoLocaltionModel);
    }

    @Transactional(readOnly = false)
    public void update(IBaseinfoLocaltionModel iBaseinfoLocaltionModel) {
        baseinfoLocationWarehouseDao.update((BaseinfoLocationWarehouse) iBaseinfoLocaltionModel);
    }

    public BaseinfoLocation getBaseinfoItemLocationModelById(Long id) {
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("locationId", id);
        mapQuery.put("isValid", LocationConstant.IS_VALID);
        List<BaseinfoLocationWarehouse> warehouseList = baseinfoLocationWarehouseDao.getBaseinfoLocationWarehouseList(mapQuery);
        return warehouseList.size() > 0 ? warehouseList.get(0) : null;
    }

    public Integer countBaseinfoLocaltionModel(Map<String, Object> params) {
        params.put("isValid", LocationConstant.IS_VALID);
        return baseinfoLocationWarehouseDao.countBaseinfoLocationWarehouse(params);
    }

    public List<BaseinfoLocation> getBaseinfoLocaltionModelList(Map<String, Object> params) {
        params.put("isValid", LocationConstant.IS_VALID);
        return (List<BaseinfoLocation>) (List<?>) baseinfoLocationWarehouseDao.getBaseinfoLocationWarehouseList(params);
    }
    @Transactional(readOnly = false)
    public IBaseinfoLocaltionModel removeLocation(Long locationId) {
        //先查,然后删除
        BaseinfoLocationWarehouse temp = (BaseinfoLocationWarehouse) this.getBaseinfoItemLocationModelById(locationId);
        if (temp == null) {
            throw new BizCheckedException("2180003");
        }
        temp.setIsValid(LocationConstant.NOT_VALID);
        this.update(temp);
        return temp;
    }
}
