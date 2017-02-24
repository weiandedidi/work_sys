package com.lsh.wms.model.task;

import java.io.Serializable;
import java.math.BigDecimal;

public class TaskInfo implements Serializable {

    /**  */
    private Long id;
    /**
     * 任务id
     */
    private Long taskId = 0L;
    /**
     * 任务名称
     */
    private String taskName = "";
    /**
     * 计划id
     */
    private Long planId = 0L;
    /**
     * 波次id
     */
    private Long waveId = 0L;
    /**
     * 订单id
     */
    private Long orderId = 0L;
    /**
     * 收货id
     */
    private Long receiptId = 0L;
    /**
     * 当前LocationId, 分配查找使用
     */
    private Long locationId = 0L;
    /**
     * 商品id，分配查找用
     */
    private Long skuId = 0L;
    /**
     * 货主id，分配查找用
     */
    private Long ownerId = 0L;
    /**
     * itemid，分配查找用
     */
    private Long itemId = 0L;
    /**
     * 移入库位id
     */
    private Long fromLocationId = 0L;
    /**
     * 移入库位id
     */
    private Long toLocationId = 0L;
    private Long realFromLocationId = 0L;
    private Long realToLocationId = 0L;
    private BigDecimal qtyDone = BigDecimal.ZERO;
    /**
     * 数量
     */
    private BigDecimal qty = BigDecimal.ZERO;
    private BigDecimal qtyUom = BigDecimal.ZERO;
    private BigDecimal qtyDoneUom = BigDecimal.ZERO;
    /**
     * 包装单位
     */
    private BigDecimal packUnit = BigDecimal.ZERO;
    /**
     * 包装名称
     */
    private String packName = "";
    /**
     * 容器id，分配查找用
     */
    private Long containerId = 0L;
    /**
     * 操作人员id
     */
    private Long operator = 0L;
    /**
     * 发起人员id
     */
    private Long planner = 0L;
    /**
     * 任务类型，100-盘点， 101-收货，102-波次， 103-上架，104-补货, 105-移库
     */
    private Long type = 0L;
    /**
     * 任务子类型, 由个任务类型自解释,可以不需要
     */
    private Long subType = 0L;
    /**
     * 任务状态，1-draft,2-assigned, 3-done, 4-cancel
     */
    private Long status = 0L;
    /**
     * 资源分配状态, 1-未分配， 2-已分配
     */
    private Long isAllocated = 0L;
    /**
     * 优先级
     */
    private Long priority = 0L;
    /**
     * 创建时间
     */
    private Long draftTime = 0L;
    /**
     * 分配时间
     */
    private Long assignTime = 0L;
    /**
     * 最晚完成时间
     */
    private Long dueTime = 0L;
    /**
     * 实际完成时间
     */
    private Long finishTime = 0L;
    /**
     * 取消时间
     */
    private Long cancelTime = 0L;
    /** 挂起时间 */
    private Long holdTime;
    /**
     * 扩展字段
     */
    private Long ext1 = 0L;
    /**
     * 扩展字段
     */
    private Long ext2 = 0L;
    /**
     * 扩展字段
     */
    private Long ext3 = 0L;
    /**
     * 扩展字段
     */
    private Long ext4 = 0L;
    /**
     * 扩展字段
     */
    private Long ext5 = 0L;
    /**
     * 扩展字段
     */
    private String ext6 = "";
    /**
     * 扩展字段
     */
    private String ext7 = "";
    /**
     * 扩展字段
     */
    private String ext8 = "";
    /**
     * 扩展字段
     */
    private String ext9 = "";
    /**
     * 合板后关系容器id
     */
    private Long mergedContainerId = 0L;
    /**
     * 子任务执行状态
     */
    private int step = 0;
    /**
     * 是否显示，非必须
     */
    private int isShow = 1;
    /**
     * 任务执行顺序
     */
    private Long taskOrder = 0L;
    /**
     * 直流在库标记 1在库 2直流
     */
    private Long businessMode = 1L;

    /**  */
    private Long createdAt;
    /**  */
    private Long updatedAt;


    public Long date = 0L;
    /**
     * TMS线路
     */
    private String transPlan = "";

    /**
     * 任务工作量(按外包装计算)
     */
    private BigDecimal taskPackQty = BigDecimal.ZERO;
    /**
     * 任务工作量(ea计算)
     */
    private BigDecimal taskEaQty = BigDecimal.ZERO;
    /** 工作任务量（按照板数） */
    private BigDecimal taskBoardQty;

    /** 工作任务量 */
    private BigDecimal taskAmount;

    private BigDecimal taskQty = BigDecimal.ZERO;

    /**
     * QC任务之前承接的taskId，用于判断生成qc前是什么任务，好去wave_detail中去，去需要qc的数量，不一定是拣货
     */
    private Long qcPreviousTaskId = 0L;

    /**
     * 是否跳过q明细，0不跳过1-跳过
     */
    private Integer qcSkip = 0;

    public BigDecimal getTaskQty() {
        return taskQty;
    }

    public void setTaskQty(BigDecimal taskQty) {
        this.taskQty = taskQty;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getPlanId() {
        return this.planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getWaveId() {
        return this.waveId;
    }

    public void setWaveId(Long waveId) {
        this.waveId = waveId;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Long getLocationId() {
        return this.locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getSkuId() {
        return this.skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getFromLocationId() {
        return this.fromLocationId;
    }

    public void setFromLocationId(Long fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public Long getToLocationId() {
        return this.toLocationId;
    }

    public void setToLocationId(Long toLocationId) {
        this.toLocationId = toLocationId;
    }

    public BigDecimal getQty() {
        return this.qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getPackUnit() {
        return this.packUnit;
    }

    public void setPackUnit(BigDecimal packUnit) {
        this.packUnit = packUnit;
    }

    public String getPackName() {
        return this.packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public Long getContainerId() {
        return this.containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public Long getOperator() {
        return this.operator;
    }

    public void setOperator(Long operator) {
        this.operator = operator;
    }

    public Long getPlanner() {
        return this.planner;
    }

    public void setPlanner(Long planner) {
        this.planner = planner;
    }

    public Long getType() {
        return this.type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getSubType() {
        return this.subType;
    }

    public void setSubType(Long subType) {
        this.subType = subType;
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getIsAllocated() {
        return this.isAllocated;
    }

    public void setIsAllocated(Long isAllocated) {
        this.isAllocated = isAllocated;
    }

    public Long getPriority() {
        return this.priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Long getDraftTime() {
        return this.draftTime;
    }

    public void setDraftTime(Long draftTime) {
        this.draftTime = draftTime;
    }

    public Long getAssignTime() {
        return this.assignTime;
    }

    public void setAssignTime(Long assignTime) {
        this.assignTime = assignTime;
    }

    public Long getDueTime() {
        return this.dueTime;
    }

    public void setDueTime(Long dueTime) {
        this.dueTime = dueTime;
    }

    public Long getFinishTime() {
        return this.finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public Long getCancelTime() {
        return this.cancelTime;
    }

    public void setCancelTime(Long cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Long getHoldTime(){
        return this.holdTime;
    }

    public void setHoldTime(Long holdTime){
        this.holdTime = holdTime;
    }

    public Long getExt1() {
        return this.ext1;
    }

    public void setExt1(Long ext1) {
        this.ext1 = ext1;
    }

    public Long getExt2() {
        return this.ext2;
    }

    public void setExt2(Long ext2) {
        this.ext2 = ext2;
    }

    public Long getExt3() {
        return this.ext3;
    }

    public void setExt3(Long ext3) {
        this.ext3 = ext3;
    }

    public Long getExt4() {
        return this.ext4;
    }

    public void setExt4(Long ext4) {
        this.ext4 = ext4;
    }

    public Long getExt5() {
        return this.ext5;
    }

    public void setExt5(Long ext5) {
        this.ext5 = ext5;
    }

    public String getExt6() {
        return this.ext6;
    }

    public void setExt6(String ext6) {
        this.ext6 = ext6;
    }

    public String getExt7() {
        return this.ext7;
    }

    public void setExt7(String ext7) {
        this.ext7 = ext7;
    }

    public String getExt8() {
        return this.ext8;
    }

    public void setExt8(String ext8) {
        this.ext8 = ext8;
    }

    public String getExt9() {
        return this.ext9;
    }

    public void setExt9(String ext9) {
        this.ext9 = ext9;
    }

    public Long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getRealFromLocationId() {
        return realFromLocationId;
    }

    public void setRealFromLocationId(Long realFromLocationId) {
        this.realFromLocationId = realFromLocationId;
    }

    public Long getRealToLocationId() {
        return realToLocationId;
    }

    public void setRealToLocationId(Long readToLocationId) {
        this.realToLocationId = readToLocationId;
    }

    public BigDecimal getQtyDone() {
        return qtyDone;
    }

    public void setQtyDone(BigDecimal qtyDone) {
        this.qtyDone = qtyDone;
    }


    public BigDecimal getQtyUom() {
        return qtyUom;
    }

    public void setQtyUom(BigDecimal qtyUom) {
        this.qtyUom = qtyUom;
    }

    public BigDecimal getQtyDoneUom() {
        return qtyDoneUom;
    }

    public void setQtyDoneUom(BigDecimal qtyDoneUom) {
        this.qtyDoneUom = qtyDoneUom;
    }

    public BigDecimal getTaskEaQty() {
        return taskEaQty;
    }

    public void setTaskEaQty(BigDecimal taskEaQty) {
        this.taskEaQty = taskEaQty;
    }

    public BigDecimal getTaskPackQty() {
        return taskPackQty;
    }

    public void setTaskPackQty(BigDecimal taskPackQty) {
        this.taskPackQty = taskPackQty;
    }

    public BigDecimal getTaskBoardQty(){
        return this.taskBoardQty;
    }

    public void setTaskBoardQty(BigDecimal taskBoardQty){
        this.taskBoardQty = taskBoardQty;
    }

    public String getTransPlan() {
        return this.transPlan;
    }

    public void setTransPlan(String transPlan) {
        this.transPlan = transPlan;
    }

    public Long getMergedContainerId() {
        return mergedContainerId;
    }

    public void setMergedContainerId(Long mergedContainerId) {
        this.mergedContainerId = mergedContainerId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getIsShow() {
        return isShow;
    }

    public void setIsShow(int isShow) {
        this.isShow = isShow;
    }

    public Long getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Long taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Long getBusinessMode() {
        return businessMode;
    }

    public void setBusinessMode(Long businessMode) {
        this.businessMode = businessMode;
    }

    public Long getQcPreviousTaskId() {
        return this.qcPreviousTaskId;
    }

    public void setQcPreviousTaskId(Long qcPreviousTaskId) {
        this.qcPreviousTaskId = qcPreviousTaskId;
    }

    public Integer getQcSkip() {
        return this.qcSkip;
    }

    public void setQcSkip(Integer qcSkip) {
        this.qcSkip = qcSkip;
    }

    public BigDecimal getTaskAmount() {
        return taskAmount;
    }

    public void setTaskAmount(BigDecimal taskAmount) {
        this.taskAmount = taskAmount;
    }
}
