package com.lsh.wms.service.receive;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.wms.api.service.back.IDataBackService;
import com.lsh.wms.api.service.po.IReceiveRestService;
import com.lsh.wms.api.service.request.RequestUtils;
import com.lsh.wms.core.service.location.BaseinfoLocationWarehouseService;
import com.lsh.wms.core.service.po.ReceiveService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by lixin-mac on 2016/10/21.
 */

@Service(protocol = "rest")
@Path("receive")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class ReceiveRestService implements IReceiveRestService{

    @Autowired
    private ReceiveRpcService receiveRpcService;

    @Reference
    private IDataBackService dataBackService;

    @Autowired
    private BaseinfoLocationWarehouseService baseinfoLocationWarehouseService;

    @Autowired
    private ReceiveService receiveService;

//    @Reference
//    private IWuMartSap wuMartSap;
//    @Reference
//    private IWuMart wuMart;

    @POST
    @Path("getReceiveHeaderList")
    public String getReceiveHeaderList(Map<String, Object> params) {

        return JsonUtils.SUCCESS(receiveRpcService.getReceiveHeaderList(params));
    }

    @POST
    @Path("countReceiveHeader")
    public String countReceiveHeader(Map<String, Object> params) {
        return JsonUtils.SUCCESS(receiveRpcService.countReceiveHeader(params));
    }

    @GET
    @Path("getReceiveDetailList")
    public String getReceiveDetailList(@QueryParam("receiveId") Long receiveId) {
        return JsonUtils.SUCCESS(receiveRpcService.getReceiveDetailList(receiveId));
    }


    @POST
    @Path("updateOrderStatus")
    public String updateOrderStatus() throws BizCheckedException {
        Map<String, Object> map = RequestUtils.getRequest();

        receiveRpcService.updateOrderStatus(map);

        //确认收货之后将验收单回传到上游系统
//        if("5".equals(map.get("orderStatus").toString())){
//            List<ReceiveDetail> receiveDetails = receiveService.getReceiveDetailListByReceiveId((Long) map.get("receiveId"));
//            ReceiveHeader receiveHeader = receiveService.getReceiveHeaderByReceiveId((Long) map.get("receiveId"));
//            // TODO: 2016/11/3 回传WMSAP 组装信息
//            CreateIbdHeader createIbdHeader = new CreateIbdHeader();
//            List<CreateIbdDetail> details = new ArrayList<CreateIbdDetail>();
//            for(ReceiveDetail receiveDetail : receiveDetails){
//                CreateIbdDetail detail = new CreateIbdDetail();
//                detail.setPoNumber(receiveHeader.getOrderOtherId());
//                detail.setPoItme(receiveDetail.getDetailOtherId());
//                BigDecimal inboudQty =  receiveDetail.getInboundQty();
//
//                BigDecimal orderQty = receiveDetail.getOrderQty();
//                BigDecimal deliveQty = receiveHeader.getOrderType().equals(3) ? orderQty : inboudQty;
//                if(deliveQty.compareTo(BigDecimal.ZERO) <= 0){
//                    continue;
//                }
//                detail.setDeliveQty(deliveQty.setScale(2,BigDecimal.ROUND_HALF_UP));
//                detail.setUnit(receiveDetail.getUnitName());
//                detail.setMaterial(receiveDetail.getSkuCode());
//                detail.setOrderType(receiveHeader.getOrderType());
//                detail.setVendMat(receiveHeader.getReceiveId().toString());
//
//                details.add(detail);
//            }
//            createIbdHeader.setItems(details);
//
//            if(receiveHeader.getOwnerUid() == 1){
//                wuMart.sendIbd(createIbdHeader);
//
//            }else{
//                dataBackService.erpDataBack(JSON.toJSONString(createIbdHeader));
//            }
//        }
        return JsonUtils.SUCCESS();
    }


    @GET
    @Path("accountBack")
    public String accountBack(@QueryParam("receiveId") Long receiveId,@QueryParam("detailOtherId") String detailOtherId) throws BizCheckedException{
        receiveRpcService.accountBack(receiveId,detailOtherId);
        return JsonUtils.SUCCESS();
    }
    @GET
    @Path("getLotByReceiptContainerId")
    public String getLotByReceiptContainerId(@QueryParam("containerId") Long containerId) throws BizCheckedException{
        Long lotId = receiveRpcService.getLotByReceiptContainerId(containerId);
        return JsonUtils.SUCCESS(lotId);
    }

    @POST
    @Path("updateQty")
    public String updateQty() throws BizCheckedException {
        Map<String, Object> request = RequestUtils.getRequest();
        Long uid = Long.valueOf(request.get("uid").toString());
        Long receiveId = (Long) request.get("receiveId");
        String detailOtherId = (String) request.get("detailOtherId");
        String qty = request.get("qty").toString();
        BigDecimal realQty = new BigDecimal(qty);

        receiveRpcService.updateQty(receiveId,detailOtherId,realQty,uid);
        return JsonUtils.SUCCESS();
    }
}
