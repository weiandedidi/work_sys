package com.lsh.wms.api.service.inhouse;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.model.transfer.StockTransferPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by mali on 16/8/1.
 */
public interface IStockTransferRpcService {
    Long addPlan(StockTransferPlan plan) throws BizCheckedException;
    //Map<String, Object> scanToLocation(Map<String, Object> params) throws BizCheckedException;
    public void scanToLocation(TaskEntry taskEntry,
                                              BaseinfoLocation location) throws BizCheckedException;
    //Map<String, Object> scanFromLocation(Map<String, Object> params) throws BizCheckedException;
    public void scanFromLocation(TaskEntry taskEntry,
                                                BaseinfoLocation location,
                                                BigDecimal uomQty) throws BizCheckedException;
    Long assign(Long staffId) throws BizCheckedException;
    Map<String, Object> sortTask(Long staffId) throws BizCheckedException;
    //void updatePlan(StockTransferPlan plan) throws BizCheckedException;
    void cancelPlan(Long taskId) throws BizCheckedException;
    //void createStockTransfer() throws BizCheckedException;
    //Long allocateToLocationId(StockQuant quant) throws BizCheckedException;
}
