package com.lsh.wms.model.stock;

import com.lsh.base.common.utils.DateUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.acl.LastOwnerException;
import java.util.Date;

public class StockLot implements Serializable {

	/**  */
    private Long id;
	/** 批次id */
    private Long lotId = 0L;
	/** 商品id */
    private Long skuId = 0L ;
	/** 生产批次号 */
    private String serialNo = "";
	/** 入库时间 */
    private Long inDate = DateUtils.getCurrentSeconds();
	/** 生产时间 */
    private Long productDate = DateUtils.getCurrentSeconds();
	/** 保质期失效时间 */
    private Long expireDate = 2145801600L; // 2037-13-31
	/**  */
    private Long createdAt = DateUtils.getCurrentSeconds();
	/**  */
    private Long updatedAt = DateUtils.getCurrentSeconds();

	private Long itemId = 0L;

	private Long poId = 0L;

	private Long receiptId = 0L;
	/** 包装单位*/
	private BigDecimal packUnit= new BigDecimal(0);
	/** 包装名称 */
	private String packName = "";
	/** 供应商信息 */
	private Long supplierId = 0L;
	/** 2级包装名称 */
	private String l2Name = "";
	/** 2级包装单位－含有基本单位个数 */
	private BigDecimal l2Unit = new BigDecimal(0);
	/** 基本单位名称 */
	private String unitName = "";
	/** 国条码 */
	private String code = "";

	private boolean isOld = false;

	public boolean isOld() {
		return isOld;
	}

	public void setIsOld(boolean isOld) {
		this.isOld = isOld;
	}

	public Long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	public Long getId(){
		return this.id;
	}

	public void setId(Long id){
		this.id = id;
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

	public String getSerialNo(){
		return this.serialNo;
	}

	public void setSerialNo(String serialNo){
		this.serialNo = serialNo;
	}

	public Long getInDate(){
		return this.inDate;
	}

	public void setInDate(Long inDate){
		this.inDate = inDate;
	}

	public Long getProductDate(){
		return this.productDate;
	}

	public void setProductDate(Long productDate){
		this.productDate = productDate;
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

	public Long getPoId() {
		return poId;
	}

	public void setPoId(Long poId) {
		this.poId = poId;
	}

	public Long getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(Long receiptId) {
		this.receiptId = receiptId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
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

	public String getL2Name(){
		return this.l2Name;
	}

	public void setL2Name(String l2Name){
		this.l2Name = l2Name;
	}

	public BigDecimal getL2Unit(){
		return this.l2Unit;
	}

	public void setL2Unit(BigDecimal l2Unit){
		this.l2Unit = l2Unit;
	}

	public String getUnitName(){
		return this.unitName;
	}

	public void setUnitName(String unitName){
		this.unitName = unitName;
	}

	public String getCode(){
		return this.code;
	}

	public void setCode(String code){
		this.code = code;
	}



}
