package com.lsh.wms.service.receive;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.StrUtils;
import com.lsh.wms.api.service.po.IReceiveRpcService;
import com.lsh.wms.api.service.wumart.IWuMart;
import com.lsh.wms.api.service.wumart.IWuMartSap;
import com.lsh.wms.core.constant.PoConstant;
import com.lsh.wms.core.service.po.PoOrderService;
import com.lsh.wms.core.service.po.PoReceiptService;
import com.lsh.wms.core.service.po.ReceiveService;
import com.lsh.wms.core.service.system.SysUserService;
import com.lsh.wms.model.po.*;
import com.lsh.wms.model.system.ModifyLog;
import com.lsh.wms.model.system.SysUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lixin-mac on 2016/10/21.
 */
@Service(protocol = "dubbo")
public class ReceiveRpcService implements IReceiveRpcService{

    @Autowired
    private ReceiveService receiveService;

    @Reference
    private IWuMart wuMart;

    @Autowired
    private PoOrderService poOrderService;

    @Autowired
    private PoReceiptService receiptService;

    @Autowired
    private SysUserService sysUserService;

    @Reference
    private IWuMartSap wuMartSap;

    public List<ReceiveHeader> getReceiveHeaderList(Map<String, Object> params) {
        return receiveService.getReceiveHeaderList(params);
    }

    public Integer countReceiveHeader(Map<String, Object> params) {
        return receiveService.countReceiveHeader(params);
    }

    public ReceiveHeader getReceiveDetailList(Long receiveId) {


        if (receiveId == null) {
            throw new BizCheckedException("1020001", "参数不能为空");
        }

        ReceiveHeader receiveHeader = receiveService.getReceiveHeaderByReceiveId(receiveId);

        receiveService.fillDetailToHeader(receiveHeader);

        return receiveHeader;
    }

    public Boolean updateOrderStatus(Map<String, Object> map) throws BizCheckedException {
        ReceiveHeader receiveHeader = new ReceiveHeader();
        receiveHeader.setOrderStatus(Integer.valueOf(String.valueOf(map.get("orderStatus"))));
        receiveHeader.setReceiveId(Long.valueOf(String.valueOf(map.get("receiveId"))));
        receiveService.updateStatus(receiveHeader);
        return true;
    }


    public void updateQty(Long receiveId, String detailOtherId, BigDecimal qty,Long uid) throws BizCheckedException {

        SysUser user = sysUserService.getSysUserByUid(uid.toString());

        //获取receiveHeader
        ReceiveHeader receiveHeader = receiveService.getReceiveHeaderByReceiveId(receiveId);
        ReceiveDetail receiveDetail = receiveService.getReceiveDetailByReceiveIdAnddetailOtherId(receiveId,detailOtherId);


        if(receiveHeader.getOrderStatus() != PoConstant.ORDER_RECTIPT_ALL){
            throw new BizCheckedException("2028888");
        }
        //原数量 inboundqty
        BigDecimal inBoundQty = receiveDetail.getInboundQty();
        receiveDetail.setInboundQty(qty);
        BigDecimal subQty = inBoundQty.subtract(qty);
        //查询ibdHeader 修改实收数量
        //IbdHeader ibdHeader = poOrderService.getInbPoHeaderByOrderId(receiveHeader.getOrderId());
        IbdDetail ibdDetail = poOrderService.getInbPoDetailByOrderIdAndDetailOtherId(receiveHeader.getOrderId(),detailOtherId);
        if(ibdDetail.getInboundQty().subtract(subQty).compareTo(ibdDetail.getOrderQty().multiply(ibdDetail.getPackUnit())) > 0){
            throw new BizCheckedException("2020005");
        }
        ibdDetail.setInboundQty(ibdDetail.getInboundQty().subtract(subQty));

        ModifyLog modifyLog = new ModifyLog();
        modifyLog.setBusinessId(receiveId);
        modifyLog.setDetailId(detailOtherId);
        modifyLog.setModifyType(1);
        modifyLog.setOperator(uid);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(new Date());
        String remark = StrUtils.formatString("用户{0}将数量由{1}改为{2},修改时间为:{3}",user.getUsername(),inBoundQty,qty,date);
        modifyLog.setModifyMessage(remark);

//        //查询有效的收货明细
//        List<InbReceiptDetail> receiptDetails = receiptService.getInbReceiptDetailListByOrderIdAndCode(receiveHeader.getOrderId(),receiveDetail.getCode(),ReceiptContant.RECEIPT_YES);
//        List<InbReceiptDetail> updateReceiptDetails = new ArrayList<InbReceiptDetail>();
//
//        List<InbReceiptDetail> addReceiptDetails = new ArrayList<InbReceiptDetail>();
//
//        for(InbReceiptDetail detail : receiptDetails){
//            InbReceiptDetail inbReceiptDetail = new InbReceiptDetail();
//            BigDecimal receiptQty = detail.getInboundQty();
//            BigDecimal sourceQty = detail.getInboundQty();
//            String remark = StrUtils.formatString("用户{0}修改数量,将收货单{1}下明细作废作废,重新生成",user.getUsername(),detail.getReceiptOrderId());
//            detail.setIsValid(ReceiptContant.RECEIPT_CANCLE);
//            //detail.setInboundQty(BigDecimal.ZERO);
//            detail.setUpdateby(uid.toString());
//            detail.setUpdatetime(new Date());
//            detail.setRemark(remark);
//            updateReceiptDetails.add(detail);
//            if(sourceQty.subtract(subQty).compareTo(BigDecimal.ZERO) < 0 ){
//                ObjUtils.bean2bean(detail,inbReceiptDetail);
//                remark = StrUtils.formatString("用户{0}修改数量,重新生成",user.getUsername());
//                inbReceiptDetail.setRemark(remark);
//                inbReceiptDetail.setInboundQty(BigDecimal.ZERO);
//                inbReceiptDetail.setIsValid(ReceiptContant.RECEIPT_YES);
//                subQty = subQty.subtract(sourceQty);
//                addReceiptDetails.add(inbReceiptDetail);
//            }else{
//                ObjUtils.bean2bean(detail,inbReceiptDetail);
//                remark = StrUtils.formatString("用户{0}修改数量,重新生成",user.getUsername());
//                inbReceiptDetail.setRemark(remark);
//                inbReceiptDetail.setInboundQty(BigDecimal.ZERO);
//                inbReceiptDetail.setIsValid(ReceiptContant.RECEIPT_YES);
//                inbReceiptDetail.setInboundQty(receiptQty.subtract(subQty));
//                addReceiptDetails.add(inbReceiptDetail);
//                break;
//            }
//        }

        receiveService.updateQty(receiveDetail,ibdDetail,modifyLog);

    }


    public void accountBack(Long receiveId, String detailOtherId) throws BizCheckedException {
        //获取receiveHeader
        ReceiveHeader receiveHeader = receiveService.getReceiveHeaderByReceiveId(receiveId);
        ReceiveDetail detail = receiveService.getReceiveDetailByReceiveIdAnddetailOtherId(receiveId,detailOtherId);

        if (receiveHeader.getOrderStatus() == PoConstant.ORDER_RECTIPT_ALL){
            String result = wuMartSap.ibd2SapBack(detail.getAccountId(),detail.getAccountDetailId());
        }
        receiveService.accountBack(receiveHeader,detail);
    }

    public Long getLotByReceiptContainerId(Long containerId) throws BizCheckedException {
        if(containerId.equals(0L)){
            return 0L;
        }
        //根据托盘码查找 InbReceiptHeader
        Map<String,Object> queryMap = new HashMap<String, Object>();
        queryMap.put("containerId",containerId);
        InbReceiptHeader receiptHeader = receiptService.getInbReceiptHeaderByParams(queryMap);
        if(receiptHeader==null){
            return 0L;
        }
        List<InbReceiptDetail> details = receiptService.getInbReceiptDetailListByReceiptId(receiptHeader.getReceiptOrderId());
        return details.get(0).getLotId();
    }

}
