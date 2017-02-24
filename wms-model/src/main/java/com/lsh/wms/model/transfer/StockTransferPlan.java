package com.lsh.wms.model.transfer;

import org.codehaus.jackson.map.annotate.JsonSerialize;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class StockTransferPlan implements Serializable {
	/** 移库计划id */
    private Long planId = 0L;
	/** 移库任务id */
    private Long taskId = 0L;
	/** 商品国条 */
	@NotNull
    private Long itemId ;
	/** 移入库位id */
	@NotNull
    private Long fromLocationId;
	/** 移入库位id */
	@NotNull
	@Size(min=1)
    private Long toLocationId;
	/** */
	private Long containerId;
	/** 数量 */
    private BigDecimal qty;
	/** 商品单位转换id */
    private String packName = "";
    /** 商品包装单位 */
    private BigDecimal packUnit = BigDecimal.ONE;
	/** 以包装单位计量的库移数量 */
	@NotNull
    private BigDecimal uomQty = BigDecimal.ZERO;
	/**优先级*/
	private Long priority =2L;
	/** 移库操作单位*/
	private Long subType = 2L;
	/** 发起者 */
	private Long planner = 0L;
	/**任务截止时间*/
	private Long dueTime = 0L;
	/**任务目标描述*/
	private String targetDesc = "";


	public Long getDueTime() {
		return dueTime;
	}

	public void setDueTime(Long dueTime) {
		this.dueTime = dueTime;
	}

	public Long getPlanId(){
		return this.planId;
	}
	
	public void setPlanId(Long planId){
		this.planId = planId;
	}
	
	public Long getTaskId(){
		return this.taskId;
	}
	
	public void setTaskId(Long taskId){
		this.taskId = taskId;
	}
	
	public Long getItemId(){
		return this.itemId;
	}
	
	public void setItemId(Long itemId){
		this.itemId = itemId;
	}
	
	public Long getFromLocationId(){
		return this.fromLocationId;
	}
	
	public void setFromLocationId(Long fromLocationId){
		this.fromLocationId = fromLocationId;
	}
	
	public Long getToLocationId(){
		return this.toLocationId;
	}
	
	public void setToLocationId(Long toLocationId){
		this.toLocationId = toLocationId;
	}

	public Long getContainerId(){
		return this.containerId;
	}

	public void setContainerId(Long containerId){
		this.containerId = containerId;
	}

	public BigDecimal getQty(){
		return this.qty;
	}
	
	public void setQty(BigDecimal qty){
		this.qty = qty;
	}
	
	public String getPackName(){
		return this.packName;
	}
	
	public void setPackName(String packName){
		this.packName = packName;
	}
	
	public BigDecimal getUomQty(){
		return this.uomQty;
	}
	
	public void setUomQty(BigDecimal uomQty){
		this.uomQty = uomQty;
	}

    public BigDecimal getPackUnit() {
        return packUnit;
    }

    public void setPackUnit(BigDecimal packUnit) {
        this.packUnit = packUnit;
    }

	public Long getPlanner() {
		return planner;
	}

	public void setPlanner(Long planner) {
		this.planner = planner;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	public Long getSubType() {
		return subType;
	}

	public void setSubType(Long subType) {
		this.subType = subType;
	}

	public void setTargetDesc(String targetDesc){
		this.targetDesc = targetDesc;
	}

	public String getTargetDesc(){
		return this.targetDesc;
	}


	@Override
	public String toString() {
		return "StockTransferPlan{" +
				"planId=" + planId +
				", taskId=" + taskId +
				", itemId=" + itemId +
				", fromLocationId=" + fromLocationId +
				", toLocationId=" + toLocationId +
				", qty=" + qty +
				", packName='" + packName + '\'' +
				", packUnit=" + packUnit +
				", uomQty=" + uomQty +
				", priority=" + priority +
				", subType=" + subType +
				", planner=" + planner +
				", dueTime=" + dueTime +
				'}';
	}
}
