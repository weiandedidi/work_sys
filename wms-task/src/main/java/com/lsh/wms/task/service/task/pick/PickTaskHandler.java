package com.lsh.wms.task.service.task.pick;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.BeanMapTransUtils;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.core.constant.TaskConstant;
import com.lsh.wms.core.service.csi.CsiSupplierService;
import com.lsh.wms.core.service.location.LocationService;
import com.lsh.wms.core.service.pick.PickTaskService;
import com.lsh.wms.core.service.stock.StockMoveService;
import com.lsh.wms.core.service.task.BaseTaskService;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiSupplier;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.wave.WaveDetail;
import com.lsh.wms.model.pick.PickTaskHead;
import com.lsh.wms.model.task.StockTakingTask;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.task.service.handler.AbsTaskHandler;
import com.lsh.wms.task.service.handler.TaskHandlerFactory;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zengwenjun on 16/7/23.
 */

@Component
public class PickTaskHandler extends AbsTaskHandler {
    @Autowired
    private TaskHandlerFactory handlerFactory;
    @Autowired
    private PickTaskService pickTaskService;
    @Autowired
    private StockMoveService stockMoveService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private WaveService waveService;
    @Autowired
    private CsiSupplierService csiSupplierService;

    @PostConstruct
    public void postConstruct() {
        handlerFactory.register(TaskConstant.TYPE_PICK, this);
    }

    public void create(TaskEntry taskEntry) {
        /*TaskInfo taskInfo = taskEntry.getTaskInfo();
        Long taskId = taskInfo.getTaskId();
        List<WaveDetail> pickTaskDetails = waveService.getDetailsByPickTaskId(taskId);
        BigDecimal qtyDone = BigDecimal.ZERO;

        for (WaveDetail pickTaskDetail: pickTaskDetails) {
            qtyDone = qtyDone.add(pickTaskDetail.getAllocQty());
        }
        taskInfo.setQtyDone(qtyDone);
        taskEntry.setTaskInfo(taskInfo);*/

        super.create(taskEntry);
    }

    public void batchCreate(List<TaskEntry> taskEntries) throws BizCheckedException{
        for (TaskEntry taskEntry : taskEntries){
            TaskInfo taskInfo = taskEntry.getTaskInfo();
            Long taskId = taskInfo.getTaskId();
            List<WaveDetail> pickTaskDetails = waveService.getDetailsByPickTaskId(taskId);
            BigDecimal qtyDone = BigDecimal.ZERO;

            for (WaveDetail pickTaskDetail: pickTaskDetails) {
                qtyDone = qtyDone.add(pickTaskDetail.getAllocQty());
            }
            taskInfo.setQtyDone(qtyDone);
            taskEntry.setTaskInfo(taskInfo);
        }
        super.batchCreate(taskEntries);
        /*
        这之后我会把来源obd里的数据的release做加法操作
         */
        List<WaveDetail> details = new LinkedList<WaveDetail>();
        for(TaskEntry taskEntry : taskEntries){
            details.addAll((List<WaveDetail>)(List<?>)taskEntry.getTaskDetailList());

        }
        waveService.increaseReleaseQtyByWaveDeTails(details);
    }

    public void createConcrete(TaskEntry taskEntry) throws BizCheckedException {
        PickTaskHead head = (PickTaskHead)taskEntry.getTaskHead();
        Long taskId = taskEntry.getTaskInfo().getTaskId();
        head.setTaskId(taskId);
        List<WaveDetail> details = (List<WaveDetail>)(List<?>)taskEntry.getTaskDetailList();
        // 因为更新托盘时detail仍为空,所以注释掉了
        /*if (details.size() < 1) {
            throw new BizCheckedException("2060002");
        }*/
        if (details != null) {
            for (WaveDetail detail : details) {
                detail.setPickTaskId(taskId);
            }
        }
        pickTaskService.createPickTask(head, details);
    }

    public void calcPerformance(TaskInfo taskInfo) {
        Long taskId = taskInfo.getTaskId();
        List<WaveDetail> pickDetails = waveService.getDetailsByPickTaskId(taskId);
        BigDecimal qtyDone = BigDecimal.ZERO;
        for (WaveDetail pickDetail : pickDetails) {
            qtyDone = qtyDone.add(pickDetail.getPickQty());
        }
        taskInfo.setQtyDone(qtyDone);
        taskInfo.setTaskEaQty(qtyDone);
    }

    public void doneConcrete(Long taskId, Long locationId, Long staffId) throws BizCheckedException{
        PickTaskHead taskHead = pickTaskService.getPickTaskHead(taskId);
        if(locationId == 0){
            //自动集货
            locationId = taskHead.getAllocCollectLocation();
        }
        taskHead.setPickAt(DateUtils.getCurrentSeconds());
        taskHead.setRealCollectLocation(locationId);
        pickTaskService.update(taskHead);
        // 更新wave_detail
        List<WaveDetail> pickDetails = waveService.getDetailsByPickTaskId(taskId);
        BigDecimal realTotalQty = new BigDecimal(0);
        for (WaveDetail pickDetail : pickDetails) {
            realTotalQty = realTotalQty.add(pickDetail.getPickQty());
            pickDetail.setRealCollectLocation(locationId);
            if (pickDetail.getPickQty().compareTo(BigDecimal.ZERO) == 0) {
                pickDetail.setIsAlive(0L); // 对于只捡了0个的,不需要qc,直接将wave_detail置为完成
            }
            waveService.updateDetail(pickDetail);
        }
        // 移动库存,实际移动库存为0时则不移动
        if (realTotalQty.compareTo(new BigDecimal(0)) == 1) {
            BaseinfoLocation collectRegionLocation = locationService.getFatherRegionBySonId(taskHead.getAllocCollectLocation());
            if (collectRegionLocation == null) {
                throw new BizCheckedException("2060019");
            }
            stockMoveService.moveWholeContainer(taskHead.getContainerId(), taskId, staffId, collectRegionLocation.getLocationId(), locationId);
        }
        //--------------稍微注意一下下面两个操作会不会影响到性能,严格来讲,其实最好是异步的,呵呵.
        //更新订单状态
        if(taskHead.getDeliveryId()>1) {
            waveService.updateOrderStatus(taskHead.getDeliveryId());
        }
        //更新波次状态
        waveService.updateWaveStatus(taskHead.getWaveId());
    }


    public void assignConcrete(Long taskId, Long staffId, Long containerId) throws BizCheckedException {
        PickTaskHead head = pickTaskService.getPickTaskHead(taskId);
        head.setContainerId(containerId);
        pickTaskService.update(head);
    }

    public void getConcrete(TaskEntry taskEntry) {
        List<WaveDetail> pickTaskDetails = waveService.getDetailsByPickTaskId(taskEntry.getTaskInfo().getTaskId());
        List<Map<String, Object>> details = new ArrayList<Map<String, Object>>();

        BigDecimal totalQty = new BigDecimal(0);

        for (WaveDetail pickTaskDetail: pickTaskDetails) {
            Map<String, Object> detail = BeanMapTransUtils.Bean2map(pickTaskDetail);
            if (Long.valueOf(detail.get("pickAt").toString()) > 0) {
                detail.put("pickStatus", 1);
            } else {
                detail.put("pickStatus", 0);
            }
            detail.put("allocQty", PackUtil.EAQty2UomQty(pickTaskDetail.getAllocQty(), pickTaskDetail.getAllocUnitName()));
            detail.put("pickQty", PackUtil.EAQty2UomQty(pickTaskDetail.getPickQty(), pickTaskDetail.getAllocUnitName()));
            /*CsiSupplier supplier = csiSupplierService.getSupplier(pickTaskDetail.getSupplierId());
            detail.put("supplierCode", supplier.getSupplierCode());*/
            details.add(detail);
            totalQty = totalQty.add(pickTaskDetail.getAllocQty());
        }
        Map<String, Object> head = BeanMapTransUtils.Bean2map(pickTaskService.getPickTaskHead(taskEntry.getTaskInfo().getTaskId()));
        head.put("totalQty", totalQty);
        taskEntry.setTaskHead(head);
        taskEntry.setTaskDetailList((List<Object>)(List<?>)details);
    }

    public void getOldConcrete(TaskEntry taskEntry) {
        List<WaveDetail> pickTaskDetails = waveService.getDetailsByPickTaskIdPc(taskEntry.getTaskInfo().getTaskId());
        List<Map<String, Object>> details = new ArrayList<Map<String, Object>>();

        BigDecimal totalQty = new BigDecimal(0);

        for (WaveDetail pickTaskDetail: pickTaskDetails) {
            Map<String, Object> detail = BeanMapTransUtils.Bean2map(pickTaskDetail);
            if (Long.valueOf(detail.get("pickAt").toString()) > 0) {
                detail.put("pickStatus", 1);
            } else {
                detail.put("pickStatus", 0);
            }
            detail.put("allocQty", PackUtil.EAQty2UomQty(pickTaskDetail.getAllocQty(), pickTaskDetail.getAllocUnitName()));
            detail.put("pickQty", PackUtil.EAQty2UomQty(pickTaskDetail.getPickQty(), pickTaskDetail.getAllocUnitName()));
            /*CsiSupplier supplier = csiSupplierService.getSupplier(pickTaskDetail.getSupplierId());
            detail.put("supplierCode", supplier.getSupplierCode());*/
            details.add(detail);
            totalQty = totalQty.add(pickTaskDetail.getAllocQty());
        }
        Map<String, Object> head = BeanMapTransUtils.Bean2map(pickTaskService.getPickTaskHead(taskEntry.getTaskInfo().getTaskId()));
        head.put("totalQty", totalQty);
        taskEntry.setTaskHead(head);
        taskEntry.setTaskDetailList((List<Object>)(List<?>)details);
    }

    public void getHeadConcrete(TaskEntry taskEntry) {
        taskEntry.setTaskHead(pickTaskService.getPickTaskHead(taskEntry.getTaskInfo().getTaskId()));
    }
}
