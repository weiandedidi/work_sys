package com.lsh.wms.model.stock;

//import com.lsh.base.common.utils.ClockUtils;
//import com.lsh.base.common.utils.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;

public class StockQuant implements Serializable,Cloneable {

	private static final Logger logger = LoggerFactory.getLogger(StockQuant.class);
	/**  */
    private Long id;
	/** 商品id */
    private Long skuId;
	/** 存储库位id */
    private Long locationId;
	/** 容器设备id */
    private Long containerId = 0L;
	/** 数量 */
    private BigDecimal qty = BigDecimal.ZERO;
	/** 包装单位*/
	private BigDecimal packUnit= new BigDecimal(0);
	/** 包装名称 */
	private String packName = "";
	/** 库存价值 */
    private BigDecimal value = BigDecimal.ZERO;
	/** 库存成本 */
    private BigDecimal cost = BigDecimal.ZERO;
	/** 占位 task_id */
    private Long reserveTaskId = 0L;
	/** 0-可用，1-被冻结 */
    private Long isFrozen = 0L;
	/** 货物供应商id */
    private Long supplierId = 0L;
	/** 货物所属公司id */
    private Long ownerId = 0L;
	/** 批次号(链商内部批次号) */
    private Long lotId = 0L;
	/** 批次号(外部批次号,商品包装上的批次号) */
	private String LotCode = "";
	/** 入库时间 */
    private Long inDate = 0L;
	/** 保质期失效时间 */
    private Long expireDate = 0L;
	/**  */
    private Long createdAt = 0L;
	/**  */
    private Long updatedAt = 0L;

	private Long itemId;

	private BigDecimal delta = BigDecimal.ZERO;

	/** 0-非残次，1-残次 */
	private Long isDefect = 0L;
	/** 0-非退货，1-退货 */
	private Long isRefund = 0L;

	private Long isInhouse = 0L;

	private Long isNormal = 1L;

	public Long getIsNormal() {
		return isRefund *  isDefect *  isFrozen;
	}

	public void setIsNormal(Long isNormal) {
		this.isNormal = isNormal;
	}

	public Long getIsInhouse() {
		return isInhouse;
	}

	public void setIsInhouse(Long isInhouse) {
		this.isInhouse = isInhouse;
	}

	public String getLotCode() {
		return LotCode;
	}

	public void setLotCode(String lotCode) {
		LotCode = lotCode;
	}


    public Long getId(){
    	return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getSkuId(){
		return this.skuId;
	}
	
	public void setSkuId(Long skuId){
		this.skuId = skuId;
	}
	
	public Long getLocationId(){
		return this.locationId;
	}
	
	public void setLocationId(Long locationId){
		this.locationId = locationId;
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

	public BigDecimal getValue(){
		return this.value;
	}
	
	public void setValue(BigDecimal value){
		this.value = value;
	}
	
	public BigDecimal getCost(){
		return this.cost;
	}
	
	public void setCost(BigDecimal cost){
		this.cost = cost;
	}
	
	public Long getReserveTaskId(){
		return this.reserveTaskId;
	}
	
	public void setReserveTaskId(Long reserveTaskId){
		this.reserveTaskId = reserveTaskId;
	}
	
	public Long getIsFrozen(){
		return this.isFrozen;
	}
	
	public void setIsFrozen(Long isFrozen){
		this.isFrozen = isFrozen;
	}
	
	public Long getSupplierId(){
		return this.supplierId;
	}
	
	public void setSupplierId(Long supplierId){
		this.supplierId = supplierId;
	}
	
	public Long getOwnerId(){
		return this.ownerId;
	}
	
	public void setOwnerId(Long ownerId){
		this.ownerId = ownerId;
	}
	
	public Long getLotId(){
		return this.lotId;
	}
	
	public void setLotId(Long lotId){
		this.lotId = lotId;
	}
	
	public Long getInDate(){
		return this.inDate;
	}
	
	public void setInDate(Long inDate){
		this.inDate = inDate;
	}
	
	public Long getExpireDate(){
		return this.expireDate;
	}
	
	public void setExpireDate(Long expireDate){
		this.expireDate = expireDate;
	}
	
	public Long getCreatedAt(){
		return this.createdAt;
	}
	
	public void setCreatedAt(Long createdAt){
		this.createdAt = createdAt;
	}
	
	public Long getUpdatedAt(){
		return this.updatedAt;
	}
	
	public void setUpdatedAt(Long updatedAt){
		this.updatedAt = updatedAt;
	}


	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getIsDefect(){
		return isDefect;
	}

	public void setIsDefect(Long isDefect){
		this.isDefect = isDefect;
	}

	public Long getIsRefund(){
		return isRefund;
	}

	public void setIsRefund(Long isRefund){
		this.isRefund = isRefund;
	}

	public BigDecimal getPackUnit(){
		return packUnit;
	}

	public void setPackUnit(BigDecimal packUnit){
		this.packUnit = packUnit;
	}

	public String getPackName(){
		return packName;
	}

	public void  setPackName(String packName){
		this.packName = packName;
	}

	public BigDecimal getDelta() {
		return delta;
	}

	public void setDelta(BigDecimal delta) {
		this.delta = delta;
	}

	@Override
    public Object clone() {
        StockQuant stockQuant = null;
        try{
            stockQuant = (StockQuant)super.clone();
            stockQuant.setId(null);
            stockQuant.setQty(null);
        }catch ( CloneNotSupportedException ex){
            logger.error(ex.toString());
        }
        return stockQuant;
    }

	public boolean isAvailable() {
		if (this.getReserveTaskId() ==0
				&& this.getIsDefect() == 0
				&& this.getIsFrozen() == 0
				&& this.getIsRefund() == 0 ) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "StockQuant{" +
				"id=" + id +
				", skuId=" + skuId +
				", locationId=" + locationId +
				", containerId=" + containerId +
				", qty=" + qty +
				", packUnit=" + packUnit +
				", packName='" + packName + '\'' +
				", value=" + value +
				", cost=" + cost +
				", reserveTaskId=" + reserveTaskId +
				", isFrozen=" + isFrozen +
				", supplierId=" + supplierId +
				", ownerId=" + ownerId +
				", lotId=" + lotId +
				", inDate=" + inDate +
				", expireDate=" + expireDate +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", itemId=" + itemId +
				", isDefect=" + isDefect +
				", isRefund=" + isRefund +
				'}';
	}
}
