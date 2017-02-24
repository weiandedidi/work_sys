package com.lsh.wms.model.baseinfo;

import java.io.Serializable;
import java.util.Date;

public class BaseinfoItemType implements Serializable {

	/** ID */
    private Long id;
	/** 课组ID */
	private Long itemTypeId;
	/** 课组名称 */
    private String itemName;
	/** 是否需要生产日期 0不需要 1需要 */
    private Long isNeedProtime;
	
	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	

	public Long getIsNeedProtime(){
		return this.isNeedProtime;
	}
	
	public void setIsNeedProtime(Long isNeedProtime){
		this.isNeedProtime = isNeedProtime;
	}


	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Long getItemTypeId() {
		return itemTypeId;
	}

	public void setItemTypeId(Long itemTypeId) {
		this.itemTypeId = itemTypeId;
	}
}
