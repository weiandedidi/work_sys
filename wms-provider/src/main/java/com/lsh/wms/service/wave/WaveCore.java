package com.lsh.wms.service.wave;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.ObjUtils;
import com.lsh.base.common.utils.RandomUtils;
import com.lsh.wms.api.service.task.ITaskRpcService;
import com.lsh.wms.core.constant.*;
import com.lsh.wms.core.service.csi.CsiCustomerService;
import com.lsh.wms.core.service.item.ItemLocationService;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.location.LocationService;
import com.lsh.wms.core.service.pick.*;
import com.lsh.wms.core.service.so.SoOrderService;
import com.lsh.wms.core.service.stock.StockQuantService;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.core.service.task.MessageService;
import com.lsh.wms.core.service.wave.WaveAllocService;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.core.service.wave.WaveTemplateService;
import com.lsh.wms.core.service.zone.WorkZoneService;
import com.lsh.wms.model.ProcurementInfo;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoItemLocation;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiCustomer;
import com.lsh.wms.model.pick.*;
import com.lsh.wms.model.so.ObdDetail;
import com.lsh.wms.model.so.ObdHeader;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.task.TaskMsg;
import com.lsh.wms.model.wave.WaveAllocDetail;
import com.lsh.wms.model.wave.WaveDetail;
import com.lsh.wms.model.wave.WaveHead;
import com.lsh.wms.model.wave.WaveTemplate;
import com.lsh.wms.model.zone.WorkZone;
import com.lsh.wms.service.sync.AsyncEventService;
import com.lsh.wms.service.wave.split.SplitModel;
import com.lsh.wms.service.wave.split.SplitNode;
import org.apache.tools.ant.taskdefs.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

import java.util.*;

/**
 * Created by zengwenjun on 16/7/15.
 */
@Component
public class WaveCore {
    private static Logger logger = LoggerFactory.getLogger(WaveCore.class);

    @Autowired
    WaveService waveService;
    @Autowired
    WaveAllocService allocService;
    @Autowired
    PickTaskService taskService;
    @Autowired
    PickModelService modelService;
    @Autowired
    WorkZoneService workZoneService;
    @Autowired
    SoOrderService orderService;
    @Autowired
    ItemService itemService;
    @Reference
    private ITaskRpcService taskRpcService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ItemLocationService itemLocationService;
    @Autowired
    private StockQuantService stockQuantService;
    @Autowired
    private WaveTemplateService waveTemplateService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CsiCustomerService customerService;

    private WaveHead waveHead;
    List<ObdDetail> orderDetails;
    List<ObdHeader> orderList;
    Map<Long, ObdHeader> mapOrder2Head;
    WaveTemplate waveTemplate;
    List<BaseinfoLocation> unUsedCollectionRoadList;
    Map<Long, Long> mapOrder2CollectBin;
    Map<Long, Long> mapOldOrder2CollectBin;
    PickModelTemplate modelTpl;
    List<PickModel> modelList;
    List<WorkZone> zoneList;
    Map<Long, WorkZone> mapZone;
    Map<Long, List<BaseinfoLocation>> mapZone2StoreLocations;
    private long waveId;
    Map<String, List<BaseinfoItemLocation>> mapItemAndPickZone2PickLocations;
    Map<String, Long> mapItemAndPickZone2PickLocationRound;
    Map<String, BigDecimal> mapPickZoneLeftAllocQty;
    List<WaveAllocDetail> pickAllocDetailList;
    List<TaskEntry> entryList;
    Map<String, Map<Long, BigDecimal>> mapItemArea2LocationInventory;
    Map<Long, BaseinfoItem> mapItems;
    Set<Long> setLocationAllocated;
    boolean bAllocAll;


    public int release(long iWaveId) throws BizCheckedException {
        //获取波次信息
        bAllocAll = true;
        waveId = iWaveId;
        //执行波次准备
        this._prepare();
        //处理线路\运输计划,分配集货道
        this._allocDock();
        //执行配货
        this._alloc();
        logger.info("begin to run outbound model "+iWaveId);
        //执行捡货模型,输出最小捡货单元
        Map<Long,ProcurementInfo> needAdjustMap = this._executePickModel();
        //锁定集货区,记得发货的时候释放哟
        //虽然分配了,却未能占用,这就别锁了
        //固定集货道模式,这也别锁了,锁了也没用
        if(waveTemplate.getCollectDynamic()==1) {
            Set<Long> collectLocations = new HashSet<Long>();
            for(TaskEntry entry : entryList){
                collectLocations.add(((PickTaskHead)entry.getTaskHead()).getAllocCollectLocation());
            }
            for(Object locationId : collectLocations.toArray()){
                locationService.lockLocation((Long)locationId);
            }
        }
        //创建捡货任务
        if(entryList.size()>0) {
            taskRpcService.batchCreate(TaskConstant.TYPE_PICK, entryList);
            //通知补货调整补货任务
            for (Map.Entry<Long, ProcurementInfo> entry : needAdjustMap.entrySet()) {
                AsyncEventService.post(entry.getValue());
            }
            //发给调度创建纪录,调度器可能需要做些处理
            try {
                Set<Long> items = new HashSet<Long>();
                for (ObdDetail detail : orderDetails) {
                    items.add(detail.getItemId());
                }
                TaskMsg msg = new TaskMsg();
                msg.setType(TaskConstant.EVENT_WAVE_RELEASE);
                Map<String, Object> body = new HashMap<String, Object>();
                body.put("itemList", items.toArray());
                msg.setMsgBody(body);
                messageService.sendMessage(msg);
            } catch (Exception e) {
                logger.error(e.getCause()!=null ? e.getCause().getMessage():e.getMessage());
                logger.error("report wave info to redis fail");
            }
        }

        //todo 标记成功,这里有风险,就是捡货任务已经创建了,但是这里标记失败了,看咋搞????
        waveService.setStatus(waveId, WaveConstant.STATUS_RELEASE_SUCC, bAllocAll);
        return 0;
    }

    private void _allocDockByRoute() throws BizCheckedException {
        Map<Long, Long> mapCollectBinCount = new HashMap<Long, Long>();
        Map<Long, List<BaseinfoLocation>> mapCollectRoad2Bin = new HashMap<Long, List<BaseinfoLocation>>();
        Map<String, BaseinfoLocation> mapRoute2CollectRoad = new HashMap<String, BaseinfoLocation>();
        int iUseIdx = 0;
        for (ObdHeader order : orderList) {
            Long collectAllocId = 0L;
            String rout = order.getTransPlan();
            BaseinfoLocation collecRoad = mapRoute2CollectRoad.get(rout);
            if (collecRoad == null) {
                if (iUseIdx >= unUsedCollectionRoadList.size()) {
                    throw new BizCheckedException("2040012");
                }
                collecRoad = unUsedCollectionRoadList.get(iUseIdx);
                iUseIdx++;
                mapRoute2CollectRoad.put(rout, collecRoad);
                mapCollectBinCount.put(collecRoad.getLocationId(), 0L);
            }
            long binIdx = mapCollectBinCount.get(collecRoad.getLocationId());
            if (waveTemplate.getCollectBinUse() == 0) {
                //不使用精细的集货位
                collectAllocId = collecRoad.getLocationId();
            } else {
                //使用精细的集货位
                List<BaseinfoLocation> collectionBins = mapCollectRoad2Bin.get(collecRoad.getLocationId());
                if (collectionBins == null) {
                    if (collecRoad.getIsLeaf() == 1) {
                        //卧槽,已经是叶子节点了,没有集货位啊,怎么搞啊
                        collectionBins = new ArrayList<BaseinfoLocation>();
                    }
                    collectionBins = locationService.getChildrenLocationsByType(collecRoad.getLocationId(), LocationConstant.COLLECTION_BIN);
                    mapCollectRoad2Bin.put(collecRoad.getLocationId(), collectionBins);
                }
                if (order.getWaveIndex() >= collectionBins.size()) {
                    //卧槽,越界了,根本放不下啊,就放在道上将就将就吧
                    collectAllocId = collecRoad.getLocationId();
                } else {
                    //哈哈,终于找到你拉兄弟
                    //不过这里我们不处理集货位的占用处理逻辑,TMS你不要瞎搞啊,否则就把我害惨了
                    collectAllocId = collectionBins.get(order.getWaveIndex()).getLocationId();
                }
            }
            mapOrder2CollectBin.put(order.getOrderId(), collectAllocId);
        }
    }

    private void _allocDockByCustomerStatic() throws BizCheckedException {
        for (ObdHeader order : orderList) {
            order.getOrderUser();
            //如何获取这个设置呢,假设有了的话;
            BaseinfoLocation collectLocation = null;
            mapOrder2CollectBin.put(order.getOrderId(), collectLocation.getLocationId());

        }
    }

    private void _allocDockByCustomerDynamic() throws BizCheckedException {
        int iUseIdx = 0;
        Map<String, BaseinfoLocation> mapUser2CollectLocation = new HashMap<String, BaseinfoLocation>();
        for (ObdHeader order : orderList) {
            BaseinfoLocation collectLocation = mapUser2CollectLocation.get(order.getOrderUser());
            if(collectLocation == null){
                if (iUseIdx >= unUsedCollectionRoadList.size()) {
                    throw new BizCheckedException("2040012");
                }
                collectLocation = unUsedCollectionRoadList.get(iUseIdx);
                iUseIdx++;
                mapUser2CollectLocation.put(order.getOrderUser(), collectLocation);
            }
            mapOrder2CollectBin.put(order.getOrderId(), collectLocation.getLocationId());
        }
    }

    private void _allocDockNew() throws BizCheckedException{
        Map<Long, Long> mapCollectBinCount = new HashMap<Long, Long>();
        Map<Long, List<BaseinfoLocation>> mapCollectRoad2Bin = new HashMap<Long, List<BaseinfoLocation>>();
        Map<String, BaseinfoLocation> mapCollectRoad = new HashMap<String, BaseinfoLocation>();
        int iUseIdx = 0;
        if(waveTemplate.getCollectBinUse() == 1){
            //当面还不支持使用集货位,呵呵,开发中
            throw new BizCheckedException("2040019");
        }
        for (ObdHeader order : orderList) {
            if(mapOldOrder2CollectBin.get(order.getOrderId())!=null){
                //已经释放过了,应该是第二次释放了,就用第一次的配置就行了.
                mapOrder2CollectBin.put(order.getOrderId(), mapOldOrder2CollectBin.get(order.getOrderId()));
                continue;
            }
            Long collectAllocId = 0L;
            String takeKey = "";
            if(waveTemplate.getCollectAllocModel().equals(PickConstant.COLLECT_ALLOC_MODE_CUSTOMER)){
                takeKey = order.getDeliveryCode();
            }else if (waveTemplate.getCollectAllocModel().equals(PickConstant.COLLECT_ALLOC_MODE_ROUTE)){
                takeKey = order.getTransPlan();
            }else{
                //不支持的分配模式
                throw new BizCheckedException("2040019");
            }
            BaseinfoLocation collecRoad = mapCollectRoad.get(takeKey);
            if (collecRoad == null) {
                //分配集货道
                if(waveTemplate.getCollectDynamic() == 1){
                    //动态
                    if (iUseIdx >= unUsedCollectionRoadList.size()) {
                        throw new BizCheckedException("2040012");
                    }
                    collecRoad = unUsedCollectionRoadList.get(iUseIdx);
                    iUseIdx++;
                    mapCollectRoad.put(takeKey, collecRoad);
                    mapCollectBinCount.put(collecRoad.getLocationId(), 0L);
                }else{
                    //静态
                    if(!waveTemplate.getCollectAllocModel().equals(PickConstant.COLLECT_ALLOC_MODE_CUSTOMER)){
                        //指定模式不支持静态分配集货道
                        throw new BizCheckedException("2040019");
                    }
                    //go
                    CsiCustomer customer = customerService.getCustomerByCustomerCode(order.getDeliveryCode());
                    if(customer == null){
                        //门店找不到了哟草
                        throw new BizCheckedException("2040020");
                    }
                    long collecRoadId = customer.getCollectRoadId();
                    BaseinfoLocation location = locationService.getLocation(collecRoadId);
                    if(location == null
                            || (!location.getType().equals(LocationConstant.COLLECTION_ROAD)
                                && !location.getType().equals(LocationConstant.COLLECTION_BIN))){
                        throw new BizCheckedException("2040021");
                    }
                    collecRoad = location;
                    mapCollectRoad.put(takeKey, collecRoad);
                    mapCollectBinCount.put(collecRoad.getLocationId(), 0L);
                }
            }
            long binIdx = mapCollectBinCount.get(collecRoad.getLocationId());
            if (waveTemplate.getCollectBinUse() == 0) {
                //不使用精细的集货位
                collectAllocId = collecRoad.getLocationId();
            } else {
                //使用精细的集货位
                List<BaseinfoLocation> collectionBins = mapCollectRoad2Bin.get(collecRoad.getLocationId());
                if (collectionBins == null) {
                    if (collecRoad.getIsLeaf() == 1) {
                        //卧槽,已经是叶子节点了,没有集货位啊,怎么搞啊
                        collectionBins = new ArrayList<BaseinfoLocation>();
                    }
                    collectionBins = locationService.getChildrenLocationsByType(collecRoad.getLocationId(), LocationConstant.COLLECTION_BIN);
                    mapCollectRoad2Bin.put(collecRoad.getLocationId(), collectionBins);
                }
                if (order.getWaveIndex() >= collectionBins.size()) {
                    //卧槽,越界了,根本放不下啊,就放在道上将就将就吧
                    collectAllocId = collecRoad.getLocationId();
                } else {
                    //哈哈,终于找到你拉兄弟
                    //不过这里我们不处理集货位的占用处理逻辑,TMS你不要瞎搞啊,否则就把我害惨了
                    collectAllocId = collectionBins.get(order.getWaveIndex()).getLocationId();
                }
            }
            mapOrder2CollectBin.put(order.getOrderId(), collectAllocId);
        }
    }

    private void _allocDock() throws BizCheckedException{
        /*
        集货道分配模型分为:动态和静态
        占用方式分为:按客户和按线路
        集货位模式主要用于播种,暂时不支持
         */
        this._allocDockNew();
        //按照线路动态划分集货道
        //this._allocDockByRoute();
        //按照客户固定集货道
        //this._allocDockByCustomerStatic();
        //按照客户滚动集货道
        //this._allocDockByCustomerDynamic();

    }

    private Map<Long,ProcurementInfo> _executePickModel() throws BizCheckedException{
        Map<Long,ProcurementInfo> needAdjustItemMap = new HashMap<Long, ProcurementInfo>();
        //List<PickTaskHead> taskHeads = new LinkedList<PickTaskHead>();
        //List<WaveDetail> taskDetails = new LinkedList<WaveDetail>();
        entryList = new LinkedList<TaskEntry>();
        for(int zidx = 0; zidx < modelList.size(); ++zidx){
            PickModel model = modelList.get(zidx);
            List<SplitNode> splitNodes = new LinkedList<SplitNode>();
            {
                //初始化分裂数据
                SplitNode node = new SplitNode();
                node.details = new ArrayList<WaveDetail>();
                for (WaveAllocDetail ad : pickAllocDetailList) {
                    if(!ad.getPickZoneId().equals(model.getPickZoneId())){
                        continue;
                    }
                    WaveDetail detail = new WaveDetail();
                    ObjUtils.bean2bean(ad, detail);
                    detail.setAllocCollectLocation(mapOrder2CollectBin.get(detail.getOrderId()));
                    node.details.add(detail);
                }
                if(node.details.size()>0) {
                    splitNodes.add(node);
                }
            }
            if(splitNodes.size()==0){
                continue;
            }
            List<SplitNode> stopNodes = new LinkedList<SplitNode>();
            String splitModelNames[] = {
                    "SplitModelSetGroup",
                    "SplitModelBigItem",
                    "SplitModelSet",
                    "SplitModelSmallItem",
                    "SplitModelOrder",
                    "SplitModelWholePallet",
                    "SplitModelWholePackage",
                    "SplitModelContainer", //这个必须是最后一个,否则就会出大问题.
            };
            for(String modelName : splitModelNames){
                SplitModel splitModel = null;
                try {
                    splitModel = (SplitModel) Class.forName("com.lsh.wms.service.wave.split."+modelName).newInstance();
                } catch (Exception e){
                    logger.error("class init fail "+modelName);
                    throw  new BizCheckedException("");
                }
                splitModel.init(model, splitNodes, mapItems);
                splitModel.split(stopNodes);
                splitNodes = splitModel.getSplitedNodes();
            }
            if(splitNodes.size()>0){
                //卧槽,这是怎么回事,出bug了?
            }
            //转换成为任务
            //多zone的捡货单元不可混合,所用分开执行
            long iContainerTake = model.getContainerNumPerTask();
            //计算最佳划分组合
            long [] bestCutPlan = this.getBestCutPlan(stopNodes.size(), iContainerTake);
            int iChooseIdx = 0;
            for(int i = 0; i < bestCutPlan.length; ++i){
                TaskEntry entry = new TaskEntry();
                TaskInfo info = new TaskInfo();
                info.setPlanId(waveId);
                info.setWaveId(waveId);
                List<Object> pickTaskDetails = new LinkedList<Object>();
                info.setType(TaskConstant.TYPE_PICK);
                info.setSubType(model.getPickType());
                info.setExt1(model.getPickZoneId());
                PickTaskHead head = new PickTaskHead();
                head.setWaveId(waveId);
                head.setPickType(1);
                head.setPickZoneId(model.getPickZoneId());
                //head.setTransPlan("");
                //head.setDeliveryId(1L);
                info.setTaskName(String.format("波次[%d]-捡货任务[%d]", waveId, entryList.size()+1));
                for(int j = 0; j < bestCutPlan[i]; j++){
                    SplitNode node = stopNodes.get(iChooseIdx+j);
                    if(node.iPickType != 0)
                    {
                        info.setSubType(node.iPickType);
                    }
                    for(int k = 0; k < node.details.size(); ++k){
                        WaveDetail detail = node.details.get(k);
                        logger.info(String.format("model[%d] orderid[%d] item %d allocQty[%s] node.pickType[%d] model.pickType[%d]",
                                model.getPickModelId(),
                                mapOrder2Head.get(detail.getOrderId()).getOrderId(),
                                detail.getItemId(),
                                detail.getAllocQty().toString(),
                                node.iPickType,
                                model.getPickType()));
                        detail.setPickZoneId(model.getPickZoneId());
                        pickTaskDetails.add(detail);
                        head.setDeliveryId(detail.getOrderId());
                        info.setOrderId(detail.getOrderId());
                        //通知补货任务，调整补货数量
                        if(!needAdjustItemMap.containsKey(detail.getItemId())) {
                           needAdjustItemMap.put(detail.getItemId(),new ProcurementInfo(detail.getAllocPickLocation(), detail.getItemId()));
                        }

                        info.setTransPlan(mapOrder2Head.get(detail.getOrderId()).getTransPlan());
                        head.setAllocCollectLocation(detail.getAllocCollectLocation());
                        if(node.iPickType == PickConstant.SHELF_PALLET_TASK_TYPE) {
                            //卧槽,这代码写成这样也真的是没脸见人了
                            //来找找有没有货架上有库存的.
                            if (!model.getPickType().equals(PickConstant.SHELF_TASK_TYPE)) {
                                info.setSubType(model.getPickType());
                            } else {
                                Map<String, Object> queryMap = new HashMap<String, Object>();
                                queryMap.put("location", locationService.getLocation(detail.getPickAreaLocation()));
                                queryMap.put("itemId", detail.getItemId());
                                List<StockQuant> quants = stockQuantService.getQuants(queryMap);
                                boolean bFindShelfStore = false;
                                for (StockQuant quant : quants) {
                                    BaseinfoLocation loation = locationService.getLocation(quant.getLocationId());
                                    if (loation.getRegionType().equals(LocationConstant.SHELFS)
                                            && loation.getBinUsage().equals(BinUsageConstant.BIN_UASGE_STORE)
                                            && !setLocationAllocated.contains(quant.getLocationId())) {
                                        bFindShelfStore = true;
                                        detail.setAllocPickLocation(loation.getLocationId());
                                        logger.info(String.format("ITEM[%d] find  shelfstore, location[%s]", detail.getItemId(), loation.getLocationCode()));
                                        break;
                                    }
                                }
                                if (!bFindShelfStore) {
                                    logger.info(String.format("ITEM[%d] find no shelf store, qutants[%d]", detail.getItemId(), quants.size()));
                                    info.setSubType(model.getPickType());
                                }
                            }
                        }
                        setLocationAllocated.add(detail.getAllocPickLocation());
                    }
                }
                iChooseIdx += bestCutPlan[i];
                entry.setTaskInfo(info);
                entry.setTaskHead(head);
                entry.setTaskDetailList(pickTaskDetails);
                entryList.add(entry);
            }
        }
        return needAdjustItemMap;
    }

    private BigDecimal _allocNormal(ObdDetail detail, PickModel model, BaseinfoItem item, BaseinfoLocation location, BigDecimal leftAllocQty) throws BizCheckedException{
        BaseinfoItemLocation pickItemLocation = this._getPickLocation(item, location);
        if (pickItemLocation == null) {
            return leftAllocQty;
        }
        Long pickLocationId = pickItemLocation.getPickLocationid();
        long pick_unit = model.getPickUnit();
        if(pickItemLocation.getPickLocationType() != 0){
            pick_unit = pickItemLocation.getPickLocationType();
            //特殊的,如果捡货位已经维护了单独的出货单位,则强制按照这个出货单位去走.
        }
        BigDecimal pick_ea_num = null;
        String unitName = "";
        if (pick_unit == 1) {
            //ea
            pick_ea_num = BigDecimal.valueOf(1L);
            unitName = "EA";
        } else if (pick_unit == 2) {
            //整箱
            pick_ea_num = item.getPackUnit();
            //TODO ZHE  LI YOU WENTI
            unitName = PackUtil.PackUnit2Uom(item.getPackUnit(), "EA");
        } else if (pick_unit == 3) {
            //整托盘,卧槽托盘上的商品数怎么求啊,这里是有风险的,因为实际的码盘数量可能和实际的不一样.
            pick_ea_num = item.getPackUnit().multiply(BigDecimal.valueOf(item.getPileX() * item.getPileY() * item.getPileZ()));
            if (pick_ea_num.compareTo(BigDecimal.ZERO) == 0) {
            }
            unitName = (item.getPileX() * item.getPileY() * item.getPileZ()) + PackUtil.PackUnit2Uom(item.getPackUnit(), "EA");
        }
        //获取分拣分区下的可分配库存数量,怎么获取?
        BigDecimal zone_qty = this._getPickZoneLeftAllocQty(item, location);
        logger.info(String.format("wave %d item %d[%s] packunit %d-%s-%s leftQty %s zone %s pickArea %s getCanAllocQty %s",
                waveId,
                item.getItemId(),
                item.getSkuCode(),
                pick_unit,
                pick_ea_num.toString(),
                unitName,
                leftAllocQty.toString(),
                model.getZone().getZoneName(),
                location.getLocationCode(),
                zone_qty.toString()));
        if (zone_qty.compareTo(BigDecimal.ZERO) <= 0) {
            return leftAllocQty;
        }
        int alloc_x = leftAllocQty.divide(pick_ea_num, 0, BigDecimal.ROUND_DOWN).intValue();
        int zone_alloc_x = zone_qty.divide(pick_ea_num, 0, BigDecimal.ROUND_DOWN).intValue();
        alloc_x = alloc_x > zone_alloc_x ? zone_alloc_x : alloc_x;
        if (alloc_x == 0) {
            return leftAllocQty;
        }
        BigDecimal alloc_qty = pick_ea_num.multiply(BigDecimal.valueOf(alloc_x));
        WaveAllocDetail allocDetail = new WaveAllocDetail();
        allocDetail.setId(RandomUtils.genId());
        allocDetail.setSkuId(detail.getSkuId());
        allocDetail.setAllocQty(alloc_qty);
        //allocDetail.setLocId(detail.getLotNum()); ??
        allocDetail.setOrderId(detail.getOrderId());
        allocDetail.setOwnerId(mapOrder2Head.get(detail.getOrderId()).getOwnerUid());
        allocDetail.setPickZoneId(model.getPickZoneId());
        allocDetail.setReqQty(new BigDecimal(0));
        allocDetail.setAllocPickLocation(pickLocationId);
        allocDetail.setItemId(item.getItemId());
        //allocDetail.setSupplierId(mapOrder2Head.get(detail.getOrderId()).get); ??
        allocDetail.setWaveId(waveId);
        allocDetail.setAllocUnitQty(BigDecimal.valueOf(alloc_x));
        allocDetail.setAllocUnitName(unitName);
        allocDetail.setPickAreaLocation(location.getLocationId());
        allocDetail.setRefObdDetailOtherId(detail.getDetailOtherId());
        allocDetail.setPickArea(location);
        pickAllocDetailList.add(allocDetail);
        leftAllocQty = leftAllocQty.subtract(alloc_qty);
        logger.info(String.format("get real qty %s", alloc_qty.toString()));
        return leftAllocQty;
    }

    private BigDecimal _allocStockPickSame(ObdDetail detail, PickModel model, BaseinfoItem item, BaseinfoLocation location, BigDecimal leftAllocQty) throws BizCheckedException{
        //没补货机制的区域,存捡合一,得精细计算到每个货位
        String key = String.format("%d-%d", item.getItemId(), location.getLocationId());
        Map<Long, BigDecimal> locationInventory = mapItemArea2LocationInventory.get(key);
        if(locationInventory == null) {
            //pick area location
            BaseinfoLocation pickArea = locationService.getLocation(location.getLocationId());
            //getLocationUnAllocQty
            locationInventory = waveService.getLocationUnAllocQty(pickArea, item.getItemId());
            mapItemArea2LocationInventory.put(key, locationInventory);
        }
        // List<WaveDetail> splitDetails = new ArrayList<WaveDetail>();
        List<Map> allocInfos = waveService.allocByLocation(locationInventory, leftAllocQty, 0L);
        for (Map info : allocInfos) {
            WaveAllocDetail allocDetail = new WaveAllocDetail();
            allocDetail.setId(RandomUtils.genId());
            allocDetail.setSkuId(detail.getSkuId());
            allocDetail.setAllocQty((BigDecimal) info.get("allocQty"));
            //allocDetail.setLocId(detail.getLotNum()); ??
            allocDetail.setOrderId(detail.getOrderId());
            allocDetail.setOwnerId(mapOrder2Head.get(detail.getOrderId()).getOwnerUid());
            allocDetail.setPickZoneId(model.getPickZoneId());
            allocDetail.setReqQty(new BigDecimal(0));
            allocDetail.setAllocPickLocation((Long) info.get("locationId"));
            allocDetail.setItemId(item.getItemId());
            //allocDetail.setSupplierId(mapOrder2Head.get(detail.getOrderId()).get); ??
            allocDetail.setWaveId(waveId);
            allocDetail.setAllocUnitName(location.getRegionType()==LocationConstant.FLOOR ? item.getPackName() : "EA");
            allocDetail.setAllocUnitQty(PackUtil.EAQty2UomQty(allocDetail.getAllocQty(), allocDetail.getAllocUnitName()));
            allocDetail.setPickAreaLocation(location.getLocationId());
            allocDetail.setPickArea(location);
            allocDetail.setRefObdDetailOtherId(detail.getDetailOtherId());
            pickAllocDetailList.add(allocDetail);
            logger.info(String.format("wave %d item %d[%s] packunit -1-EA leftQty %s zone %s pickArea %s getCanAllocQty %s sotrePickSame %s",
                    waveId,
                    item.getItemId(),
                    item.getSkuCode(),
                    leftAllocQty.toString(),
                    model.getZone().getZoneName(),
                    location.getLocationCode(),
                    info.get("allocQty").toString(),
                    info.get("locationId").toString()));
            leftAllocQty = leftAllocQty.subtract((BigDecimal) info.get("allocQty"));
            logger.info(String.format("get real qty %s", info.get("allocQty").toString()));
        }
        return leftAllocQty;
    }

    private void _alloc() throws BizCheckedException{
        //进行库存精确锁定
        //按捡货分区设置设定库存锁定粒度
        //特殊货位属性精确锁定库存
        //库存锁定全局只允许单向运行,否则将全部错误
        //怎么加锁呢?草勒.
        //地堆区有货,优先配在地堆区
        pickAllocDetailList = new ArrayList<WaveAllocDetail>();
        if(waveHead.getIsResAlloc() == 0) {
            logger.info("begin to run alloc waveId{1}", waveId);
            for (int i = 0; i < orderDetails.size(); ++i) {
                ObdDetail detail = orderDetails.get(i);
                int zone_idx = 0;
                //获取商品的基本信息
                BaseinfoItem item = mapItems.get(detail.getItemId());
                if(item == null) {
                    item = itemService.getItem(mapOrder2Head.get(detail.getOrderId()).getOwnerUid(), detail.getSkuId());
                    mapItems.put(item.getItemId(), item);
                }
                if (item == null) {
                    logger.error("item get fail %d", detail.getSkuId());
                    throw new BizCheckedException("");
                }
                //获取商品的捡货位
                BigDecimal leftAllocQty = (detail.getOrderQty().multiply(detail.getPackUnit())).subtract(detail.getReleaseQty());
                for (PickModel model : modelList) {
                    if (leftAllocQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                    List<BaseinfoLocation> locationList = mapZone2StoreLocations.get(model.getPickZoneId());
                    for(BaseinfoLocation location : locationList) {
                        if (leftAllocQty.compareTo(BigDecimal.ZERO) <= 0) {
                            break;
                        }
                        if(location.getRegionType().equals(LocationConstant.SPLIT_AREA)
                                || location.getRegionType().equals(LocationConstant.FLOOR)){
                            leftAllocQty = this._allocStockPickSame(detail, model, item, location, leftAllocQty);
                        } else {
                            //有补货机制的区域,不考虑捡货位货量,只考虑区域货量
                            leftAllocQty = this._allocNormal(detail, model, item, location, leftAllocQty);
                        }
                    }
                }
                if(leftAllocQty.compareTo(BigDecimal.ZERO)>0){
                    bAllocAll = false;
                }
                //if (leftAllocQty.compareTo(BigDecimal.ZERO) > 0) {
                    logger.error(String.format("GOD WAVE %d order %d idx[%s] item[%d][%s] needQty[%s] releasedQty[%s] leftQty[%s] %s",
                        waveId,
                        detail.getOrderId(),
                        detail.getDetailOtherId(),
                        item.getItemId(),
                        item.getSkuCode(),
                        detail.getOrderQty().multiply(detail.getPackUnit()).toString(),
                            detail.getReleaseQty().toString(),
                        leftAllocQty.toString(),
                            leftAllocQty.compareTo(BigDecimal.ZERO)>0?"QUEJIAO":"NORMAL"));
                //}
            }
            //存储配货结果
            //waveService.storeAlloc(waveHead, pickAllocDetailList);
        }else{
            logger.info("skip to run alloc waveId[%d], load from db", waveId);
            pickAllocDetailList = allocService.getAllocDetailsByWaveId(waveId);
        }
    }


    private void _prepare() throws BizCheckedException{
        mapItemAndPickZone2PickLocations = new HashMap<String, List<BaseinfoItemLocation>>();
        mapItemAndPickZone2PickLocationRound = new HashMap<String, Long>();
        mapPickZoneLeftAllocQty = new HashMap<String, BigDecimal>();
        mapOrder2CollectBin = new HashMap<Long, Long>();
        mapOldOrder2CollectBin = new HashMap<Long, Long>();
        mapItemArea2LocationInventory = new HashMap<String, Map<Long, BigDecimal>>();
        setLocationAllocated = new HashSet<Long>();
        mapItems = new HashMap<Long, BaseinfoItem>();
        this._prepareWave();
        this._prepareOrder();
        this._preparePickModel();
    }

    private void _prepareWave() throws BizCheckedException{
        waveHead = waveService.getWave(waveId);
        if(waveHead==null){
            throw new BizCheckedException("2040001");
        }
        //集货模版
        waveTemplate = waveTemplateService.getWaveTemplate(waveHead.getWaveTemplateId());
        if(waveTemplate==null){
            throw new BizCheckedException("2040008");
        }
        //获取集货组
        BaseinfoLocation locGrp = locationService.getLocation(waveTemplate.getCollectLocations());
        if(locGrp==null){
            throw new BizCheckedException("2040009");
        }
        //获取集货道列表
        List<BaseinfoLocation> collectLocations = locationService.getChildrenLocationsByType(locGrp.getLocationId(), LocationConstant.COLLECTION_ROAD);
        if(collectLocations.size()==0){
            throw new BizCheckedException("2040010");
        }
        unUsedCollectionRoadList = new ArrayList<BaseinfoLocation>();
        for(BaseinfoLocation location : collectLocations){
            if(location.getCanUse()==1 && location.getIsLocked()==0){
                unUsedCollectionRoadList.add(location);
            }
        }
        //无可用的集货道
        if(unUsedCollectionRoadList.size()==0){
            //throw new BizCheckedException("2040011");
        }
        //集货到分配排序
        //TODO collection sort

        List<WaveDetail> releasedWaveList = waveService.getDetailsByWaveId(waveId);
        for(WaveDetail detail : releasedWaveList){
            mapOldOrder2CollectBin.put(detail.getOrderId(), detail.getAllocCollectLocation());
        }

        List<Long> allocatedLocationList = waveService.getAllocatedLocationList();
        for(Long locationId : allocatedLocationList){
            setLocationAllocated.add(locationId);
        }
    }
    
    private void _prepareOrder() throws BizCheckedException{
        orderDetails = new LinkedList<ObdDetail>();
        mapOrder2Head = new HashMap<Long, ObdHeader>();
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("waveId", waveId);
        orderList = orderService.getOutbSoHeaderList(mapQuery);
        Collections.sort(orderList, new Comparator<ObdHeader>() {
            //此处可以设定一个排序规则,对波次中的订单优先级进行排序
            public int compare(ObdHeader o1, ObdHeader o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        boolean bAllAlloc = true;
        for(int i = 0;i  < orderList.size(); ++i){
            mapOrder2Head.put(orderList.get(i).getOrderId(), orderList.get(i));
            List<ObdDetail> details = orderService.getOutbSoDetailListByOrderId(orderList.get(i).getOrderId());
            for(ObdDetail detail : details){
                if(detail.getReleaseQty().compareTo(detail.getOrderQty().multiply(detail.getPackUnit()))<0){
                    bAllAlloc = false;
                }
            }
            orderDetails.addAll(details);
        }
        if(bAllAlloc){
            waveService.setStatus(waveId, WaveConstant.STATUS_RELEASE_SUCC, true);
            throw new BizCheckedException("2040022");
        }
    }

    private void _preparePickModel() throws BizCheckedException{
        //获取捡货模版
        modelTpl = modelService.getPickModelTemplate(waveHead.getPickModelTemplateId());
        modelList = modelService.getPickModelsByTplId(waveHead.getPickModelTemplateId());
        if(modelList.size()==0){
            throw new BizCheckedException("2040006");
        }
        Collections.sort(modelList,new Comparator<PickModel> (){
            //按捡货区权重排序
            public int compare(PickModel arg0, PickModel arg1) {
                return arg1.getPickWeight().compareTo(arg0.getPickWeight());
            }
        });
        //获取分区信息
        zoneList = new LinkedList<WorkZone>();
        mapZone = new HashMap<Long, WorkZone>();
        for(int i = 0; i < modelList.size(); ++i){
            WorkZone zone = workZoneService.getWorkZone(modelList.get(i).getPickZoneId());
            if(zone == null){
                logger.error("get outbound zone fail %d", modelList.get(i).getPickZoneId());
                throw new BizCheckedException("2040023");
            }
            zoneList.add(zone);
            mapZone.put(zone.getZoneId(), zone);
            modelList.get(i).setZone(zone);
        }
        //将捡货分区location详细信息
        mapZone2StoreLocations = new HashMap<Long, List<BaseinfoLocation>>();
        for(PickModel model : modelList) {
            WorkZone zone = mapZone.get(model.getPickZoneId());
            String[] pickLocations = zone.getLocations().split(",");

            List<BaseinfoLocation> locationList = new LinkedList<BaseinfoLocation>();
            for(String loc : pickLocations) {
                if(loc.trim().compareTo("")==0){
                    logger.error("hee");
                    throw new BizCheckedException("2040023");
                }
                locationList.add(locationService.getLocation(Long.valueOf(loc)));
            }
            Collections.sort(locationList, new Comparator<BaseinfoLocation>() {
                //地堆区在前面
                public int compare(BaseinfoLocation o1, BaseinfoLocation o2) {
                    if((o1.getType().equals(LocationConstant.BIN) && o1.getBinUsage().equals(BinUsageConstant.BIN_FLOOR_STORE))
                            || o1.getType().equals(LocationConstant.FLOOR)){
                        //01在前
                        return -1;
                    }else if ((o2.getType().equals(LocationConstant.BIN) && o2.getBinUsage().equals(BinUsageConstant.BIN_FLOOR_STORE)) || o2.getType().equals(LocationConstant.FLOOR_BIN)
                            || o2.getType().equals(LocationConstant.FLOOR)){
                        return 1;
                    }else{
                        return 0;
                    }
                }
            });
            mapZone2StoreLocations.put(zone.getZoneId(), locationList);
        }
    }

    private void _prepareX(){
    }

    private BaseinfoItemLocation _getPickLocation(BaseinfoItem item, BaseinfoLocation location){
        /*
        这其实应该有一个捡货位分配算法.
        地堆区的捡货是个蛋疼的问题.
         */
        String key = String.format("%d-%d", item.getItemId(), location.getLocationId());
        List<BaseinfoItemLocation> pickLocations = mapItemAndPickZone2PickLocations.get(key);
        if(pickLocations == null) {
            List<BaseinfoItemLocation> pickLocationList = new ArrayList<BaseinfoItemLocation>();
            final List<BaseinfoItemLocation> itemLocationList = itemLocationService.getItemLocationList(item.getItemId());
            //判断此区域是否有对应的捡货位
            for(BaseinfoItemLocation pickLocation : itemLocationList) {
                if (pickLocation.getPickLocationid() >= location.getLeftRange()
                        && pickLocation.getPickLocationid() <= location.getRightRange()) {
                    pickLocationList.add(pickLocation);
                }
            }
            /*
            if(pickLocationList.size()==0 &&
                    ((location.getType().equals(LocationConstant.BIN) && location.getBinUsage().equals(BinUsageConstant.BIN_FLOOR_STORE)) || location.getType().equals(LocationConstant.FLOOR)
                            || location.getType().equals(LocationConstant.FLOOR_BIN)) ) {
                //地堆区不设置也可以直接捡货
                BaseinfoItemLocation itemLocation = new BaseinfoItemLocation();
                itemLocation.setItemId(item.getItemId());
                itemLocation.setPickLocationid(location.getLocationId());
                itemLocation.setPickLocationType(0);
                pickLocationList.add(itemLocation);
            }*/
            mapItemAndPickZone2PickLocations.put(key, pickLocationList);
            pickLocations = pickLocationList;
        }
        if(pickLocations.size()==0){
            return null;
        }
        if(mapItemAndPickZone2PickLocationRound.get(key) == null){
            mapItemAndPickZone2PickLocationRound.put(key, 0L);
        }
        Long round = mapItemAndPickZone2PickLocationRound.get(key);
        mapItemAndPickZone2PickLocationRound.put(key, round+1);
        return pickLocations.get((int)(round%pickLocations.size()));
    }

    private BigDecimal _getPickZoneLeftAllocQty(BaseinfoItem item, BaseinfoLocation location){
        //锁库存.怎么锁??
        String key = String.format("%d-%d", item.getItemId(), location.getLocationId());
        BigDecimal leftQty = mapPickZoneLeftAllocQty.get(key);
        if ( leftQty == null ) {
            /*存捡合一类型,无补货机制,需要判断具体捡货位的数量来做分配*/
            leftQty = waveService.getUnAllocQty(item.getItemId(), location);
            mapPickZoneLeftAllocQty.put(key, leftQty);
        }
        return leftQty;
    }

    private long[] getBestCutPlan(long num, long maxPer){
        int needNum = (int)Math.ceil(num/(float)maxPer);
        long []bestCutPlan = new long[needNum];
        for(int i = 0;i < needNum-1; ++i){
            bestCutPlan[i] = maxPer;
        }
        bestCutPlan[needNum-1] = num - maxPer*(needNum-1);
        if(needNum>1) {
            while(bestCutPlan[needNum-1] < bestCutPlan[needNum-2]-1) {
                int idx = needNum - 1;
                while (idx > 0 && bestCutPlan[needNum - 1] < bestCutPlan[needNum - 2]) {
                    bestCutPlan[needNum-1]++;
                    bestCutPlan[idx]--;
                }
            }
        }
        return bestCutPlan;
    }

}
