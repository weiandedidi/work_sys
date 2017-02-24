package com.lsh.wms.api.service.po;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.api.model.base.BaseResponse;
import com.lsh.wms.api.model.po.ReceiptRequest;

import java.text.ParseException;
import java.util.Map;

/**
 * Project Name: lsh-wms
 * Created by fuhao
 * Date: 16/7/12
 * Time: 16/7/12.
 * 北京链商电子商务有限公司
 * Package name:com.lsh.wms.api.service.po.
 * desc:类功能描述
 */
public interface IReceiptRestService {

    public String init(String poReceiptInfo);

    public BaseResponse insertOrder(ReceiptRequest request) throws BizCheckedException, ParseException;

    /* 投单接口 */
    public String throwOrder(String orderOtherId) throws BizCheckedException;

    public String updateReceiptStatus() throws BizCheckedException;

    public String getPoReceiptDetailByReceiptId(Long receiptId) throws BizCheckedException;

    public String getPoReceiptDetailByOrderId(Long orderId) throws BizCheckedException;

    public String countInbPoReceiptHeader();

    public String getPoReceiptDetailList();
    String insertReceipt(Long orderId,Long staffId) throws BizCheckedException, ParseException;

    String getCpoReceiptDetailByOrderId(Long orderId) throws BizCheckedException;

    String getInbReceiptHeaderDetailList() throws BizCheckedException;

    String getInbReceiptHeaderList() throws BizCheckedException;

    String getInbReceiptDetailList() throws BizCheckedException;

    String countInbPoReceiptDetail();

    String modifyQty() throws BizCheckedException;

    String getInbReceiptIds() throws BizCheckedException;
    String modifyProTime() throws ParseException;

}
