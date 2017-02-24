package com.lsh.wms.service.outbound;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.api.service.container.IContainerRpcService;
import com.lsh.wms.api.service.csi.ICsiRpcService;
import com.lsh.wms.api.service.pick.IQCRpcService;
import com.lsh.wms.api.service.tu.ITuRpcService;
import com.lsh.wms.core.constant.*;
import com.lsh.wms.core.service.csi.CsiCustomerService;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.location.LocationService;
import com.lsh.wms.core.service.so.SoOrderService;
import com.lsh.wms.core.service.stock.StockMoveService;
import com.lsh.wms.core.service.stock.StockQuantService;
import com.lsh.wms.core.service.taking.StockTakingService;
import com.lsh.wms.core.service.task.BaseTaskService;
import com.lsh.wms.core.service.tu.TuService;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.baseinfo.BaseinfoContainer;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiCustomer;
import com.lsh.wms.model.csi.CsiSku;
import com.lsh.wms.model.so.ObdHeader;
import com.lsh.wms.model.stock.StockMove;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.tu.TuDetail;
import com.lsh.wms.model.wave.WaveDetail;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zengwenjun on 16/7/30.
 */
@Service(protocol = "dubbo")
public class QCRpcService implements IQCRpcService {
    @Autowired
    private WaveService waveService;
    @Autowired
    private BaseTaskService baseTaskService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private StockQuantService stockQuantService;
    @Autowired
    private TuService tuService;
    @Reference
    private ITuRpcService iTuRpcService;
    @Reference
    private ICsiRpcService csiRpcService;
    @Autowired
    private CsiCustomerService csiCustomerService;
    @Autowired
    private StockMoveService stockMoveService;
    @Reference
    private IContainerRpcService iContainerRpcService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private StockTakingService stockTakingService;
    @Autowired
    private SoOrderService soOrderService;

    public void skipException(long id) throws BizCheckedException {
        WaveDetail detail = waveService.getWaveDetailById(id);
        if (detail == null) {
            throw new BizCheckedException("2070001");
        }
        //必须要进行库存操作
        detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_SKIP);
        waveService.updateDetail(detail);


        //库存操作
        //差异库存移到盘亏盘盈区
        Long itemId = detail.getItemId();
        Long qcTaskId = detail.getQcTaskId();
        StockMove move = new StockMove();
        BaseinfoLocation toLocation = locationService.getDiffAreaLocation();
        BaseinfoContainer toContainer = iContainerRpcService.createTray();
        //差异数量
        BigDecimal diffQty = detail.getPickQty().subtract(detail.getQcQty()).abs();
        if (null == toLocation) {
            throw new BizCheckedException("2180002");
        }

//        List<StockQuant> stockQuants = stockQuantService.getQuantsByContainerId(detail.getContainerId());
//        if (null == stockQuants || stockQuants.size() < 1) {
//            throw new BizCheckedException("2990043");
//        }
        //拣货0 没有库存
        BaseinfoLocation location = locationService.getLocation(detail.getAllocCollectLocation());

        Long locationId = location.getLocationId();

        move.setItemId(itemId);
        move.setSkuId(detail.getSkuId());
        move.setFromContainerId(detail.getContainerId());
        move.setFromLocationId(locationId);
        move.setToContainerId(toContainer.getContainerId());
        move.setToLocationId(toLocation.getLocationId());
        move.setQty(diffQty);
        move.setTaskId(qcTaskId);
        move.setOwnerId(detail.getOwnerId());

        //移到盘亏盘盈区,销库存
        stockTakingService.doQcPickDifference(move);

    }

    public void repairException(long id) throws BizCheckedException {
        WaveDetail detail = waveService.getWaveDetailById(id);
        if (detail == null) {
            throw new BizCheckedException("2070001");
        }
        detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_DONE);
        detail.setQcQty(detail.getPickQty());
        detail.setQcExceptionQty(new BigDecimal("0.0000"));
        detail.setQcException(WaveConstant.QC_EXCEPTION_NORMAL);
        waveService.updateDetail(detail);

    }

    public void fallbackException(long id) throws BizCheckedException {
        WaveDetail detail = waveService.getWaveDetailById(id);
        if (detail == null) {
            throw new BizCheckedException("2070001");
        }
        detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_NORMAL);
        detail.setQcQty(detail.getPickQty());
        detail.setQcException(WaveConstant.QC_EXCEPTION_NORMAL);
        detail.setQcExceptionQty(new BigDecimal("0.0000"));
        waveService.updateDetail(detail);
    }

    /**
     * 门店维度组盘列表
     *
     * @param mapQuery
     * @return
     * @throws BizCheckedException
     */
    public List<Map<String, Object>> getGroupList(Map<String, Object> mapQuery) throws BizCheckedException {
//        mapQuery.put("customerType", CustomerConstant.STORE); // 小店不合板(大小点都显示)
        mapQuery.put("isValid", 1); // 正常
        mapQuery.put("status", 1); // 正常
        List<CsiCustomer> csiCustomers = csiCustomerService.getCustomerList(mapQuery);
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (CsiCustomer customer : csiCustomers) {
            BigDecimal totalBoxes = new BigDecimal("0.0000"); // 该门店未装车总箱子
            BigDecimal restBoxes = new BigDecimal("0.0000"); // 该门店未装车余货箱子
            String storeNo = customer.getCustomerCode();
            List<Long> countedContainerIds = new ArrayList<Long>();
            //全量的waveDetail包含合板的
            List<WaveDetail> waveDetails = this.getQcWaveDetailsByStoreNo(storeNo);
            List<TaskInfo> qcDoneInfos = this.getQcDoneTaskInfoByWaveDetails(waveDetails);

            if (qcDoneInfos.size() > 0) {

                for (TaskInfo info : qcDoneInfos) {
                    //合板后数据不展示
                    Long tempContainerId = info.getContainerId();
                    if (!tempContainerId.equals(info.getMergedContainerId())) {
                        continue;
                    }

                    //校验装车
                    TuDetail tuDetail = iTuRpcService.getDetailByBoardId(tempContainerId);
                    if (null != tuDetail) {     //查到tudetail就是撞车了
                        continue;
                    }
                    totalBoxes = totalBoxes.add(info.getTaskPackQty()); //总箱数
                    //是否余货
                    if (info.getFinishTime() < DateUtils.getTodayBeginSeconds()) {
                        restBoxes = restBoxes.add(info.getTaskPackQty());
                    }
                }
            }
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("customerCode", customer.getCustomerCode());
            result.put("customerName", customer.getCustomerName());
            result.put("address", customer.getAddress());
            result.put("totalBoxes", totalBoxes);
            result.put("restBoxes", restBoxes);
            results.add(result);
        }
        return results;
    }

    /**
     * 组盘列表的total
     *
     * @param mapQuery
     * @return
     * @throws BizCheckedException
     */
    public Integer countGroupList(Map<String, Object> mapQuery) throws BizCheckedException {
//        mapQuery.put("customerType", CustomerConstant.STORE); // 小店
        mapQuery.put("status", 1);
        mapQuery.put("isValid", 1);
        Integer total = csiCustomerService.getCustomerCount(mapQuery);
        return total;
    }

    /**
     * 获取门店的组盘详情
     *
     * @param storeNo
     * @return 《containerId,map《string,object》》
     * @throws BizCheckedException
     */
    public Map<Long, Map<String, Object>> getGroupDetailByStoreNo(String storeNo) throws BizCheckedException {
        Map<Long, Map<String, Object>> results = new HashMap<Long, Map<String, Object>>();
        List<WaveDetail> waveDetails = this.getQcWaveDetailsByStoreNo(storeNo);

        if (null == waveDetails || waveDetails.isEmpty()) {
            return results;
        }
        //合板后的组盘托盘不在组盘中显示
        List<WaveDetail> totalWaveDetails = new ArrayList<WaveDetail>();
        //封装托盘码
        Map<Long, List<WaveDetail>> containerDetails = new HashMap<Long, List<WaveDetail>>();

        for (WaveDetail detail : waveDetails) {
            if (!detail.getMergedContainerId().equals(0L)) {  //合板了
                continue;
            }
            totalWaveDetails.add(detail);


            if (null == containerDetails.get(detail.getContainerId())) {
                containerDetails.put(detail.getContainerId(), new ArrayList<WaveDetail>());
            }
            List<WaveDetail> detailList = containerDetails.get(detail.getContainerId());
            detailList.add(detail);
            containerDetails.put(detail.getContainerId(), detailList);
        }

        List<TaskInfo> qcDoneTaskinfos = this.getQcDoneTaskInfoByWaveDetails(totalWaveDetails);
        if (null == qcDoneTaskinfos || qcDoneTaskinfos.size() < 1) {
            return results;
        }

        //taskinfo里面的qc托盘码唯一
        for (TaskInfo info : qcDoneTaskinfos) {
            Long containerId = info.getContainerId(); //获取托盘
            // 未装车的
            TuDetail tuDetail = tuService.getDetailByBoardId(containerId);
            Boolean needCount = true;
            if (null != tuDetail) {    //一旦能查到就是装车了
                needCount = false;
            }
            if (!needCount) {   //不需要计数
                continue;
            }
            // todo贵品
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("containerId", containerId);
            result.put("markContainerId", info.getContainerId());  //当前作为查找板子码标识的物理托盘码,随机选的
            result.put("containerCount", 1);
            result.put("packCount", info.getExt4()); //总箱数
            result.put("turnoverBoxCount", info.getExt3()); //周转箱
            result.put("storeNo", storeNo);


            result.put("finishTime", info.getFinishTime());   //贵品默认是部署的
            if (info.getFinishTime() < DateUtils.getTodayBeginSeconds()) {
                result.put("isRest", true);
            } else {
                result.put("isRest", false);
            }


            //贵品处理
            result.put("isExpensive", false);   //贵品默认是部署的
            List<WaveDetail> qcWaveDetail = containerDetails.get(containerId);
            if (null == qcWaveDetail || qcWaveDetail.isEmpty()) {
                results.put(containerId, result);
                continue;
            }

            //赵贵品
            Set<Long> itemIds = new HashSet<Long>();
            for (WaveDetail oneDetail : qcWaveDetail) {
                itemIds.add(oneDetail.getItemId());
            }


            for (Long itemId : itemIds) {
                BaseinfoItem item = itemService.getItem(itemId);
                if (null == item) { //不想报错
                    continue;
                }
                if (item.getIsValuable().equals(ItemConstant.TYPE_IS_VALUABLE)) {
                    result.put("isExpensive", true);   //贵品
                }
            }


            results.put(containerId, result);
        }
        return results;
    }

    /**
     * 通过门店号获取Taskinfo聚类qc完的托盘
     * 考虑到以后taskinfo的托盘可能复用,一个托盘码可能有多个完成的qc任务
     *
     * @return
     */
    public List<TaskInfo> getQcDoneTaskInfoByWaveDetails(List<WaveDetail> waveDetails) {
        List<TaskInfo> qcDoneInfos = new ArrayList<TaskInfo>();
        //拿qctaskId 没有qc不会写入wave
        HashSet<Long> qcTaskIds = new HashSet<Long>();
        if (waveDetails.size() > 0) {
            for (WaveDetail detail : waveDetails) {
                if (!detail.getQcTaskId().equals(0L)) {  //有qc任务
                    qcTaskIds.add(detail.getQcTaskId());
                }
            }
        }
        //过滤完成的qc任务
        if (qcTaskIds.size() > 0) {
            for (Long qcTaskId : qcTaskIds) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("taskId", qcTaskId);
                params.put("type", TaskConstant.TYPE_QC);
                params.put("status", TaskConstant.Done);
                List<TaskInfo> taskInfos = baseTaskService.getTaskInfoList(params);
                if (null != taskInfos && taskInfos.size() > 0) {
                    qcDoneInfos.add(taskInfos.get(0));
                }
            }
        }
        return qcDoneInfos;
    }

    public List<WaveDetail> getQcWaveDetailsByStoreNo(String customerCode) {
        //获取location的id
        CsiCustomer customer = csiCustomerService.getCustomerByCustomerCode(customerCode); // 门店对应的集货道
        if (null == customer) {
            throw new BizCheckedException("2180023");
        }
        if (null == customer.getCollectRoadId()) {
            throw new BizCheckedException("2180024");
        }
        BaseinfoLocation location = locationService.getLocation(customer.getCollectRoadId());

        //先去集货位拿到所有的托盘的wave_detailList
        List<WaveDetail> waveDetailList = new ArrayList<WaveDetail>();
        List<StockQuant> quants = stockQuantService.getQuantsByLocationId(location.getLocationId());

        //集货道没有库存,拣货缺交为0
        if (null == quants || quants.isEmpty()) {
            return new ArrayList<WaveDetail>();
        }

        for (StockQuant quant : quants) {
            Long containerId = quant.getContainerId();
            List<WaveDetail> waveDetails = waveService.getAliveDetailsByContainerId(containerId);
            if (null == waveDetails || waveDetails.size() < 1) {
                continue;
            }
            waveDetailList.addAll(waveDetails);
        }
        return waveDetailList;
    }

    public boolean repairExceptionRf(Map<String, Object> request) throws BizCheckedException {
        Long containerId = Long.valueOf(request.get("containerId").toString());
        String code = request.get("code").toString();
        BigDecimal qtyUom = new BigDecimal(request.get("uomQty").toString());   //可以是箱数或EA数量
        BigDecimal pickQty = new BigDecimal("0.0000");  //拣货数量
        if (null == containerId || null == code) {
            throw new BizCheckedException("2120019");
        }
        CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
        if (skuInfo == null) {
            throw new BizCheckedException("2120001");
        }
        long skuId = skuInfo.getSkuId();

        //查找qcInfo
        Map<String, Object> qcQuery = new HashMap<String, Object>();
        qcQuery.put("containerId", containerId);
        qcQuery.put("type", TaskConstant.TYPE_QC);
        List<TaskInfo> qcInfos = baseTaskService.getTaskInfoList(qcQuery);
        if (null == qcInfos || qcInfos.isEmpty()) {
            throw new BizCheckedException("2070003");
        }

        //现在qc没有写入ownerId
        TaskInfo qcInfo = qcInfos.get(0);
//        Long ownerId = qcInfo.getOwnerId();
        Long orderId = qcInfo.getOrderId();
        //找item
        ObdHeader obdHeader = soOrderService.getOutbSoHeaderByOrderId(orderId);
        if (null == obdHeader) {
            throw new BizCheckedException("2870006");
        }
        BaseinfoItem item = getItem(obdHeader.getOwnerUid(),code);

        Map<String, Object> detailQuery = new HashMap<String, Object>();
        detailQuery.put("containerId", containerId);
        detailQuery.put("itemId", item.getItemId());
        detailQuery.put("isValid", 1);
        detailQuery.put("isAlive", 1);
        List<WaveDetail> waveDetails = waveService.getWaveDetails(detailQuery);

        //以商品为维度,根据skuId和containerId找wave_detail的
//        List<WaveDetail> waveDetails = waveService.getDetailByContainerIdAndSkuId(containerId, skuId);
        if (null == waveDetails || waveDetails.size() < 1) {
            throw new BizCheckedException("2120018");
        }
        BigDecimal normalQty = new BigDecimal("0.0000");    //除了最后那条detail的正常的数量
        //缺交判断
        for (WaveDetail d : waveDetails) {
            pickQty = pickQty.add(d.getPickQty());
            if (d.getQcException() == WaveConstant.QC_EXCEPTION_NORMAL) {    //正常的
                normalQty = normalQty.add(d.getQcQty());
            }
        }

        BigDecimal qty = PackUtil.UomQty2EAQty(qtyUom, waveDetails.get(0).getAllocUnitName());
        BigDecimal inputQty = qty.setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal realPickQty = pickQty.setScale(0, BigDecimal.ROUND_DOWN);

        if (inputQty.compareTo(realPickQty) != 0) {   //多货或者数量相同
            throw new BizCheckedException("2120022");
        }

        /**
         * 忽略异常
         * 数量写在有异常的那条
         * 设置库存的qc的数量
         * 残次不追责
         */
        for (WaveDetail detail : waveDetails) {
            if (detail.getQcException() != WaveConstant.QC_EXCEPTION_NORMAL) {
                //残次不追责
                if (WaveConstant.QC_EXCEPTION_DEFECT == detail.getQcException()) {
                    //设置qc数量
                    detail.setQcFault(WaveConstant.QC_FAULT_NOMAL);
                    detail.setQcFaultQty(new BigDecimal("0.0000"));
                } else { //非残次拣货人的责任
                    detail.setQcFault(WaveConstant.QC_FAULT_PICK);
                    detail.setQcFaultQty(detail.getQcExceptionQty().abs());
                }
                detail.setQcQty(qty.subtract(normalQty));   //前面的都是正常的,有异常那条记录异常的数量
                detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_DONE);
//                waveService.updateDetail(detail);
            } else {
                detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_NORMAL);
            }
            //异常数量置为0
            detail.setQcExceptionQty(BigDecimal.ZERO);
            waveService.updateDetail(detail);
        }

        //检验修复完毕
        boolean result = true;
        for (WaveDetail d : waveDetails) {
            if (d.getQcExceptionDone() == WaveConstant.QC_EXCEPTION_STATUS_UNDO) {
                result = false;
            }
        }
        return result;
    }

    public boolean fallbackExceptionRf(Map<String, Object> request) throws BizCheckedException {
        Long containerId = Long.valueOf(request.get("containerId").toString());
        BigDecimal qtyUom = new BigDecimal(request.get("uomQty").toString());   //复QC的数量
        String code = request.get("code").toString();
        BigDecimal pickQty = new BigDecimal("0.0000");  //拣货数量

        if (null == containerId || null == code) {
            throw new BizCheckedException("2120019");
        }
        CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
        if (skuInfo == null) {
            throw new BizCheckedException("2120001");
        }
        long skuId = skuInfo.getSkuId();

        //查找qcInfo
        Map<String, Object> qcQuery = new HashMap<String, Object>();
        qcQuery.put("containerId", containerId);
        qcQuery.put("type", TaskConstant.TYPE_QC);
        List<TaskInfo> qcInfos = baseTaskService.getTaskInfoList(qcQuery);
        if (null == qcInfos || qcInfos.isEmpty()) {
            throw new BizCheckedException("2070003");
        }

        //现在qc没有写入ownerId
        TaskInfo qcInfo = qcInfos.get(0);
//        Long ownerId = qcInfo.getOwnerId();
        Long orderId = qcInfo.getOrderId();
        //找item
        ObdHeader obdHeader = soOrderService.getOutbSoHeaderByOrderId(orderId);
        if (null == obdHeader) {
            throw new BizCheckedException("2870006");
        }
       BaseinfoItem item = getItem(obdHeader.getOwnerUid(),code);

        Map<String, Object> detailQuery = new HashMap<String, Object>();
        detailQuery.put("containerId", containerId);
        detailQuery.put("itemId", item.getItemId());
        detailQuery.put("isValid", 1);
        detailQuery.put("isAlive", 1);
        List<WaveDetail> waveDetails = waveService.getWaveDetails(detailQuery);

        //以商品为维度,根据skuId和containerId找wave_detail的
//        List<WaveDetail> waveDetails = waveService.getDetailByContainerIdAndSkuId(containerId, skuId);
        if (null == waveDetails || waveDetails.size() < 1) {
            throw new BizCheckedException("2120018");
        }

        //数量判断
        for (WaveDetail d : waveDetails) {
            pickQty = pickQty.add(d.getPickQty());
        }

        BigDecimal qty = PackUtil.UomQty2EAQty(qtyUom, waveDetails.get(0).getAllocUnitName());
        BigDecimal inputQty = qty.setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal realPickQty = pickQty.setScale(0, BigDecimal.ROUND_DOWN);

        if (inputQty.compareTo(realPickQty) != 0) {   //多货或者数量相同
            throw new BizCheckedException("2120023");
        }

        //责任变更,记录数量
        for (WaveDetail d : waveDetails) {
            if (d.getQcException() != WaveConstant.QC_EXCEPTION_NORMAL) {
                //残次不追责
                if (WaveConstant.QC_EXCEPTION_DEFECT == d.getQcException()) {
                    //设置qc数量
                    d.setQcFault(WaveConstant.QC_FAULT_NOMAL);
                    d.setQcFaultQty(new BigDecimal("0.0000"));
                } else {
                    d.setQcFault(WaveConstant.QC_FAULT_QC);
                    d.setQcFaultQty(d.getQcExceptionQty().abs());
                }
                d.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_DONE);
                d.setQcQty(d.getPickQty());
                d.setQcExceptionQty(new BigDecimal("0.0000"));
                d.setQcException(WaveConstant.QC_EXCEPTION_NORMAL);
//                waveService.updateDetail(d);
            } else {
                d.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_NORMAL);
            }
            //异常数量置为0
            d.setQcExceptionQty(BigDecimal.ZERO);
            waveService.updateDetail(d);
        }
        //检验修复完毕
        boolean result = true;
        for (WaveDetail d : waveDetails) {
            if (d.getQcExceptionDone() == WaveConstant.QC_EXCEPTION_STATUS_UNDO) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 丢掉的库存放在差异区
     *
     * @param request
     * @return
     * @throws BizCheckedException
     */
    public boolean skipExceptionRf(Map<String, Object> request) throws BizCheckedException {
        Long containerId = Long.valueOf(request.get("containerId").toString());
        String code = request.get("code").toString();
        BigDecimal qtyUom = new BigDecimal(request.get("uomQty").toString());   //可以是箱数或EA数量
        BigDecimal pickQty = new BigDecimal("0.0000");  //拣货数量
        if (null == containerId || null == code) {
            throw new BizCheckedException("2120019");
        }

        CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
        if (skuInfo == null) {
            throw new BizCheckedException("2120001");
        }
        long skuId = skuInfo.getSkuId();

        //查找qcInfo
        Map<String, Object> qcQuery = new HashMap<String, Object>();
        qcQuery.put("containerId", containerId);
        qcQuery.put("type", TaskConstant.TYPE_QC);
        List<TaskInfo> qcInfos = baseTaskService.getTaskInfoList(qcQuery);
        if (null == qcInfos || qcInfos.isEmpty()) {
            throw new BizCheckedException("2070003");
        }

        //现在qc没有写入ownerId
        TaskInfo qcInfo = qcInfos.get(0);
//        Long ownerId = qcInfo.getOwnerId();
        Long orderId = qcInfo.getOrderId();
        //找item
        ObdHeader obdHeader = soOrderService.getOutbSoHeaderByOrderId(orderId);
        if (null == obdHeader) {
            throw new BizCheckedException("2870006");
        }
        BaseinfoItem item = getItem(obdHeader.getOwnerUid(),code);

        Map<String, Object> detailQuery = new HashMap<String, Object>();
        detailQuery.put("containerId", containerId);
        detailQuery.put("itemId", item.getItemId());
        detailQuery.put("isValid", 1);
        detailQuery.put("isAlive", 1);
        List<WaveDetail> waveDetails = waveService.getWaveDetails(detailQuery);

        //以商品为维度,根据skuId和containerId找wave_detail的
//        List<WaveDetail> waveDetails = waveService.getDetailByContainerIdAndSkuId(containerId, skuId);
        if (null == waveDetails || waveDetails.size() < 1) {
            throw new BizCheckedException("2120018");
        }
        BigDecimal normalQty = new BigDecimal("0.0000");    //除了最后那条detail的正常的数量
        //缺交判断
        for (WaveDetail d : waveDetails) {
            pickQty = pickQty.add(d.getPickQty());
            if (d.getQcException() == WaveConstant.QC_EXCEPTION_NORMAL) {    //正常的
                normalQty = normalQty.add(d.getQcQty());
            }
        }

        BigDecimal qty = PackUtil.UomQty2EAQty(qtyUom, waveDetails.get(0).getAllocUnitName());
        if (pickQty.compareTo(qty) <= 0) {   //数量相同和多货不可以
            throw new BizCheckedException("2120021");
        }

        /**
         * 忽略异常
         * 数量写在有异常的那条
         * 设置库存的qc的数量
         * 残次不追责
         */
        for (WaveDetail detail : waveDetails) {
            if (detail.getQcException() != WaveConstant.QC_EXCEPTION_NORMAL) {
                //残次不追责
                if (WaveConstant.QC_EXCEPTION_DEFECT == detail.getQcException()) {
                    //设置qc数量
                    detail.setQcFault(WaveConstant.QC_FAULT_NOMAL);
                    detail.setQcFaultQty(new BigDecimal("0.0000"));
                } else { //非残次拣货人的责任
                    detail.setQcFault(WaveConstant.QC_FAULT_PICK);
                    detail.setQcFaultQty(detail.getQcExceptionQty().abs());
                    detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_SKIP);
                }
                detail.setQcQty(qty.subtract(normalQty));   //前面的都是正常的,有异常那条记录异常的数量
                detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_SKIP);
                //设置异常的数量
                detail.setQcExceptionQty(pickQty.subtract(qty));
            } else {
                detail.setQcExceptionDone(PickConstant.QC_EXCEPTION_DONE_NORMAL);
                detail.setQcExceptionQty(BigDecimal.ZERO);
            }

//            //如果qc真正缺交且数量为0,waveDetail该品is_alive置为0
//            if (qtyUom.compareTo(BigDecimal.ZERO) == 0) {
//                detail.setIsAlive(0L);
//            }
            waveService.updateDetail(detail);
        }

        //差异库存移到盘亏盘盈区
        Long itemId = waveDetails.get(0).getItemId();
        Long qcTaskId = waveDetails.get(0).getQcTaskId();
        StockMove move = new StockMove();
        BaseinfoLocation toLocation = locationService.getDiffAreaLocation();
        BaseinfoContainer toContainer = iContainerRpcService.createTray();
        //差异数量
        BigDecimal diffQty = pickQty.subtract(qty).abs();
        if (null == toLocation) {
            throw new BizCheckedException("2180002");
        }

        BaseinfoLocation location = locationService.getLocation(waveDetails.get(0).getAllocCollectLocation());
        Long locationId = location.getLocationId();

        move.setOwnerId(waveDetails.get(0).getOwnerId());
        move.setItemId(itemId);
        move.setSkuId(skuId);
        move.setFromContainerId(containerId);
        move.setFromLocationId(locationId);
        move.setToContainerId(toContainer.getContainerId());
        move.setToLocationId(toLocation.getLocationId());
        move.setQty(diffQty);
        move.setTaskId(qcTaskId);
        //移到盘亏盘盈区,销库存
        stockTakingService.doQcPickDifference(move);

        //检验修复完毕
        boolean result = true;
        for (WaveDetail d : waveDetails) {
            if (d.getQcExceptionDone() == WaveConstant.QC_EXCEPTION_STATUS_UNDO) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 获取该门店所有的完成的qc的托盘明细,并写入贵品的标识
     *
     * @param customerCode
     * @return
     * @throws BizCheckedException
     */
    public Map<Long, Map<String, Object>> getQcDoneExpensiveMapByCustmerCode(String customerCode) throws BizCheckedException {
        List<WaveDetail> waveDetails = this.getQcWaveDetailsByStoreNo(customerCode);
        if (null == waveDetails || waveDetails.size() < 1) {
            return new HashMap<Long, Map<String, Object>>();
        }
        //qc完成校验
        Map<Long, Long> containerQcIdMap = new HashMap<Long, Long>();
        for (WaveDetail detail : waveDetails) {
            if (detail.getQcTaskId().equals(0L)) {
                continue;
            }
            Long containerId = null;
            //已装车的不给出
            if (detail.getMergedContainerId().equals(0L)) {

                containerId = detail.getContainerId();
            } else {

                containerId = detail.getMergedContainerId();
            }
            TuDetail tuDetail = tuService.getDetailByBoardId(containerId);
            if (null != tuDetail) {
                continue;
            }
            containerQcIdMap.put(detail.getContainerId(), detail.getQcTaskId());

        }
        //拿id查task,因为托盘可能复用
        Set<Long> qcDoneContainerIds = new HashSet<Long>();
        Map<Long, TaskInfo> qcTaskInfosMap = new HashMap<Long, TaskInfo>();

        for (Long key : containerQcIdMap.keySet()) {
            Long qcTaskId = containerQcIdMap.get(key);
            TaskInfo qcInfo = baseTaskService.getTaskInfoById(qcTaskId);
            if (qcInfo == null || !qcInfo.getStatus().equals(TaskConstant.Done)) {
                continue;
            }
            //已将装车的不给出

            qcDoneContainerIds.add(qcInfo.getContainerId());
            qcTaskInfosMap.put(qcTaskId, qcInfo);
        }
        //完成qc的detailList 判断贵品
        //找到当前的所有贵品,是按照有一个贵品就是贵品
        Map<Long, Map<String, Object>> qcDoneDetailMap = new HashMap<Long, Map<String, Object>>();
        for (WaveDetail oneDetail : waveDetails) {

            if (qcDoneContainerIds.contains(oneDetail.getContainerId())) {   //已经完成
                //看商品是否是贵品
                BaseinfoItem item = itemService.getItem(oneDetail.getItemId());
                if (null == item) { //不想报错
                    continue;
                }

                if (qcDoneDetailMap.get(oneDetail.getContainerId()) == null) {
                    Map<String, Object> qcDoneMap = new HashMap<String, Object>();
                    List<WaveDetail> containerDetails = new ArrayList<WaveDetail>();
                    qcDoneMap.put("containerDetails", containerDetails);
                    qcDoneMap.put("containerId", oneDetail.getContainerId());
                    qcDoneMap.put("isExpensive", false);
                    qcDoneMap.put("qcDoneInfo", qcTaskInfosMap.get(oneDetail.getQcTaskId()));
                    qcDoneMap.put("customerCode", customerCode);
                    qcDoneDetailMap.put(oneDetail.getContainerId(), qcDoneMap);
                }


                //加入
                Map<String, Object> qcDoneMap = qcDoneDetailMap.get(oneDetail.getContainerId());
                List<WaveDetail> qcDetailList = (List<WaveDetail>) qcDoneMap.get("containerDetails");
                qcDetailList.add(oneDetail);
                //贵品的判断
                if (item.getIsValuable().equals(ItemConstant.TYPE_IS_VALUABLE)) {
                    qcDoneMap.put("isExpensive", true);
                }
                qcDoneDetailMap.put(oneDetail.getContainerId(), qcDoneMap);
            }
        }

        if (qcDoneDetailMap.isEmpty()) {
            return new HashMap<Long, Map<String, Object>>();
        }
        Map<Long, Map<String, Object>> dateMap = new HashMap<Long, Map<String, Object>>();
        //贵品过滤
        for (Long key : qcDoneDetailMap.keySet()) {
            boolean isExpensive = Boolean.valueOf(qcDoneDetailMap.get(key).get("isExpensive").toString());
            if (isExpensive) {
                dateMap.put(key, qcDoneDetailMap.get(key));
            }
        }
        return dateMap;
    }


    /**
     * 先国条后箱子码
     *
     * @param ownerId
     * @param code
     * @return
     * @throws BizCheckedException
     */
    private BaseinfoItem getItem(Long ownerId, String code) throws BizCheckedException {
        BaseinfoItem baseinfoItem = null;
        //国条码
        CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
        if (null != skuInfo && skuInfo.getSkuId() != null) {
            baseinfoItem = itemService.getItem(ownerId, skuInfo.getSkuId());
        }

        if (baseinfoItem != null) {
            return baseinfoItem;
        }
        //箱码
        baseinfoItem = itemService.getItemByPackCode(ownerId, code);
        if (baseinfoItem != null) {
            return baseinfoItem;
        }
        if (baseinfoItem == null) {
            throw new BizCheckedException("2900001");
        }
        return baseinfoItem;
    }
}
