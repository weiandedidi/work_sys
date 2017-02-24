package com.lsh.wms.service.receipt;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.alibaba.fastjson.JSON;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.base.common.utils.StrUtils;
import com.lsh.wms.api.model.base.BaseResponse;
import com.lsh.wms.api.model.base.ResUtils;
import com.lsh.wms.api.model.base.ResponseConstant;
import com.lsh.wms.api.model.po.ReceiptRequest;
import com.lsh.wms.api.service.po.IReceiptRestService;
import com.lsh.wms.api.service.request.RequestUtils;
import com.lsh.wms.core.service.po.PoReceiptService;
import com.lsh.wms.model.po.InbReceiptDetail;
import com.lsh.wms.model.po.InbReceiptHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;


/**
 * Project Name: lsh-wms
 * Created by fuhao
 * Date: 16/7/12
 * Time: 16/7/12.
 * 北京链商电子商务有限公司
 * Package name:com.lsh.wms.service.po.
 * desc:类功能描述
 */
@Service(protocol = "rest")
@Path("order/po/receipt")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class ReceiptRestService implements IReceiptRestService {

    private static Logger logger = LoggerFactory.getLogger(ReceiptRestService.class);

    @Autowired
    private ReceiptRpcService receiptRpcService;

    @Autowired
    private PoReceiptService poReceiptService;

    @POST
    @Path("init")
    public String init(String poReceiptInfo) {
        InbReceiptHeader inbReceiptHeader = JSON.parseObject(poReceiptInfo,InbReceiptHeader.class);
        List<InbReceiptDetail> inbReceiptDetailList = JSON.parseArray((String)inbReceiptHeader.getReceiptDetails(),InbReceiptDetail.class);
        poReceiptService.orderInit(inbReceiptHeader,inbReceiptDetailList);
        return JsonUtils.SUCCESS();
    }

    @POST
    @Path("throw")
    public String throwOrder(String orderOtherId) throws BizCheckedException {
        if(receiptRpcService.throwOrder(orderOtherId)){
            return JsonUtils.SUCCESS();
        }else {
            return JsonUtils.FAIL("2020006");
        }
    }

    @POST
    @Path("insert")
    public BaseResponse insertOrder(ReceiptRequest request) throws BizCheckedException, ParseException {
        receiptRpcService.insertOrder(request);
        return ResUtils.getResponse(ResponseConstant.RES_CODE_1,ResponseConstant.RES_MSG_OK,null);
    }

    @POST
    @Path("updateReceiptStatus")
    public String updateReceiptStatus() throws BizCheckedException {
        Map<String, Object> map = RequestUtils.getRequest();

        receiptRpcService.updateReceiptStatus(map);

        return JsonUtils.SUCCESS();
    }

    @GET
    @Path("getPoReceiptDetailByReceiptId")
    public String getPoReceiptDetailByReceiptId(@QueryParam("receiptId") Long receiptId) throws BizCheckedException {
        InbReceiptHeader inbReceiptHeader = receiptRpcService.getPoReceiptDetailByReceiptId(receiptId);
        return JsonUtils.SUCCESS(inbReceiptHeader);
    }

    @GET
    @Path("getPoReceiptDetailByOrderId")
    public String getPoReceiptDetailByOrderId(@QueryParam("orderId") Long orderId) throws BizCheckedException {
        List<InbReceiptHeader> inbReceiptHeaderList = receiptRpcService.getPoReceiptDetailByOrderId(orderId);
        return JsonUtils.SUCCESS(inbReceiptHeaderList);
    }

    @GET
    @Path("getCpoReceiptDetailByOrderId")
    public String getCpoReceiptDetailByOrderId(@QueryParam("orderId") Long orderId) throws BizCheckedException {
        List<InbReceiptDetail> inbReceiptDetailList = receiptRpcService.getInbReceiptDetailListByOrderId(orderId);
        return JsonUtils.SUCCESS(inbReceiptDetailList);
    }

    @POST
    @Path("countInbPoReceiptHeader")
    public String countInbPoReceiptHeader() {
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.countInbPoReceiptHeader(params));
    }


    @POST
    @Path("getPoReceiptDetailList")
    public String getPoReceiptDetailList() {
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.getInbReceiptHeaderList(params));
    }


    @GET
    @Path("insertPoReceipt")
    public String insertReceipt(@QueryParam("orderId") Long orderId ,@QueryParam("staffId") Long staffId) throws BizCheckedException, ParseException {
        receiptRpcService.insertReceipt(orderId,staffId);
        return JsonUtils.SUCCESS();
    }

    @POST
    @Path("getInbReceiptHeaderDetailList")
    public String getInbReceiptHeaderDetailList() throws BizCheckedException {
        Map<String, Object> params = RequestUtils.getRequest();
        List<InbReceiptHeader> inbReceiptHeaderList = receiptRpcService.getInbReceiptHeaderDetailList(params);
        return JsonUtils.SUCCESS(inbReceiptHeaderList);
    }
    @POST
    @Path("getInbReceiptHeaderList")
    public String getInbReceiptHeaderList() {
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.getInbReceiptHeaderList(params));
    }
    @POST
    @Path("getInbReceiptDetailList")
    public String getInbReceiptDetailList() {
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.getInbReceiptDetailList(params));
    }
    @POST
    @Path("countInbPoReceiptDetail")
    public String countInbPoReceiptDetail(){
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.countInbPoReceiptDetail(params));
    }

    @POST
    @Path("modifyQty")
    public String modifyQty() throws BizCheckedException {
        Map<String, Object> request = RequestUtils.getRequest();
        Long uid = Long.valueOf(request.get("uid").toString());
        Long receiptId = (Long) request.get("receiptId");
        String qty = request.get("qty").toString();
        BigDecimal realQty = new BigDecimal(qty);
        receiptRpcService.modifyQty(receiptId,realQty,uid);
        return JsonUtils.SUCCESS();
    }
    @POST
    @Path("getInbReceiptIds")
    public String getInbReceiptIds() throws BizCheckedException{
        Map<String, Object> params = RequestUtils.getRequest();
        return JsonUtils.SUCCESS(receiptRpcService.getInbReceiptIds(params));
    }

    @POST
    @Path("modifyProTime")
    public String modifyProTime() throws BizCheckedException, ParseException {
        Map<String, Object> request = RequestUtils.getRequest();
        logger.info(StrUtils.formatString("Params is : ",request));
        Long uid = Long.valueOf(request.get("uid").toString());
        Long receiptId = (Long) request.get("receiptId");
        String newProTime = request.get("newProTime").toString();
        receiptRpcService.modifyProTime(receiptId,newProTime,uid);
        return JsonUtils.SUCCESS();
    }


}
