package com.lsh.wms.rf.service.inhouse;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.wms.api.service.item.IItemRpcService;
import com.lsh.wms.api.service.location.ILocationRpcService;
import com.lsh.wms.api.service.request.RequestUtils;
import com.lsh.wms.api.service.stock.IStockQuantRfRestService;
import com.lsh.wms.api.service.stock.IStockQuantRpcService;
import com.lsh.wms.core.constant.CsiConstan;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiSku;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.stock.StockQuantCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mali on 16/8/2.
 */
@Service(protocol = "rest")
@Path("inhouse/stock")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class StockQuantRfRestService implements IStockQuantRfRestService {

    private static Logger logger = LoggerFactory.getLogger(StockQuantRfRestService.class);

    @Reference
    private IStockQuantRpcService stockQuantRpcService;

    @Reference
    private IItemRpcService itemRpcService;

    @Reference
    private ILocationRpcService locationRpcService;

    @POST
    @Path("getItem")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON})
    @Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
    public String getItemByLocation() throws BizCheckedException {
        Map<String, Object> params = RequestUtils.getRequest();
        String locationCode = params.get("locationCode").toString();
        Long locationId = locationRpcService.getLocationIdByCode(locationCode);
        BaseinfoLocation location;
        try {
            location = locationRpcService.getLocation(locationId);
        } catch (BizCheckedException e) {
            throw new BizCheckedException("2060012");
        }
        if (location == null) {
            throw new BizCheckedException("2060012");
        }
        String barCode = params.get("barcode").toString();
        CsiSku csiSku = itemRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, barCode);
        if (csiSku == null) {
            throw new BizCheckedException("2550032");
        }
        StockQuantCondition condition = new StockQuantCondition();
        condition.setLocationId(locationId);
        condition.setSkuId(csiSku.getSkuId());
        List<StockQuant> quantList = stockQuantRpcService.getQuantList(condition);
        if (quantList.isEmpty()) {
            throw new BizCheckedException("2550032");
        }
        StockQuant quant = quantList.get(0);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("itemId", quant.getItemId());
        m.put("name", csiSku.getSkuName());
        m.put("packName", quant.getPackName());
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        result.put("info", m);
        return JsonUtils.SUCCESS(result);
    }
}