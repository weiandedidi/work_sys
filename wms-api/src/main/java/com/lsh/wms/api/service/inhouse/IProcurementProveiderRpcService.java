package com.lsh.wms.api.service.inhouse;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.transfer.StockTransferPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mali on 16/8/1.
 */
public interface IProcurementProveiderRpcService {
    boolean addProcurementPlan (StockTransferPlan plan) throws  BizCheckedException;
    boolean updateProcurementPlan(StockTransferPlan plan)  throws BizCheckedException;
    void createProcurement(boolean canMax) throws BizCheckedException;
    void createProcurementByMax(boolean canMax) throws BizCheckedException;
    void scanFromLocation(Map<String, Object> params) throws BizCheckedException;
    void scanToLocation(Map<String, Object> params) throws  BizCheckedException;
    Long assign(Long staffId) throws BizCheckedException;
    void createLoftProcurement(boolean canMax) throws BizCheckedException;
    boolean checkAndFillPlan(StockTransferPlan plan) throws BizCheckedException;
    boolean checkPlan(StockTransferPlan plan) throws BizCheckedException;
    Set<Long> getOutBoundLocation(Long itemId,Long locationId);
    boolean adjustTaskQty(BigDecimal requiredQty,BaseinfoLocation pickLocation,Long itemId);
    void createShelfProcurementBak2(boolean canMax) throws BizCheckedException;
    void createShelfProcurementBak(boolean canMax) throws BizCheckedException;
    void createShelfProcurement(boolean canMax) throws BizCheckedException;
}
