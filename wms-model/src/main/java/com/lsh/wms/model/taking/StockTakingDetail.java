package com.lsh.wms.model.taking;

import com.lsh.base.common.utils.DateUtils;
import java.io.Serializable;
import java.math.BigDecimal;

public class StockTakingDetail implements Serializable {

	/**  */
    private Long id;
	/** 哪次盘点的明细 */
    private Long takingId;
	/** 任务Id */
	private Long taskId = 0L;
	/** 盘点行项目编号 */
    private Long detailId = 0L;
	/** 第几轮盘点的结果 */
	private Long round = 1L ;
	/** 是否是最终盘点任务 */
	private Integer isFinal = 0 ;
	/** 实际值 */
    private BigDecimal realQty = BigDecimal.ZERO;
	/** 理论值 */
    private BigDecimal theoreticalQty = BigDecimal.ZERO;
	/** 容器设备id */
    private Long containerId = 0L;
	/** 批次号 */
    private Long lotId = 0L;
	/** 理论商品id */
    private Long skuId = 0L;
	/** 实际商品id */
    private Long realSkuId = 0L;
	/** 存储库位id */
    private Long locationId = 0L;
	/** 盘点人员 */
    private Long operator = 0L;
	/** */
	private Long itemId = 0L;
	/** */
	private Long realItemId = 0L;
	/**货主id */
	private Long ownerId = 0L;
	/**单价金额 */
	private BigDecimal price = BigDecimal.ZERO;
	/**差异金额 */
	private BigDecimal differencePrice = BigDecimal.ZERO;
	/**包装名称 */
	private String packName = "EA";
	/**国条 */
	private String barcode = "";
	/**商品名称 */
	private String skuName = "";
	/**物美码 */
	private String skuCode = "";
	/** 库位编码*/
	private String locationCode = "";
	/** 包装单位*/
	private BigDecimal packUnit = new BigDecimal(1);
	/** */
	private int isValid = 1;
	/** 详情执行状态*/
	private Long status = 1L;
	/** 分区id*/
	private Long zoneId = 0L;
	/** 第一次盘点任务的任务类型*/
	private Long refTaskType = 0L;
	/** 箱码*/
	private String packCode = "";
	/**  实际箱数*/
	private BigDecimal umoQty  = BigDecimal.ZERO;
	/**  */
    private Long createdAt = DateUtils.getCurrentSeconds();
	/**  */
    private Long updatedAt = DateUtils.getCurrentSeconds();
	
	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getTakingId(){
		return this.takingId;
	}
	
	public void setTakingId(Long takingId){
		this.takingId = takingId;
	}
	
	public Long getDetailId(){
		return this.detailId;
	}
	
	public void setDetailId(Long detailId){
		this.detailId = detailId;
	}
	
	public Long getRound(){
		return this.round;
	}
	
	public void setRound(Long round){
		this.round = round;
	}
	
	public BigDecimal getRealQty(){
		return this.realQty;
	}
	
	public void setRealQty(BigDecimal realQty){
		this.realQty = realQty;
	}
	
	public BigDecimal getTheoreticalQty(){
		return this.theoreticalQty;
	}
	
	public void setTheoreticalQty(BigDecimal theoreticalQty){
		this.theoreticalQty = theoreticalQty;
	}
	
	public Long getContainerId(){
		return this.containerId;
	}
	
	public void setContainerId(Long containerId){
		this.containerId = containerId;
	}
	
	public Long getLotId(){
		return this.lotId;
	}
	
	public void setLotId(Long lotId){
		this.lotId = lotId;
	}
	
	public Long getSkuId(){
		return this.skuId;
	}
	
	public void setSkuId(Long skuId){
		this.skuId = skuId;
	}
	
	public Long getRealSkuId(){
		return this.realSkuId;
	}
	
	public void setRealSkuId(Long realSkuId){
		this.realSkuId = realSkuId;
	}
	
	public Long getLocationId(){
		return this.locationId;
	}
	
	public void setLocationId(Long locationId){
		this.locationId = locationId;
	}
	
	public Long getOperator(){
		return this.operator;
	}
	
	public void setOperator(Long operator){
		this.operator = operator;
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

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getRealItemId() {
		return realItemId;
	}

	public void setRealItemId(Long realItemId) {
		this.realItemId = realItemId;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public int getIsValid() {
		return isValid;
	}

	public void setIsValid(int isValid) {
		this.isValid = isValid;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public BigDecimal getPackUnit() {
		return packUnit;
	}

	public void setPackUnit(BigDecimal packUnit) {
		this.packUnit = packUnit;
	}

	public Integer getIsFinal() {
		return isFinal;
	}

	public void setIsFinal(Integer isFinal) {
		this.isFinal = isFinal;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public Long getZoneId() {
		return zoneId;
	}

	public void setZoneId(Long zoneId) {
		this.zoneId = zoneId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getDifferencePrice() {
		return differencePrice;
	}

	public void setDifferencePrice(BigDecimal differencePrice) {
		this.differencePrice = differencePrice;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}


	public Long getRefTaskType() {
		return refTaskType;
	}

	public void setRefTaskType(Long refTaskType) {
		this.refTaskType = refTaskType;
	}

	public String getPackCode() {
		return packCode;
	}

	public void setPackCode(String packCode) {
		this.packCode = packCode;
	}

	public BigDecimal getUmoQty() {
		return umoQty;
	}

	public void setUmoQty(BigDecimal umoQty) {
		this.umoQty = umoQty;
	}
}
