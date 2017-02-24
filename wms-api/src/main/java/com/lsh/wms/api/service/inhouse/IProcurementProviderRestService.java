package com.lsh.wms.api.service.inhouse;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.transfer.StockTransferPlan;

import javax.ws.rs.QueryParam;

/**
 * Created by mali on 16/8/2.
 */
public interface IProcurementProviderRestService {
    String addProcurementPlan (StockTransferPlan plan) throws BizCheckedException;
    String updateProcurementPlan(StockTransferPlan plan)  throws BizCheckedException;
    String cancelProcurementPlan(long taskId)  throws BizCheckedException;
    String createProcurement() throws BizCheckedException;
    String getOutBoundLocation(long itemId,long locationId)  throws BizCheckedException;
    String fetchTask(long uid, long zoneId) throws BizCheckedException;
    String autoCreateWaveTask()  throws BizCheckedException;
    String test()  throws BizCheckedException;
}
