package com.lsh.wms.api.service.inhouse;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.taking.*;

import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by mali on 16/7/14.
 */
public interface IStockTakingProviderRpcService {
    void create(Long locationId,Long uid) throws BizCheckedException;
    void createTask(StockTakingHead head, List<StockTakingDetail> detailList,Long round,Long dueTime) throws BizCheckedException;
    List<StockTakingDetail> prepareDetailList(StockTakingHead head);
    List<Long> getTakingLocation(StockTakingRequest request,boolean needSort);
    void createTemporary(StockTakingRequest request);
    void  updateItem(Long itemId,Long detailId,Long proDate,Long round) throws BizCheckedException;
    void confirmDetail(List<Long> detailList) throws BizCheckedException;
    void batchCreateStockTaking(Map<Long,List<Long>> takingMap,Long takingType,Long planner) throws BizCheckedException;
    void createStockTaking(List<Long> locations,Long zoneId,Long takingType,Long planner) throws BizCheckedException;
    void createPlanWarehouse(List<Long> zoneIds, Long planer) throws BizCheckedException;
    void createPlanSales(List<Long> zoneIds, Long planer) throws BizCheckedException;
    void replay(List<Long> detailList,Long planner) throws BizCheckedException;
    void calcelTask(Long taskId) throws BizCheckedException;
    void createAndDoTmpTask(Long locationId,BigDecimal realEaQty,BigDecimal realUmoQty,String barcode,Long planner) throws BizCheckedException;
    List<StockTakingDetail>  fillTask(FillTakingPlanParam planParam) throws BizCheckedException;
    List<StockTakingDetail> checkFillTask(Long taskId,Long operator) throws BizCheckedException;
    void doneTaskDetail(List detailList) throws BizCheckedException;
}
