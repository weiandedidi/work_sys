package com.lsh.wms.core.service.location;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.core.constant.LocationConstant;
import com.lsh.wms.core.dao.baseinfo.BaseinfoLocationShelfDao;
import com.lsh.wms.model.baseinfo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 马启迪 maqidi@lsh123.com
 * @Date 16/7/23 下午7:22
 */
@Component
@Transactional(readOnly = true)
public class BaseinfoLocationShelfService implements IStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BaseinfoLocationShelfService.class);
    @Autowired
    private BaseinfoLocationShelfDao baseinfoLocationShelfDao;

    @Transactional(readOnly = false)
    public void insert(IBaseinfoLocaltionModel iBaseinfoLocaltionModel) {
        iBaseinfoLocaltionModel.setClassification(LocationConstant.CLASSIFICATION_SHELFS);
        baseinfoLocationShelfDao.insert((BaseinfoLocationShelf) iBaseinfoLocaltionModel);
    }

    @Transactional(readOnly = false)
    public void update(IBaseinfoLocaltionModel iBaseinfoLocaltionModel) {
        baseinfoLocationShelfDao.update((BaseinfoLocationShelf) iBaseinfoLocaltionModel);
    }

    public BaseinfoLocation getBaseinfoItemLocationModelById(Long id) {
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("locationId", id);
        mapQuery.put("isValid", LocationConstant.IS_VALID);
        List<BaseinfoLocationShelf> shelfList = baseinfoLocationShelfDao.getBaseinfoLocationShelfList(mapQuery);
        return shelfList.size() > 0 ? shelfList.get(0) : null;
    }

    public Integer countBaseinfoLocaltionModel(Map<String, Object> params) {
        params.put("isValid",LocationConstant.IS_VALID);
        return baseinfoLocationShelfDao.countBaseinfoLocationShelf(params);
    }

    public List<BaseinfoLocation> getBaseinfoLocaltionModelList(Map<String, Object> params) {
        params.put("isValid",LocationConstant.IS_VALID);
        return (List<BaseinfoLocation>) (List<?>) baseinfoLocationShelfDao.getBaseinfoLocationShelfList(params);
    }
    @Transactional(readOnly = false)
    public IBaseinfoLocaltionModel removeLocation(Long locationId) {
        //先查,然后删除
        BaseinfoLocationShelf temp = (BaseinfoLocationShelf) this.getBaseinfoItemLocationModelById(locationId);
        if (temp == null) {
            throw new BizCheckedException("2180003");
        }
        temp.setIsValid(LocationConstant.NOT_VALID);
        this.update(temp);
        return temp;
    }
}
