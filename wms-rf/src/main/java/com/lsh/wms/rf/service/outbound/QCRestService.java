package com.lsh.wms.rf.service.outbound;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.alibaba.fastjson.JSON;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.api.service.container.IContainerRpcService;
import com.lsh.wms.api.service.csi.ICsiRpcService;
import com.lsh.wms.api.service.item.IItemRpcService;
import com.lsh.wms.api.service.location.ILocationRpcService;
import com.lsh.wms.api.service.pick.IQCRpcService;
import com.lsh.wms.api.service.pick.IRFQCRestService;
import com.lsh.wms.api.service.request.RequestUtils;
import com.lsh.wms.api.service.so.ISoRpcService;
import com.lsh.wms.api.service.task.ITaskRpcService;
import com.lsh.wms.core.constant.CsiConstan;
import com.lsh.wms.core.constant.PickConstant;
import com.lsh.wms.core.constant.TaskConstant;
import com.lsh.wms.core.constant.WaveConstant;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.stock.StockQuantService;
import com.lsh.wms.core.service.system.SysUserService;
import com.lsh.wms.core.service.task.BaseTaskService;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.baseinfo.BaseinfoContainer;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiSku;
import com.lsh.wms.model.so.ObdHeader;
import com.lsh.wms.model.system.SysUser;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.wave.WaveDetail;
import com.lsh.wms.model.wave.WaveQcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zengwenjun on 16/7/30.
 */


@Service(protocol = "rest")
@Path("outbound/qc")
@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class QCRestService implements IRFQCRestService {
    private static Logger logger = LoggerFactory.getLogger(QCRestService.class);
    @Reference
    private ICsiRpcService csiRpcService;
    @Autowired
    private WaveService waveService;
    @Reference
    private IItemRpcService itemRpcService;
    @Reference
    private ITaskRpcService iTaskRpcService;
    @Autowired
    private BaseTaskService baseTaskService;
    @Reference
    private IContainerRpcService iContainerRpcService;
    @Reference
    private ISoRpcService iSoRpcService;
    @Reference
    private ILocationRpcService iLocationRpcService;
    @Reference
    private IQCRpcService iqcRpcService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ItemService itemService;

    /**
     * 扫码获取qc任务详情
     * 输入捡货签或者托盘嘛,捡货签优先,托盘码其次
     *
     * @return
     * @throws BizCheckedException
     */
    @POST
    @Path("scan")
    public String scan() throws BizCheckedException {
        Map<String, Object> mapRequest = RequestUtils.getRequest();
        Long pickTaskId = null;
        TaskInfo pickTaskInfo = null;
        Long containerId = null;
        TaskInfo qcTaskInfo = null;
        boolean isDirect = false;   //直流跳过明细qc的开关,true是跳过
        //判断是拣货签还是托盘码
        //pickTaskId拣货签12开头,18位的长度
        String code = (String) mapRequest.get("code");


/*        //获取QC任务
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("containerId", Long.valueOf(code));

        //未完成qc查询
        List<Long> statusList = new ArrayList<Long>();
        statusList.add(TaskConstant.Draft);
        statusList.add(TaskConstant.Assigned);
        statusList.add(TaskConstant.Allocated);
        mapQuery.put("statusList", statusList);
        mapQuery.put("type", TaskConstant.TYPE_QC);
//        List<TaskEntry> tasks = iTaskRpcService.getTaskHeadList(TaskConstant.TYPE_QC, mapQuery);
        List<TaskInfo> tasks = baseTaskService.getUnDoneTask(mapQuery);

        //查已完成,先去wave_detail中必须task和wave_detail能对应的上
        if (null == tasks || tasks.size() < 1){
            Map<String, Object> doneQuery = new HashMap<String, Object>();
            doneQuery.put("type",TaskConstant.TYPE_QC);
            doneQuery.put("containerId",TaskConstant.Done);
        }

        //都完成回溯的话,要保证,当下的wave_detail中只有一个托盘生命周期是活着的
        if (null == tasks || tasks.size() < 1) {
            pickTaskId = Long.valueOf(code);
            pickTaskInfo = baseTaskService.getTaskInfoById(pickTaskId);
            if (null == pickTaskInfo) {
                throw new BizCheckedException("2120007");
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("containerId", pickTaskInfo.getContainerId());
            params.put("statusList", statusList);
            params.put("type", TaskConstant.TYPE_QC);
            tasks = baseTaskService.getTaskInfoList(params);
        }
        //检验
        if (null == tasks || tasks.size() == 0) {
            throw new BizCheckedException("2120007");
        }
        if (tasks != null && tasks.size() > 1) {
            throw new BizCheckedException("2120006");
        }*/

        List<WaveDetail> details = waveService.getDetailsByContainerId(Long.valueOf(code));    //一个托盘上是一个货主的货,需要taskId和container两个去确认了,因为肯定有任务不唯一,加生命周期
        if (null == details || details.isEmpty()) {
            details = waveService.getDetailsByPickTaskId(Long.valueOf(code));
        }

        if (null == details || details.isEmpty()) {
            throw new BizCheckedException("2120029");
        }
        Set<Long> qcTaskIds = new HashSet<Long>();
        Set<Long> pickTaskIds = new HashSet<Long>();
        for (WaveDetail detail : details) {
            qcTaskIds.add(detail.getQcTaskId());
            pickTaskIds.add(detail.getPickTaskId());
        }

        if (pickTaskIds.size() > 1) {
            throw new BizCheckedException("2120026");
        }

        if (qcTaskIds.size() > 1) {
            throw new BizCheckedException("2120027");
        }

        //一个也没有就是没生成QC
        if (!qcTaskIds.iterator().hasNext()) {
            throw new BizCheckedException("2120029");
        }

        qcTaskInfo = baseTaskService.getTaskInfoById(qcTaskIds.iterator().next());
        pickTaskId = qcTaskInfo.getQcPreviousTaskId();
        containerId = qcTaskInfo.getContainerId();

        //显示qc的进行状态
        int step = qcTaskInfo.getStep();

        if (qcTaskInfo.getStatus() == TaskConstant.Draft) {
            iTaskRpcService.assign(qcTaskInfo.getTaskId(), Long.valueOf(RequestUtils.getHeader("uid")));
        }
        if (details.size() == 0) {
            //空托盘
            throw new BizCheckedException("2120005");
        }
        //merge item_id 2 pick  qty
        Map<Long, BigDecimal> mapItem2PickQty = new HashMap<Long, BigDecimal>();
        Map<Long, WaveDetail> mapItem2WaveDetail = new HashMap<Long, WaveDetail>();
        //qc完成后的回显示qc的数量的接口
        Map<Long, BigDecimal> mapItem2QCQty = new HashMap<Long, BigDecimal>();
        //计算是拣货量,还是其他货量
        TaskInfo beforeTask = iTaskRpcService.getTaskInfo(qcTaskInfo.getQcPreviousTaskId());    //qc前一个任务量
        boolean isFirstQC = false;  //是否是第一次QC
        //聚类,计算总的QC量    前一个的任务量只在pickTaskQtc
        for (WaveDetail d : details) {
            if (d.getQcTimes() == WaveConstant.QC_TIMES_FIRST) {   //一旦有第一遍没QC的,就不是复Q
                isFirstQC = true;
            }
            if (mapItem2PickQty.get(d.getItemId()) == null) {
                mapItem2PickQty.put(d.getItemId(), new BigDecimal(d.getPickQty().toString()));
            } else {
                mapItem2PickQty.put(d.getItemId(), mapItem2PickQty.get(d.getItemId()).add(d.getPickQty()));
            }

            if (mapItem2QCQty.get(d.getItemId()) == null) {
                mapItem2QCQty.put(d.getItemId(), new BigDecimal(d.getQcQty().toString()));
            } else {
                mapItem2QCQty.put(d.getItemId(), mapItem2QCQty.get(d.getItemId()).add(d.getQcQty()));
            }

            //如果同种商品,同种货物的出货的包装单位不同,按照EA算
            if (mapItem2WaveDetail.get(d.getItemId()) != null) {
                //问题是有异常的行项目需要替换的
                if (WaveConstant.QC_EXCEPTION_STATUS_UNDO == d.getQcExceptionDone()) {
                    mapItem2WaveDetail.put(d.getItemId(), d);
                }
                //同种商品有ea存在的,就退出,按照ea算
                if (mapItem2WaveDetail.get(d.getItemId()).getAllocUnitName().equals("EA")) {
                    continue;
                }
            } else {
                mapItem2WaveDetail.put(d.getItemId(), d);
            }
        }

        //找出有责任的得detail,现有流程是一个商品只有一个挂有异常,把异常的detail的id、和商品绑定起来,--->那个商品的哪个detail有问题,需要修复
        int boxNum = 0;
        int allBoxNum = 0;
        boolean hasEA = false;
        List<Map<String, Object>> undoDetails = new LinkedList<Map<String, Object>>();
        for (Long itemId : mapItem2PickQty.keySet()) {
            WaveDetail waveDetail = mapItem2WaveDetail.get(itemId);
            Map<String, Object> detail = new HashMap<String, Object>();
            BaseinfoItem item = itemRpcService.getItem(itemId);
            detail.put("skuId", item.getSkuId());
            detail.put("itemId", item.getItemId());
            //箱子码
            detail.put("packCode", item.getPackCode());

            detail.put("code", item.getCode());
            detail.put("codeType", item.getCodeType());
            //显示六位吗
            detail.put("skuCode", item.getSkuCode());

            //加入qc的状态
            detail.put("qcDone", waveDetail.getQcExceptionDone() != WaveConstant.QC_EXCEPTION_STATUS_UNDO);  //qc任务未处理的的判断  那种商品做,哪种商品没做
            //总数量
            BigDecimal uomQty = PackUtil.EAQty2UomQty(mapItem2PickQty.get(itemId), waveDetail.getAllocUnitName());
            //qc组盘完成
            if (TaskConstant.Done.equals(qcTaskInfo.getStatus())) {
                uomQty = PackUtil.EAQty2UomQty(mapItem2QCQty.get(itemId), waveDetail.getAllocUnitName());
            }

            if (waveDetail.getAllocUnitName().compareTo("EA") == 0) {
                hasEA = true;
            } else {
                boxNum += (int) (uomQty.floatValue());
            }
            detail.put("uomQty", uomQty);
            detail.put("uom", waveDetail.getAllocUnitName());
            detail.put("isSplit", waveDetail.getAllocUnitName().compareTo("EA") == 0);
            //TODO packName
            detail.put("itemName", item.getSkuName());
            detail.put("isFristTime", waveDetail.getQcTimes() == WaveConstant.QC_TIMES_FIRST);


            //判断是第几次的QC,只有QC过一遍,再次QC都是复核QC
            detail.put("qcTimes", waveDetail.getQcTimes());
            undoDetails.add(detail);
        }
        allBoxNum = boxNum;
        if (hasEA) {
            allBoxNum++;
        }
        //获取托盘信息
        BaseinfoContainer containerInfo = iContainerRpcService.getContainer(containerId);
        if (containerInfo == null) {
            throw new BizCheckedException("2000002");
        }
        //获取客户信息
        ObdHeader soInfo = iSoRpcService.getOutbSoHeaderDetailByOrderId(details.get(0).getOrderId());   //出库单,orderid
        if (null == soInfo) {
            throw new BizCheckedException("2120016");
        }
        //获取集货道信息去库存表中查位置,   系统设计 一个托盘只能在一个位置上
//        List<Long> locationIds = stockQuantService.getLocationIdByContainerId(containerInfo.getContainerId());
//        if (null == locationIds || locationIds.size() < 1) {
//            throw new BizCheckedException("2180040");
//        }
        //使用wave_detail查位置
        Long locationId = details.get(0).getAllocCollectLocation();
        BaseinfoLocation collectLocaion = iLocationRpcService.getLocation(locationId);
        Map<String, Object> rstMap = new HashMap<String, Object>();
        rstMap.put("qcList", undoDetails);
        rstMap.put("isDirect", isDirect);
        rstMap.put("containerType", containerInfo.getType());
        rstMap.put("pickTaskId", qcTaskInfo.getQcPreviousTaskId().toString());
        //送达方的信息
        rstMap.put("customerCode", soInfo.getDeliveryCode().toString());
        rstMap.put("customerName", soInfo.getDeliveryName());
        rstMap.put("collectionRoadCode", collectLocaion.getLocationCode());
        rstMap.put("itemLineNum", mapItem2PickQty.size());
        rstMap.put("allBoxNum", allBoxNum);
        rstMap.put("itemBoxNum", boxNum);
        rstMap.put("turnoverBoxNum", hasEA ? 1 : 0);
        if (TaskConstant.Done.equals(qcTaskInfo.getStatus())) {
            rstMap.put("allBoxNum", qcTaskInfo.getTaskPackQty());
            rstMap.put("itemBoxNum", qcTaskInfo.getExt4());
            rstMap.put("turnoverBoxNum", qcTaskInfo.getExt3());
        }
        rstMap.put("qcTaskDone", qcTaskInfo.getStatus() == TaskConstant.Done);
        rstMap.put("qcTaskId", qcTaskInfo.getTaskId().toString());
        rstMap.put("containerId", qcTaskInfo.getContainerId());
        rstMap.put("isFristQc", isFirstQC);
        rstMap.put("step", step);
        //捡货人员的名字
        TaskInfo pickTask = baseTaskService.getTaskByTaskId(qcTaskInfo.getQcPreviousTaskId());
        if (null == pickTask) {
            throw new BizCheckedException("2060003", qcTaskInfo.getQcPreviousTaskId(), "");
        }
        SysUser picker = sysUserService.getSysUserByUid(pickTask.getOperator().toString());
        rstMap.put("pickerName", picker.getScreenname());
        return JsonUtils.SUCCESS(rstMap);
    }


    @POST
    @Path("qcOneItem")
    public String qcOneItem() throws BizCheckedException {
        //获取参数
        Map<String, Object> request = RequestUtils.getRequest();
        //在库拣货qc输入ea数量,直流按照EA或者箱子播种和收货
        long qcTaskId = Long.valueOf(request.get("qcTaskId").toString());
        if (null == request.get("uomQty")) {
            throw new BizCheckedException("2120024");
        }
        BigDecimal qtyUom = new BigDecimal(request.get("uomQty").toString());   //可以是箱数或EA数量
        BigDecimal defectQty = null;
        if (null == request.get("defectQty")) {
            defectQty = new BigDecimal("0.0000");
        } else {
            defectQty = new BigDecimal(request.get("defectQty").toString()); //可以是箱数或EA数量(两者是箱子的话都是箱子)
        }
        long exceptionType = 0L;
        BigDecimal exceptionQty = new BigDecimal("0.0000");
        if (defectQty.compareTo(BigDecimal.ZERO) > 0) {
            exceptionType = WaveConstant.QC_EXCEPTION_DEFECT;   //残次异常  有残次,不追着
            exceptionQty = defectQty;
        }
        //初始化QC任务
        TaskInfo qcTaskInfo = iTaskRpcService.getTaskInfo(qcTaskId);
        if (qcTaskInfo == null) {
            throw new BizCheckedException("2120007");
        }
        //转换商品条形码为sku码
        //未来可能会使用item  前提出库的时候一个托盘的货是一个货主的
        ObdHeader obdHeader = iSoRpcService.getOutbSoHeaderDetailByOrderId(qcTaskInfo.getOrderId());
        if (null == obdHeader) {
            throw new BizCheckedException("2870006");
        }


        String code = (String) request.get("code");
        Long ownerId = obdHeader.getOwnerUid();
        BaseinfoItem item = this.getItem(ownerId, code);
        if (null == item) {
            throw new BizCheckedException("2120001");
        }

        List<WaveDetail> details = waveService.getDetailsByContainerId(qcTaskInfo.getContainerId());    //qc的数量不在是拣货的数量了,而是集货的数量和收货的数量
        // 标识是拣货生成的QC还是集货生成的QC
        //计算是拣货生成的|集货生成的|收货生成的
        int seekNum = 0;
        List<WaveDetail> matchDetails = new LinkedList<WaveDetail>();
        BigDecimal pickQty = new BigDecimal("0.0000");
        for (WaveDetail d : details) {          //一个itemId多个国条的时候的解法,只能用itemId来解决了,  前提一个托盘只能是一个火族的货
            if (!d.getItemId().equals(item.getItemId())) {
                continue;
            }
            seekNum++;
            matchDetails.add(d);
            pickQty = pickQty.add(d.getPickQty());
        }
        if (seekNum == 0) {
            if (true) {
                throw new BizCheckedException("2120002");
            }
            if (exceptionType != WaveConstant.QC_EXCEPTION_NOT_MATCH) {  //一件没找到,还不是错货
                throw new BizCheckedException("2120009");
            }
            WaveQcException qcException = new WaveQcException();
//            qcException.setSkuId(skuInfo.getSkuId());
            qcException.setExceptionQty(exceptionQty);
            qcException.setExceptionType(exceptionType);
            qcException.setQcTaskId(qcTaskId);
            qcException.setWaveId(qcTaskInfo.getWaveId());
            waveService.insertQCException(qcException);
        } else {
            BigDecimal qty = PackUtil.UomQty2EAQty(qtyUom, matchDetails.get(0).getAllocUnitName());
            exceptionQty = PackUtil.UomQty2EAQty(exceptionQty, matchDetails.get(0).getAllocUnitName());
            if (exceptionQty.compareTo(qty) > 0) {
                throw new BizCheckedException("2120013");
            }
            int cmpRet = pickQty.compareTo(qty);    //拣货 - qc的数量
            if (cmpRet > 0) exceptionType = WaveConstant.QC_EXCEPTION_LACK; //少货
            if (cmpRet < 0) exceptionType = WaveConstant.QC_EXCEPTION_OVERFLOW; //多货
            BigDecimal curQty = new BigDecimal("0.0000");
            for (int i = 0; i < matchDetails.size(); ++i) {
                WaveDetail detail = matchDetails.get(i);
                BigDecimal lastQty = curQty;
                curQty = curQty.add(detail.getPickQty());
                detail.setQcQty(qty);
                detail.setQcAt(DateUtils.getCurrentSeconds());
                detail.setQcUid(Long.valueOf(RequestUtils.getHeader("uid")));
                detail.setQcException(exceptionType);
                detail.setQcAt(DateUtils.getCurrentSeconds());
                if (exceptionType != 0) {
                    //多货
                    if (i == matchDetails.size() - 1) {     //记录detail的id
                        detail.setQcException(exceptionType);
                        //设置成拣货人的责任
                        if (exceptionType == WaveConstant.QC_EXCEPTION_DEFECT) {
                            detail.setQcExceptionQty(exceptionQty);
                            //残次不追责
//                            detail.setQcFault(WaveConstant.QC_FAULT_NOMAL);
//                            detail.setQcFaultQty(exceptionQty); //残次不记录责任
                        } else {
                            detail.setQcExceptionQty(qty.subtract(curQty).abs());   //取绝对值
                            detail.setQcFaultQty(qty.subtract(curQty).abs());   //少货或者多货取绝对值
                            detail.setQcFault(WaveConstant.QC_FAULT_PICK); //永远是拣货人的责任,不修复的话
                        }
                        // 以后做有残次不追责
                        detail.setQcExceptionDone(0L);
                        detail.setQcQty(qty.subtract(lastQty));

                    } else {    //最后一个记录异常,其他的都正常
                        //忽略
                        detail.setQcQty(detail.getPickQty());
                        detail.setQcException(0L);
                        detail.setQcExceptionQty(BigDecimal.ZERO);
                        detail.setQcExceptionDone(1L);
                        detail.setQcQty(detail.getPickQty());
                        detail.setQcFault(WaveConstant.QC_FAULT_NOMAL);//无责任人
                        detail.setQcFaultQty(new BigDecimal("0.0000"));
                    }
                } else {
                    detail.setQcQty(detail.getPickQty());
                    detail.setQcException(0L);
                    detail.setQcExceptionQty(BigDecimal.ZERO);
                    detail.setQcExceptionDone(1L);
                    detail.setQcFault(WaveConstant.QC_FAULT_NOMAL);//无责任人
                    detail.setQcFaultQty(new BigDecimal("0.0000"));
                }
                detail.setQcTimes(WaveConstant.QC_TIMES_MORE);  //qc的次数变更
                waveService.updateDetail(detail);
            }
            qcTaskInfo.setExt2(exceptionType);
            TaskEntry entry = new TaskEntry();
            entry.setTaskInfo(qcTaskInfo);
            iTaskRpcService.update(TaskConstant.TYPE_QC, entry);
        }
        //校验qc任务是否完全完成;
        boolean bSucc = true;
        for (WaveDetail d : details) {
            if (d.getQcExceptionDone() == WaveConstant.QC_EXCEPTION_STATUS_UNDO) {  //未处理异常
                bSucc = false;
                break;
            }
            //计算QC的任务量  qc的数量,一个拣货任务的任务量,怎么算,一次qc的数量吗,复QC的人的任务量怎么算
            //TODO QC TaSK qTY
        }
        //返回结果
        Map<String, Object> rstMap = new HashMap<String, Object>();
        rstMap.put("qcDone", bSucc);
        return JsonUtils.SUCCESS(rstMap);
    }

    /**
     * 箱数boxNum 周转箱数是turnoverBoxNum   总箱数allboxNum= boxNum+turnoverBoxNum
     * 跳过qc明细 skip字段为ture,系统默认拣货数量(待qc数量)和qc相同
     *
     * @return
     * @throws BizCheckedException
     */
    @POST
    @Path("confirm")
    public String confirm() throws BizCheckedException {
        Map<String, Object> request = RequestUtils.getRequest();
        //前端跳过q明细字段,跳过,系统默认拣货量和qc量的一致
        Boolean skip = Boolean.valueOf(request.get("skip").toString());
        long qcTaskId = Long.valueOf(request.get("qcTaskId").toString());
        if (null == request.get("boxNum") || null == request.get("turnoverBoxNum")) {
            throw new BizCheckedException("2120025");
        }
        long boxNum = Long.valueOf(request.get("boxNum").toString());
        long turnoverBoxNum = Long.valueOf(request.get("turnoverBoxNum").toString());
        //初始化QC任务
        TaskInfo qcTaskInfo = iTaskRpcService.getTaskInfo(qcTaskId);
        if (qcTaskInfo == null) {
            throw new BizCheckedException("2120007");
        }
        List<WaveDetail> details = waveService.getDetailsByContainerId(qcTaskInfo.getContainerId());


        //跳过qc明细,系统默认拣货量和qc量相等
        qcTaskInfo.setSubType(TaskConstant.QC_TYPE_QC_GROUP);
        if (skip) {
            for (WaveDetail detail : details) {
                detail.setQcQty(detail.getPickQty()); //先默认qc数量是正常的
                detail.setQcTimes(WaveConstant.QC_TIMES_MORE);
                detail.setQcExceptionDone(WaveConstant.QC_EXCEPTION_STATUS_NORMAL);
                detail.setQcException(WaveConstant.QC_EXCEPTION_NORMAL);  // 正常不需要处理
                waveService.updateDetail(detail);
            }
            //qc的方式  qc的类型  显示 PC的列表页   QC 的状态   只组盘 未QC加组盘
            qcTaskInfo.setSubType(TaskConstant.QC_TYPE_ONLY_GROUP);
        }
        //校验qc任务是否完全完成;
        boolean bSucc = true;
        BigDecimal sumEAQty = new BigDecimal("0.0000");

        //如果商品的数量确实缺交,就不应该有该商品的出库记录了,is_alive置为0
        List<WaveDetail> deathDetails = new ArrayList<WaveDetail>();
        //不能组盘的时候,PC忽略异常
        for (WaveDetail d : details) {
            if (d.getQcExceptionDone().equals(WaveConstant.QC_EXCEPTION_STATUS_UNDO)) {
                bSucc = false;
                break;
            }
            sumEAQty = sumEAQty.add(d.getPickQty());
            //计算QC的任务量
            //TODO QC TaSK qTY
            if (d.getQcQty().compareTo(BigDecimal.ZERO)==0){
                d.setIsAlive(0L);
                deathDetails.add(d);
            }
        }
        if (!bSucc) {
            throw new BizCheckedException("2120004");
        }
        if (bSucc) {
            //成功
            //设置task的信息;
            qcTaskInfo.setTaskEaQty(sumEAQty);
            qcTaskInfo.setTaskPackQty(BigDecimal.valueOf(boxNum + turnoverBoxNum));     //总箱数
            qcTaskInfo.setExt4(boxNum); //箱数
            qcTaskInfo.setExt3(turnoverBoxNum); //总周转箱数
            //设置合板的托盘
            qcTaskInfo.setMergedContainerId(qcTaskInfo.getContainerId());
            qcTaskInfo.setExt2(WaveConstant.QC_EXCEPTION_NORMAL);
            TaskEntry entry = new TaskEntry();
            entry.setTaskInfo(qcTaskInfo);
            entry.setTaskDetailList((List<Object>) (List<?>) details);
            iTaskRpcService.update(TaskConstant.TYPE_QC, entry);
            iTaskRpcService.done(qcTaskId, qcTaskInfo.getLocationId());

            //将出库为0的商品记录消掉
            if (!deathDetails.isEmpty()){
                waveService.updateDetails(deathDetails);
            }
        }
        return JsonUtils.SUCCESS(new HashMap<String, Boolean>() {
            {
                put("response", true);
            }
        });
    }


    /**
     * 跳过异常
     *
     * @return
     * @throws BizCheckedException
     */
    @POST
    @Path("dealCase")
    public String dealCase() throws BizCheckedException {
        Map<String, Object> request = RequestUtils.getRequest();
        Integer type = Integer.valueOf(request.get("type").toString());
        Long containerId = Long.valueOf(request.get("containerId").toString());
        if (null == request.get("uomQty")) {
            throw new BizCheckedException("2120024");
        }
        BigDecimal qtyUom = new BigDecimal(request.get("uomQty").toString());   //复QC的数量
        String code = request.get("code").toString();
        boolean result = false;
        if (WaveConstant.QC_RF_SKIP == type) {
            result = iqcRpcService.skipExceptionRf(request);
        }
        if (WaveConstant.QC_RF_FALLBACK == type) {
            result = iqcRpcService.fallbackExceptionRf(request);
        }
        if (WaveConstant.QC_RF_REPAIR == type) {
            result = iqcRpcService.repairExceptionRf(request);
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("qcDone", result);
        return JsonUtils.SUCCESS(resultMap);
    }


    /**
     * 废弃了,呵呵
     *
     * @return
     * @throws BizCheckedException
     */
    @POST
    @Path("confirmAll")
    public String confirmAll() throws BizCheckedException {
        Map<String, Object> mapRequest = RequestUtils.getRequest();
        HttpSession session = RequestUtils.getSession();
        Long containerId = Long.valueOf(mapRequest.get("containerId").toString());
        //获取当前的有效待QC container 任务列表
        //get task  by containerId
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("containerId", containerId);
        //mapQuery.put("");
        List<TaskEntry> tasks = iTaskRpcService.getTaskHeadList(TaskConstant.TYPE_QC, mapQuery);
        if (tasks.size() == 0) {
            throw new BizCheckedException("2120007");
        } else if (tasks.size() > 1) {
            throw new BizCheckedException("2120006");
        }
        /*
        if(tasks.get(0).getTaskInfo().getStatus()==TaskConstant.Done
                || tasks.get(0).getTaskInfo().getStatus() == TaskConstant.Cancel){
            throw new BizCheckedException("2120011");
        }
        */
        List<Map> qcList = JSON.parseArray(mapRequest.get("qcList").toString(), Map.class);
        List<WaveDetail> details = waveService.getDetailsByContainerId(containerId);
        int addExceptionNum = 0;
        for (Map<String, Object> qcItem : qcList) {
            long exceptionType = 0;
            String code = qcItem.get("code").toString().trim();
            BigDecimal qty = new BigDecimal(qcItem.get("qty").toString());
            CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
            if (skuInfo == null) {
                throw new BizCheckedException("2120001");
            }
            long skuId = skuInfo.getSkuId();
            int seekNum = 0;
            List<WaveDetail> matchDetails = new LinkedList<WaveDetail>();
            BigDecimal pickQty = new BigDecimal("0.0000");
            for (WaveDetail d : details) {
                if (d.getSkuId() != skuId) {
                    continue;
                }
                seekNum++;
                matchDetails.add(d);
                pickQty = pickQty.add(d.getPickQty());
            }
            if (seekNum == 0) {
                exceptionType = 3;
                long tmpExceptionType = qcItem.get("exceptionType") == null ? 0L : Long.valueOf(qcItem.get("exceptionType").toString());
                if (tmpExceptionType != exceptionType) {
                    throw new BizCheckedException("2120009");
                }
                if (qcItem.get("exceptionQty") == null) {
                    throw new BizCheckedException("2120010");
                }
                WaveQcException qcException = new WaveQcException();
                qcException.setSkuId(skuInfo.getSkuId());
                BigDecimal exctpionQty = new BigDecimal(qcItem.get("exceptionQty").toString());
                qcException.setExceptionQty(exctpionQty);
                qcException.setExceptionType(exceptionType);
                qcException.setQcTaskId(0L);
                qcException.setWaveId(0L);
                waveService.insertQCException(qcException);
                addExceptionNum++;
                continue;
            }
            int cmpRet = pickQty.compareTo(qty);    //拣货数和实际的QC数的差别
            if (cmpRet > 0) exceptionType = 2; //多货
            if (cmpRet < 0) exceptionType = 1; //少货

            BigDecimal curQty = new BigDecimal("0.0000");
            for (int i = 0; i < matchDetails.size(); ++i) {
                WaveDetail detail = matchDetails.get(i);
                BigDecimal lastQty = curQty;
                curQty = curQty.add(detail.getPickQty());
                detail.setQcQty(qty);
                detail.setQcAt(DateUtils.getCurrentSeconds());
                detail.setQcUid(Long.valueOf(RequestUtils.getHeader("uid")));
                detail.setQcException(exceptionType);
                detail.setQcAt(DateUtils.getCurrentSeconds());
                detail.setQcUid(Long.valueOf(RequestUtils.getHeader("uid")));
                if (exceptionType != 0) {
                    //多货
                    if (i == matchDetails.size() - 1) {
                        detail.setQcException(exceptionType);
                        detail.setQcExceptionQty(qty.subtract(curQty));
                        detail.setQcExceptionDone(0L);
                        detail.setQcQty(qty.subtract(lastQty));

                    } else {
                        //忽略
                        detail.setQcQty(detail.getPickQty());
                        detail.setQcException(0L);
                        detail.setQcExceptionQty(BigDecimal.ZERO);
                        detail.setQcExceptionDone(1L);
                        detail.setQcQty(detail.getPickQty());
                    }
                } else {
                    detail.setQcQty(detail.getPickQty());
                    detail.setQcException(0L);
                    detail.setQcExceptionQty(BigDecimal.ZERO);
                    detail.setQcExceptionDone(1L);
                }
                waveService.updateDetail(detail);
            }
        }
        Set<Long> setItem = new HashSet<Long>();
        for (WaveDetail detail : details) {
            setItem.add(detail.getItemId());
        }
        if (qcList.size() - addExceptionNum != setItem.size()) {
            throw new BizCheckedException("2120004");
        }
        // 最后qc提交的状态是有task状态的变更的
        iTaskRpcService.done(tasks.get(0).getTaskInfo().getTaskId());
        return JsonUtils.SUCCESS(new HashMap<String, Boolean>() {
            {
                put("response", true);
            }
        });
    }

    /**
     * 先国条后箱子码
     *
     * @param ownerId
     * @param code
     * @return
     * @throws BizCheckedException
     */
    private BaseinfoItem getItem(Long ownerId, String code) throws BizCheckedException {
        BaseinfoItem baseinfoItem = null;
        //国条码
        CsiSku skuInfo = csiRpcService.getSkuByCode(CsiConstan.CSI_CODE_TYPE_BARCODE, code);
        if (null != skuInfo && skuInfo.getSkuId() != null) {
            baseinfoItem = itemService.getItem(ownerId, skuInfo.getSkuId());
        }

        if (baseinfoItem != null) {
            return baseinfoItem;
        }
        //箱码
        baseinfoItem = itemService.getItemByPackCode(ownerId, code);
        if (baseinfoItem != null) {
            return baseinfoItem;
        }
        if (baseinfoItem == null) {
            throw new BizCheckedException("2900001");
        }
        return baseinfoItem;
    }
}

