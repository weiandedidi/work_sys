package com.lsh.wms.api.service.location;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.api.model.location.LocationDetailRequest;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;

import javax.ws.rs.QueryParam;
import java.util.Map;

/**
 * Created by fengkun on 16/7/11.
 */
public interface ILocationRestService {
    public String getLocation(Long locationId) throws BizCheckedException;

    public String getStoreLocationIds(Long locationId) throws BizCheckedException;

    public String getFatherByType(Long locationId, Long type) throws BizCheckedException;

    public String getFatherArea(Long locationId) throws BizCheckedException;

    public String getWarehouseLocationId();

    public String getInventoryLostLocationId();

    public String insertLocation(LocationDetailRequest request) throws BizCheckedException;

    public String updateLocation(LocationDetailRequest request) throws BizCheckedException;

    public String countBaseinfoLocation(Map<String, Object> params) throws BizCheckedException;

    public String searchList(Map<String, Object> params) throws BizCheckedException;

    public String getTemp(Long type) throws BizCheckedException;

    String getItemLocation()throws BizCheckedException;

    String getFatherRegion() throws BizCheckedException;

    //仓库找货区
    public String getRegionByWareHouseId();

    //货区找货架
    public String getShelfByRegionId(Long locationId) throws BizCheckedException;

    //货架找货位
    public String getBinByShelf(Long locationId) throws BizCheckedException;

    //仓库找货位
    public String getBinByWarehouseId();

    /**
     * 货物所有的拣货位
     *
     * @return
     */
    public String getAllColletionBins();
    /*
     * 获取可用拣货位
     */
    String getColletionBinsCanUse();

    /**
     * 获取全货架
     *
     * @return
     */
    public String getAllShelfs();

    /**
     * 将mysql一次性导入redis
     *
     * @return
     */
    public String syncRedisAll() throws BizCheckedException;

    /**
     * 锁库位
     * @param locationId
     * @return 锁结果
     * @throws BizCheckedException
     */
    public String lockLocation(Long locationId) throws BizCheckedException;

    /**
     * 解锁库位
     * @param locationId
     * @return
     * @throws BizCheckedException
     */
    public String unlockLocation(Long locationId) throws BizCheckedException;

    public String getLocationIdByCode(String locationCode) throws BizCheckedException;

    String getLocationByCode(@QueryParam("locationCode") String locationCode) throws BizCheckedException;

    /**
     * 根据库位的左右范围获取指定库位
     * @return
     * @throws BizCheckedException
     */
    public String getRangeLocationList() throws BizCheckedException;

    /**
     * 获取下一层的位置节点
     * @return
     * @throws BizCheckedException
     */
    public String getNextLevelLocations() throws BizCheckedException;

    /**
     * 获取位置类型,是货架还是阁楼
     * @param locationId
     * @return
     * @throws BizCheckedException
     */
    String getLocationType(@QueryParam("locationId") String locationId) throws BizCheckedException;
    String initLocationTree();
    String splitBin() throws BizCheckedException;

    String getLocationByType(Long type) throws BizCheckedException;

    String getChildrenLocationsByFatherIdAndType(Long fatherLocationId, Long sonType)throws BizCheckedException;

    String buildLocations() throws BizCheckedException;

    String getMaxCoordsInNextLevelsByLocationCode(String fatherLocationCode) throws BizCheckedException;
}
