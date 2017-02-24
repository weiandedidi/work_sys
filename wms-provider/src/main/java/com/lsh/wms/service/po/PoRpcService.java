package com.lsh.wms.service.po;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.lsh.base.common.config.PropertyUtils;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.*;
import com.lsh.wms.api.model.po.PoItem;
import com.lsh.wms.api.model.po.PoRequest;
import com.lsh.wms.api.service.back.IBackInStorageProviderRpcService;
import com.lsh.wms.api.service.po.IPoRpcService;
import com.lsh.wms.api.service.task.ITaskRpcService;
import com.lsh.wms.core.constant.PoConstant;
import com.lsh.wms.core.constant.SoConstant;
import com.lsh.wms.core.service.item.ItemLocationService;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.po.PoOrderService;
import com.lsh.wms.core.service.so.SoOrderService;
import com.lsh.wms.core.service.utils.IdGenerator;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoItemLocation;
import com.lsh.wms.model.po.IbdDetail;
import com.lsh.wms.model.po.IbdHeader;
import com.lsh.wms.model.po.IbdObdRelation;
import com.lsh.wms.model.so.ObdDetail;
import com.lsh.wms.model.so.ObdHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

/**
 * Project Name: lsh-wms
 * Created by fuhao
 * Date: 16/7/8
 * Time: 16/7/8.
 * 北京链商电子商务有限公司
 * Package name:com.lsh.wms.service.po.
 * desc:类功能描述
 */
@Service(protocol = "dubbo")
public class PoRpcService implements IPoRpcService {

    private static Logger logger = LoggerFactory.getLogger(PoRpcService.class);

    @Autowired
    private PoOrderService poOrderService;

    @Autowired
    private SoOrderService soOrderService;

    @Autowired
    private ItemService itemService;

    @Reference
    private ITaskRpcService iTaskRpcService;
    @Autowired
    protected IdGenerator idGenerator;
    @Autowired
    private ItemLocationService itemLocationService;

    @Reference
    private IBackInStorageProviderRpcService backInStorageProviderRpcService;

    public Long insertOrder(PoRequest request) throws BizCheckedException{
        //初始化InbPoHeader
        IbdHeader ibdHeader = new IbdHeader();
        ObjUtils.bean2bean(request, ibdHeader);
        Integer orderType = ibdHeader.getOrderType();


        //新增的状态为待投单
        ibdHeader.setOrderStatus(PoConstant.ORDER_DELIVERY);
        ibdHeader.setCreatedAt(DateUtils.getCurrentSeconds());
        if(PoConstant.ORDER_TYPE_SO_BACK == orderType){
            //返仓单的下单用户
            String orderUser = ibdHeader.getOrderUser();
            //返仓的置为新增
            ibdHeader.setOrderStatus(PoConstant.ORDER_YES);
            //自动下单做校验
            if(request.getAutoDone() == 1){

                if(orderUser == null){
                    throw new BizCheckedException("2020010");
                }
                //使用so orderOtherId 与 type确定so
                ObdHeader soHeader = soOrderService.getOutbSoHeaderByOrderOtherIdAndType(ibdHeader.getOrderOtherRefId(),SoConstant.ORDER_TYPE_SO);
                if(null == soHeader){
                    throw new BizCheckedException("2020001");
                }
            }
        }else{
            String supplierCode = ibdHeader.getSupplierCode();
            if(supplierCode == null){
                throw new BizCheckedException("2020011");
            }
        }


        //设置orderId
        String idKey = "ibd_id";
        ibdHeader.setOrderId(idGenerator.genId(idKey, true, true));

        //初始化List<InbPoDetail>
        List<IbdDetail> ibdDetailList = new ArrayList<IbdDetail>();

        for(PoItem poItem : request.getItems()) {
            IbdDetail ibdDetail = new IbdDetail();

            ObjUtils.bean2bean(poItem, ibdDetail);

            //设置orderId
            ibdDetail.setOrderId(ibdHeader.getOrderId());
//            // 获取skuId
//            // TODO: 16/8/19 取baseinfoItem中的skuId与so单中的skuId 取交集
//            List<Long> soSkuIds = new ArrayList<Long>();
//            List<Long> iSkuIds = new ArrayList<Long>();
//            if(PoConstant.ORDER_TYPE_SO_BACK == orderType){
//                //获取so订单明细
//                List<OutbSoDetail> soDetails =
//                        soOrderService.getOutbSoDetailListByOrderId(Long.parseLong(ibdHeader.getOrderOtherId()));
//                for(OutbSoDetail soDetail : soDetails) {
//                    soSkuIds.add(soDetail.getSkuId());
//                }
//                List<BaseinfoItem>  baseinfoItemList= itemService.getItemsBySkuCode(ibdHeader.getOwnerUid(), ibdDetail.getSkuCode());
//                for (BaseinfoItem item : baseinfoItemList){
//                    iSkuIds.add(item.getSkuId());
//                }
//                soSkuIds.retainAll(iSkuIds);
//                if(soSkuIds.size() > 0){
//                    ibdDetail.setSkuId(soSkuIds.get(0));
//                }
//
//            }
//
//            else{
//                List<BaseinfoItem>  baseinfoItemList= itemService.getItemsBySkuCode(ibdHeader.getOwnerUid(), ibdDetail.getSkuCode());
//                if(null != baseinfoItemList && baseinfoItemList.size()>=1){
//                    BaseinfoItem baseinfoItem = baseinfoItemList.get(baseinfoItemList.size()-1);
//                    ibdDetail.setSkuId(baseinfoItem.getSkuId());
//                }
//            }


            ibdDetailList.add(ibdDetail);
        }

        //插入订单
        poOrderService.insertOrder(ibdHeader, ibdDetailList);
        return ibdHeader.getOrderId();

    }

    public Boolean updateOrderStatus(Map<String, Object> map) throws BizCheckedException {
        //OrderOtherId与OrderId都为NULL 或者 OrderStatus为NULL
        if((map.get("orderOtherId") == null && map.get("orderId") == null)
                || map.get("orderStatus") == null) {
            throw new BizCheckedException("1010001", "参数不能为空");
        }

        if(map.get("orderOtherId") == null && map.get("orderId") != null) {
            if(!StringUtils.isInteger(String.valueOf(map.get("orderId")))) {
                throw new BizCheckedException("1010002", "参数类型不正确");
            }
        }

        if(map.get("orderOtherId") != null && map.get("orderId") == null) {
            if(StringUtils.isBlank(String.valueOf(map.get("orderOtherId")))) {
                throw new BizCheckedException("1010002", "参数类型不正确");
            }
        }

        if(map.get("orderOtherId") != null && map.get("orderId") != null) {
            if(StringUtils.isBlank(String.valueOf(map.get("orderOtherId")))
                    && !StringUtils.isInteger(String.valueOf(map.get("orderId")))) {
                throw new BizCheckedException("1010002", "参数类型不正确");
            }
        }
        if(!StringUtils.isInteger(String.valueOf(map.get("orderStatus")))) {
            throw new BizCheckedException("1010002", "参数类型不正确");
        }
        IbdHeader ibdHeader = new IbdHeader();
        if(map.get("orderOtherId") != null && !StringUtils.isBlank(String.valueOf(map.get("orderOtherId")))) {
            ibdHeader.setOrderOtherId(String.valueOf(map.get("orderOtherId")));
        }
        if(map.get("orderId") != null && StringUtils.isInteger(String.valueOf(map.get("orderId")))) {
            ibdHeader.setOrderId(Long.valueOf(String.valueOf(map.get("orderId"))));
        }
        ibdHeader.setOrderStatus(Integer.valueOf(String.valueOf(map.get("orderStatus"))));
        if(ibdHeader.getOrderStatus() == PoConstant.ORDER_THROW){
            //投单时,记录投单时间
            ibdHeader.setThrowAt(DateUtils.getCurrentSeconds());
            //投单校验
            //checkThrowOrder(ibdHeader.getOrderId());
        }
        poOrderService.updateInbPoHeaderByOrderOtherIdOrOrderId(ibdHeader);

        return true;
    }

    public Map<String,Object> throwOrder(List<Map<String,Object>> idList){
        Map<String,Object> returnMap = new HashMap<String, Object>();
        Set<String> itemNameSet = new HashSet<String>();
        Set<Long> successSet = new HashSet<Long>();
        Set<Long> failedSet = new HashSet<Long>();

        for(Map<String,Object> map : idList){
            Long orderId = Long.valueOf(String.valueOf(map.get("orderId")));
            /*
            投单验证
             */
            IbdHeader ibdHeader = poOrderService.getInbPoHeaderByOrderId(orderId);
            if(ibdHeader == null){
                //throw new BizCheckedException("2020001");
                failedSet.add(orderId);
                continue;
            }
            List<IbdDetail> ibdDetailList = poOrderService.getInbPoDetailListByOrderId(orderId);
            if(ibdDetailList == null || ibdDetailList.size() <= 0){
                failedSet.add(orderId);
                continue;
            }
            boolean isThrow = true;
            for(IbdDetail ibdDetail : ibdDetailList){
                String skuCode = ibdDetail.getSkuCode();
                BaseinfoItem baseinfoItem = itemService.getItemsBySkuCode(ibdHeader.getOwnerUid(),skuCode);
                if(baseinfoItem == null){
                    itemNameSet.add(ibdDetail.getSkuName());
                    failedSet.add(orderId);
                    isThrow = false;
                    continue;
                    //throw new BizCheckedException("2020112",ibdDetail.getSkuName(),"");//商品不存在
                }
                if(baseinfoItem.getIsInfoIntact()==0){
                    itemNameSet.add(ibdDetail.getSkuName());
                    failedSet.add(orderId);
                    isThrow = false;
                    continue;
                    //throw new BizCheckedException("2020110",baseinfoItem.getSkuName(),"");//商品信息不完整
                }
                List<BaseinfoItemLocation> itemLocationList = itemLocationService.getItemLocationList(baseinfoItem.getItemId());
                if(itemLocationList == null || itemLocationList.size() == 0){
                    itemNameSet.add(ibdDetail.getSkuName());
                    failedSet.add(orderId);
                    isThrow = false;
                    continue;
                    //throw new BizCheckedException("2020111",baseinfoItem.getSkuName(),"");//该商品没有拣货位
                }
            }
            if(isThrow){
                successSet.add(orderId);
                updateOrderStatus(map);
            }
        }
        if(failedSet.size() == 0){
            //都投单成功
            return null;
        }else{
            returnMap.put("itemNameList",itemNameSet);
            returnMap.put("successList",successSet);
            returnMap.put("failedList",failedSet);
        }

        return returnMap;
    }
    //投单校验
    private void checkThrowOrder(Long orderId)throws BizCheckedException{
        IbdHeader ibdHeader = poOrderService.getInbPoHeaderByOrderId(orderId);
        if(ibdHeader == null){
            //throw new BizCheckedException("2020001");
        }
        List<IbdDetail> ibdDetailList = poOrderService.getInbPoDetailListByOrderId(orderId);
        if(ibdDetailList == null || ibdDetailList.size() <= 0){
            return;
        }
        for(IbdDetail ibdDetail : ibdDetailList){
            String skuCode = ibdDetail.getSkuCode();
            BaseinfoItem baseinfoItem = itemService.getItemsBySkuCode(ibdHeader.getOwnerUid(),skuCode);
            if(baseinfoItem == null){

                throw new BizCheckedException("2020112",baseinfoItem.getSkuName(),"");//商品不存在
            }
            if(baseinfoItem.getIsInfoIntact()==0){
                throw new BizCheckedException("2020110",baseinfoItem.getSkuName(),"");//商品信息不完整
            }
            List<BaseinfoItemLocation> itemLocationList = itemLocationService.getItemLocationList(baseinfoItem.getItemId());
            if(itemLocationList == null || itemLocationList.size() == 0){
                throw new BizCheckedException("2020111",baseinfoItem.getSkuName(),"");//该商品没有拣货位
            }
        }
        return ;
    }

    public List<IbdHeader> getPoHeaderList(Map<String, Object> params) {
        return poOrderService.getInbPoHeaderList(params);
    }

    public IbdHeader getPoDetailByOrderId(Long orderId) throws BizCheckedException {
        if(orderId == null) {
            throw new BizCheckedException("1010001", "参数不能为空");
        }

        IbdHeader ibdHeader = poOrderService.getInbPoHeaderByOrderId(orderId);

        poOrderService.fillDetailToHeader(ibdHeader);

        return ibdHeader;
    }

    public Integer countInbPoHeader(Map<String, Object> params) {
        return poOrderService.countInbPoHeader(params);
    }

    public List<IbdHeader> getPoDetailList(Map<String, Object> params) {
        List<IbdHeader> ibdHeaderList = poOrderService.getInbPoHeaderList(params);

        poOrderService.fillDetailToHeaderList(ibdHeaderList);

        return ibdHeaderList;
    }


    public void canReceipt(Map<String, Object> map){
        IbdDetail ibdDetail = BeanMapTransUtils.map2Bean(map,IbdDetail.class);
        poOrderService.updateInbPoDetail(ibdDetail);
    }

    /**
     * 根据门店Id来找对应的ibd
     */
    public List<IbdHeader> getIbdHeader(String storeCode) throws BizCheckedException{
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("deliveryCode",storeCode);
        mapQuery.put("orderType", SoConstant.ORDER_TYPE_STO);
        mapQuery.put("orderStatus",1);
        List<ObdHeader> list = soOrderService.getOutbSoHeaderList(mapQuery);
        if(list.size() <= 0){
            throw new BizCheckedException("2020100");
        }
        for(ObdHeader obdHeader : list){


        }

        return null;
    }
    /**
     * 根据po找门店
     */
    public Set<ObdHeader> getObdHeader(String ibdOtherId){
        List<IbdObdRelation> list = poOrderService.getIbdObdRelationByIbdOtherId(ibdOtherId);

        Set<ObdHeader> obdSet = new HashSet<ObdHeader>();

        for(IbdObdRelation ibdObdRelation : list){
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("orderOtherId",ibdObdRelation.getObdOtherId());
            ObdHeader obdheader = soOrderService.getOutbSoHeaderList(map).get(0);
            obdSet.add(obdheader);
        }

        return obdSet;
    }

    public List<Map<String,Object>> getStoreInfo(Long orderId, String detailOtherId) {
        IbdHeader ibdHeader = poOrderService.getInbPoHeaderByOrderId(orderId);

        String orderOtherId = ibdHeader.getOrderOtherId();
        List<IbdObdRelation> relations = poOrderService.getIbdObdRelationListByIbd(orderOtherId,detailOtherId);

        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        for(IbdObdRelation ibdObdRelation : relations){
            ObdHeader obdHeader = soOrderService.getOutbSoHeaderByOrderOtherIdAndType(ibdObdRelation.getObdOtherId(),SoConstant.ORDER_TYPE_DIRECT);

            ObdDetail obdDetail = soOrderService.getObdDetailByOrderIdAndDetailOtherId(obdHeader.getOrderId(),ibdObdRelation.getObdDetailId());

            Map<String,Object> map = new HashMap<String, Object>();
            map.put("skuName",obdDetail.getSkuName());
            map.put("skuCode",obdDetail.getSkuCode());
            map.put("storeCode",obdHeader.getDeliveryCode());
            map.put("storeName",obdHeader.getDeliveryName());
            map.put("orderQty",obdDetail.getOrderQty());
            map.put("packName",obdDetail.getPackName());
            map.put("unitQty",obdDetail.getUnitQty());
            list.add(map);

        }
        return list;
    }
    /**
      修改投单状态,由待收货改为待投单
     intervalTime 修改间隔时间,单位小时
     */
    public void updateStatusTOthrow(Long intervalTime){
        Long time = intervalTime * 60 * 60;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("oldOrderStatus1", PoConstant.ORDER_THROW);
        params.put("oldOrderStatus2", PoConstant.ORDER_RECTIPTING);
        params.put("newOrderStatus",PoConstant.ORDER_DELIVERY);
        params.put("throwAt",time);
        params.put("orderType",PoConstant.ORDER_TYPE_CPO);//直流
        poOrderService.updateStatusTOthrow(params);
    }

    public void closeIbdOrder() {
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("orderType",PoConstant.ORDER_TYPE_PO);
        mapQuery.put("orderStatus",PoConstant.ORDER_RECTIPTING);
        List<IbdHeader> ibdHeaderList = poOrderService.getInbPoHeaderList(mapQuery);
        List<IbdHeader> updateIbdHeaders = new ArrayList<IbdHeader>();
        //记录下修改过的订单
        List<Long> orderIds = new ArrayList<Long>();
        for (IbdHeader ibdHeader : ibdHeaderList){
            Long orderId = ibdHeader.getOrderId();
            List<IbdDetail> ibdDetails = poOrderService.getInbPoDetailListByOrderId(orderId);
            Boolean flag = true;
            for (IbdDetail ibdDetail : ibdDetails) {
                //订货单位转换为EA来计算
                BigDecimal orderUnitQty = PackUtil.UomQty2EAQty(ibdDetail.getOrderQty(),ibdDetail.getPackUnit());
                BigDecimal inboundQty = ibdDetail.getInboundQty();
                if(orderUnitQty.compareTo(inboundQty) > 0){
                    flag = false;
                    break;
                }
            }
            if(flag){
                IbdHeader updateIbdHeader = new IbdHeader();
                updateIbdHeader.setOrderId(orderId);
                updateIbdHeader.setOrderStatus(PoConstant.ORDER_RECTIPT_ALL);
                orderIds.add(orderId);
                updateIbdHeaders.add(updateIbdHeader);
            }
        }
        logger.info(StrUtils.formatString("modify ibdHeader orderStatus include {0}",orderIds.toString()));
        //批量修改状态
        poOrderService.batchUpdateIbdHeaderByOrderId(updateIbdHeaders);

    }

}
