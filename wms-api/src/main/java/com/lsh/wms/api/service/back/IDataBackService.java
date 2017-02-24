package com.lsh.wms.api.service.back;


import com.lsh.wms.api.model.po.IbdBackRequest;
import com.lsh.wms.api.model.wumart.CreateIbdHeader;
import com.lsh.wms.api.model.wumart.CreateObdHeader;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.datareport.SkuMap;
import com.lsh.wms.model.stock.OverLossReport;
import com.lsh.wms.model.system.SysLog;

import java.util.List;
import java.util.Map;

/**
 * Created by lixin-mac on 16/9/6.
 */

public interface IDataBackService {
    String wmDataBackByPost(String request, String url , Integer type,SysLog sysLog);
    String ofcDataBackByPost(String request, String url,SysLog sysLog);
    Boolean erpDataBack(CreateIbdHeader createIbdHeader,SysLog sysLog);
    Boolean erpOvLosserBack(OverLossReport overLossReport,SysLog sysLog);
    Boolean erpBackCommodityData(BaseinfoItem baseinfoItem,SysLog sysLog);

    Boolean obd2Erp(CreateObdHeader createObdHeader,SysLog sysLog);


    List<SkuMap> skuMapFromErp(List<String> skuCodes);
}
