package com.lsh.wms.integration.service.back;

import com.alibaba.fastjson.JSON;
import com.lsh.base.common.config.PropertyUtils;
import com.lsh.wms.api.model.so.ObdOfcBackRequest;
import com.lsh.wms.api.model.so.ObdOfcItem;
import com.lsh.wms.core.constant.IntegrationConstan;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.location.LocationService;
import com.lsh.wms.core.service.so.SoDeliveryService;
import com.lsh.wms.core.service.so.SoOrderService;
import com.lsh.wms.core.service.utils.SkuUtil;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.so.ObdHeader;
import com.lsh.wms.model.so.OutbDeliveryDetail;
import com.lsh.wms.model.so.OutbDeliveryHeader;
import com.lsh.wms.model.system.SysLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lixin-mac on 2016/11/17.
 */
@Component
public class ObdOfcTransporter implements ITransporter{

    @Autowired
    private BuildDataRpcService buildDataRpcService;
    @Autowired
    private SoDeliveryService soDeliveryService;
    @Autowired
    private SoOrderService soOrderService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private DataBackService dataBackService;
    @Autowired
    private LocationService locationService;

    public void process(SysLog sysLog) {
        //buildDataRpcService.BuildLshOfcObdData(sysLog.getBusinessId());

        OutbDeliveryHeader header = soDeliveryService.getOutbDeliveryHeaderByDeliveryId(sysLog.getBusinessId());
        List<OutbDeliveryDetail> details = soDeliveryService.getOutbDeliveryDetailListByDeliveryId(sysLog.getBusinessId());
        if(header == null){
            //error
        }
        if(details == null || details.size() == 0) {
            //error
        }
        ObdOfcBackRequest request = new ObdOfcBackRequest();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String now = sdf.format(date);
        request.setWms(2);//该字段写死 2
        request.setDeliveryTime(now);
        request.setObdCode(header.getOrderId().toString());
        ObdHeader obdHeader = soOrderService.getOutbSoHeaderByOrderId(header.getOrderId());
        request.setSoCode(obdHeader.getOrderOtherId());
        request.setWaybillCode(header.getTuId());//运单号
        request.setBoxNum(header.getBoxNum().intValue());
        request.setTurnoverBoxNum(header.getTurnoverBoxNum().intValue());
        //获取仓库名称
        String warehouseCode = locationService.getWarehouseLocation().getLocationCode();
        //request.setWarehouseCode(PropertyUtils.getString("wumart.werks"));
        request.setWarehouseCode(warehouseCode);
        List<ObdOfcItem> items = new ArrayList<ObdOfcItem>();
        for (OutbDeliveryDetail detail : details) {
            BaseinfoItem baseinfoItem = itemService.getItem(detail.getItemId());
            ObdOfcItem item = new ObdOfcItem();
            //取基础数据中的箱规
            item.setPackNum(baseinfoItem.getPackUnit());
            BigDecimal outQty = detail.getDeliveryNum();
            item.setSkuQty(outQty);
            item.setSupplySkuCode(SkuUtil.getSkuCode(baseinfoItem.getSkuCode()));
            items.add(item);
        }
        request.setDetails(items);

        dataBackService.ofcDataBackByPost(JSON.toJSONString(request), PropertyUtils.getString("url_lshofc_obd"),sysLog);



    }
}
