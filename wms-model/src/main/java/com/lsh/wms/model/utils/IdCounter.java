package com.lsh.wms.model.utils;

import java.io.Serializable;
import java.util.Date;

public class IdCounter implements Serializable {

	/**  */
    private Long id;
	/** id的键 */
    private String idKey;
	/** 计数器 */
    private Long counter;
	/**  */
    private Long createdAt;
	/**  */
    private Long updatedAt;
	
	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public String getIdKey(){
		return this.idKey;
	}
	
	public void setIdKey(String idKey){
		this.idKey = idKey;
	}
	
	public Long getCounter(){
		return this.counter;
	}
	
	public void setCounter(Long counter){
		this.counter = counter;
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
	
	
}
