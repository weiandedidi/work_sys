package com.lsh.wms.core.service.po;

import com.alibaba.fastjson.JSON;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.core.constant.PoConstant;
import com.lsh.wms.core.constant.SysLogConstant;
import com.lsh.wms.core.dao.po.IbdDetailDao;
import com.lsh.wms.core.dao.po.InbReceiptDetailDao;
import com.lsh.wms.core.dao.po.ReceiveDetailDao;
import com.lsh.wms.core.dao.po.ReceiveHeaderDao;
import com.lsh.wms.core.service.persistence.PersistenceManager;
import com.lsh.wms.core.service.persistence.PersistenceProxy;
import com.lsh.wms.core.service.system.ModifyLogService;
import com.lsh.wms.model.po.IbdDetail;
import com.lsh.wms.model.po.InbReceiptDetail;
import com.lsh.wms.model.po.ReceiveDetail;
import com.lsh.wms.model.po.ReceiveHeader;
import com.lsh.wms.model.system.ModifyLog;
import com.lsh.wms.model.system.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by lixin-mac on 2016/10/21.
 */
@Component
@Transactional(readOnly = true)
public class ReceiveService {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveService.class);

    @Autowired
    private ReceiveHeaderDao receiveHeaderDao;

    @Autowired
    private ReceiveDetailDao receiveDetailDao;

    @Autowired
    private InbReceiptDetailDao inbReceiptDetailDao;

    @Autowired
    private IbdDetailDao ibdDetailDao;

    @Autowired
    private PersistenceProxy persistenceProxy;

    @Autowired
    private PersistenceManager persistenceManager;

    @Autowired
    private ModifyLogService modifyLogService;



    /**
     * 根据参数获取receiveHeaderList
     */
    public List<ReceiveHeader> getReceiveHeaderList(Map<String, Object> params){
        return receiveHeaderDao.getReceiveHeaderList(params);
    }

    /**
     * 根据参数获取receiveHeaderList count
     */
    public Integer countReceiveHeader(Map<String, Object> params){
        return receiveHeaderDao.countReceiveHeader(params);
    }

    /**
     * 根据 orderId 获取正常状态的receiveHeader
     */
    public ReceiveHeader getReceiveHeader(Long orderId){
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("orderId",orderId);
        mapQuery.put("orderStatus",1);
        List<ReceiveHeader> list = this.getReceiveHeaderList(mapQuery);
        if(list.size() <= 0){
            return null;
        }
        return list.get(0);
    }
    /**
     * 根据receiveId获取receiveHeader
     */
    public ReceiveHeader getReceiveHeaderByReceiveId(Long receiveId){
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("receiveId",receiveId);
        List<ReceiveHeader> list = this.getReceiveHeaderList(mapQuery);
        if(list == null){
            return null;
        }
        return list.get(0);
    }

    /**
     * 插入receiveHeader及receiveDetail
     * @param receiveHeader
     * @param receiveDetails
     */
    @Transactional(readOnly = false)
    public void insertReceive(ReceiveHeader receiveHeader, List<ReceiveDetail> receiveDetails) {

        receiveHeaderDao.insert(receiveHeader);

        receiveDetailDao.batchInsert(receiveDetails);

    }

    /**
     * 根据OrderId及skuCode获取InbPoDetail
     * @param receiveId
     * @param skuCode
     * @return
     */
    public ReceiveDetail getReceiveDetailByReceiveIdAndSkuCode(Long receiveId, String skuCode) {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("receiveId", receiveId);
        params.put("skuCode", skuCode);

        return getReceiveDetail(params);
    }
    public List<ReceiveDetail> getReceiveDetailListByReceiveIdAndSkuCode(Long receiveId, String skuCode) {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("receiveId", receiveId);
        params.put("skuCode", skuCode);

        List<ReceiveDetail> receiveDetails =  receiveDetailDao.getReceiveDetailList(params);
        if(receiveDetails.size() <= 0){
            return null;
        }
        return receiveDetails;
    }

    /**
     * 根据条件查询receiveDetail
     */
    public ReceiveDetail getReceiveDetail(Map<String,Object> params){
        List<ReceiveDetail> receiveDetails =  receiveDetailDao.getReceiveDetailList(params);
        if(receiveDetails.size() <= 0){
            return null;
        }
        return receiveDetails.get(0);
    }

    /**
     * 获取receiveDetailList
     */
    public List<ReceiveDetail> getReceiveDetailListByReceiveId(Long receiveId){
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("receiveId",receiveId);
        List<ReceiveDetail> receiveDetails = receiveDetailDao.getReceiveDetailList(map);
        if (receiveDetails == null) {
            return new ArrayList<ReceiveDetail>();
        }
        return receiveDetails;
    }


    /**
     * receiveHeader填充receiveDetail
     * @param receiveHeader
     */
    public void fillDetailToHeader(ReceiveHeader receiveHeader) {
        if(receiveHeader == null) {
            return;
        }

        List<ReceiveDetail> receiveDetails = this.getReceiveDetailListByReceiveId(receiveHeader.getReceiveId());

        receiveHeader.setReceiveDetails(receiveDetails);
    }

    /**
     * 修改receiveHeader状态
     */
    @Transactional(readOnly = false)
    public void updateStatus(ReceiveHeader receiveHeader){
        receiveHeader.setUpdatedAt(DateUtils.getCurrentSeconds());
        receiveHeaderDao.update(receiveHeader);
        //查询验收单 正常po以及sto单加入日志表
        ReceiveHeader receiveHeader1 = this.getReceiveHeaderByReceiveId(receiveHeader.getReceiveId());
        if(receiveHeader1.getOrderType() == PoConstant.ORDER_TYPE_PO || receiveHeader1.getOrderType() == PoConstant.ORDER_TYPE_TRANSFERS){
            persistenceProxy.doOne(SysLogConstant.LOG_TYPE_IBD,receiveHeader.getReceiveId(),0);
        }

    }

    /**
     * 根据receive_id 与detail_other_id来修改receive_detail
     */
    @Transactional(readOnly = false)
    public void updateByReceiveIdAndDetailOtherId(ReceiveDetail receiveDetail){
        receiveDetail.setUpdatedAt(DateUtils.getCurrentSeconds());
        receiveDetailDao.updateByReceiveIdAndDetailOtherId(receiveDetail);
    }


    /**
     * 根据OrderId及detailOtherId获取InbPoDetail
     * @param receiveId
     * @param detailOtherId
     * @return
     */
    public ReceiveDetail getReceiveDetailByReceiveIdAnddetailOtherId(Long receiveId, String detailOtherId) {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("receiveId", receiveId);
        params.put("detailOtherId", detailOtherId);

        return getReceiveDetail(params);
    }

    @Transactional(readOnly = false)
    public void updateQty(ReceiveDetail receiveDetail, IbdDetail ibdDetail, ModifyLog modifyLog){
        receiveDetail.setUpdatedAt(DateUtils.getCurrentSeconds());
        logger.info("~~~~~~~~~~~~~~~~~~~1111111 receiveDetail : " + JSON.toJSONString(receiveDetail));
        receiveDetailDao.update(receiveDetail);
        ibdDetail.setUpdatedAt(DateUtils.getCurrentSeconds());
        logger.info("~~~~~~~~~~~~~~~~~~~~2222222 ibdDetail :" + JSON.toJSONString(ibdDetail));
        ibdDetailDao.update(ibdDetail);

        //修改之后将验收单状态修改为正常 可以再次回传。
        ReceiveHeader receiveHeader = this.getReceiveHeaderByReceiveId(receiveDetail.getReceiveId());
        receiveHeader.setOrderStatus(PoConstant.ORDER_YES);
        receiveHeader.setUpdatedAt(DateUtils.getCurrentSeconds());
        receiveHeaderDao.update(receiveHeader);


        modifyLogService.addModifyLog(modifyLog);
//        for (InbReceiptDetail inbReceiptDetail : updateReceiptDetails){
//            inbReceiptDetail.setUpdatetime(new Date());
//            inbReceiptDetailDao.update(inbReceiptDetail);
//        }
//        //重新生成receiptDetail
//        inbReceiptDetailDao.batchInsert(addReceiptDetails);
    }
    /**
     * 根据ibdId
     * ibdDetailId 查找receiveDetail
     * */
    public ReceiveDetail getReceiveDetailByOtherId(String ibdId,String ibdDetailId){
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("ibdId",ibdId);
        mapQuery.put("ibdDetailId",ibdDetailId);
        return this.getReceiveDetail(mapQuery);
    }


    /**
     *根据order_id获取receiveHeaderList
     */
    public List<ReceiveHeader> getReceiveHeaderList(Long orderId){
        Map<String,Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("orderId",orderId);
        List<ReceiveHeader> list = this.getReceiveHeaderList(mapQuery);
        if(list.size() <= 0){
            return null;
        }
        return list;
    }

    @Transactional(readOnly = false)
    public void accountBack(ReceiveHeader receiveHeader,ReceiveDetail detail){

        detail.setBackStatus(PoConstant.RECEIVE_DETAIL_STATUS_FAILED);
        detail.setAccountId("");
        detail.setAccountDetailId("");
        this.updateByReceiveIdAndDetailOtherId(detail);
        //冲销之后重新生成一条回传日志。
        persistenceProxy.doOne(SysLogConstant.LOG_TYPE_IBD,receiveHeader.getReceiveId(),null);


    }



}
