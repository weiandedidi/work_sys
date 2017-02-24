package com.lsh.wms.model.baseinfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class BaseinfoLocationBin extends BaseinfoLocation implements Serializable,IBaseinfoLocaltionModel {

	/**  */
    private Long id;
	/** 位置id */
    protected Long locationId;
	/** 仓位体积 */
    private BigDecimal volume;
	/** 承重，默认单位kg，默认0，能承受东西很轻 */
    private BigDecimal weigh;
	/** 描述 */
    private String description;
	/** 创建日期 */
    private Long createdAt;
	/** 更新日期 */
    private Long updatedAt;
	/** 主表类型 */
    private Long type;
    /** 温区1常温2冷藏 */
    private Integer zoneType;
	/** 是否可用1可用，0删除 */
	private Integer isValid;
	/** 原库位体积，用于拆分库位还原体积使用 */
	private BigDecimal oldVolume;
	/** 合并后关联库位的locationId */
	private Long relLocationId;


	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public Long getLocationId(){
		return this.locationId;
	}
	
	public void setLocationId(Long locationId){
		this.locationId = locationId;
	}

	public BigDecimal getVolume(){
		return this.volume;
	}
	
	public void setVolume(BigDecimal volume){
		this.volume = volume;
	}
	
	public BigDecimal getWeigh(){
		return this.weigh;
	}
	
	public void setWeigh(BigDecimal weigh){
		this.weigh = weigh;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public void setDescription(String description){
		this.description = description;
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
	
	public Long getType(){
		return this.type;
	}
	
	public void setType(Long type){
		this.type = type;
	}

	public Integer getZoneType() {
		return zoneType;
	}

	public void setZoneType(Integer zoneType) {
		this.zoneType = zoneType;
	}

	@Override
	public Integer getIsValid() {
		return isValid;
	}

	@Override
	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}

	public void setDefaultClassification() {
		this.classification = 2;
	}

	public BigDecimal getOldVolume() {
		return oldVolume;
	}

	public Long getRelLocationId() {
		return relLocationId;
	}

	public void setOldVolume(BigDecimal oldVolume) {
		this.oldVolume = oldVolume;
	}

	public void setRelLocationId(Long relLocationId) {
		this.relLocationId = relLocationId;
	}
}
