package com.lsh.wms.api.model.tu;

import java.util.List;
import java.util.Map;

/**
 * @Author 马启迪 maqidi@lsh123.com
 * @Date 2016/10/25 下午9:29
 */
public class TuHeadRequest {
    /**
     * 运单查询的开始时间
     */
    private Long startTime;

    /**
     * 运单查询的截止时间
     */
    private Long endTime;
    /**  */
    private Long id;
    /**
     * 运单编号
     */
    private String tuId;
    /**
     * 司机ID
     */
    private Long transUid;
    /**
     * 司机手机号码
     */
    private String cellphone;
    /**
     * 司机姓名
     */
    private String name;
    /**
     * 车牌号
     */
    private String carNumber;
    /**
     * 状态1.待装车5已装车9已发货
     */
    private Integer status;
    /**
     * 门店id字符串
     */
    private String storeIds;
    /**
     * 预装板数
     */
    private Long preBoard;
    /**
     * 实际板数
     */
    private Long realBoard;
    /**
     * 提交时间
     */
    private Long commitedAt;
    /**
     * 仓库ID 天津id
     */
    private String warehouseId;
    /**  */
    private Long createdAt;
    /**  */
    private Long updatedAt;
    /**
     * 装车完毕时间
     */
    private Long loadedAt;
    /**
     * 发货人|中控
     */
    private Long deliveryUid;
    /**
     * 发货时间
     */
    private Long deliveryAt;
    /**
     * 是否有效, 0否, 1是
     */
    private Integer isValid = 1;
    /** 扫货装车人的uid */
    private Long loadUid;

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Long getId() {
        return id;
    }

    public String getTuId() {
        return tuId;
    }

    public Long getTransUid() {
        return transUid;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getName() {
        return name;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStoreIds() {
        return storeIds;
    }

    public Long getPreBoard() {
        return preBoard;
    }

    public Long getRealBoard() {
        return realBoard;
    }

    public Long getCommitedAt() {
        return commitedAt;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getLoadedAt() {
        return loadedAt;
    }

    public Long getDeliveryUid() {
        return deliveryUid;
    }

    public Long getDeliveryAt() {
        return deliveryAt;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTuId(String tuId) {
        this.tuId = tuId;
    }

    public void setTransUid(Long transUid) {
        this.transUid = transUid;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setStoreIds(String storeIds) {
        this.storeIds = storeIds;
    }

    public void setPreBoard(Long preBoard) {
        this.preBoard = preBoard;
    }

    public void setRealBoard(Long realBoard) {
        this.realBoard = realBoard;
    }

    public void setCommitedAt(Long commitedAt) {
        this.commitedAt = commitedAt;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLoadedAt(Long loadedAt) {
        this.loadedAt = loadedAt;
    }

    public void setDeliveryUid(Long deliveryUid) {
        this.deliveryUid = deliveryUid;
    }

    public void setDeliveryAt(Long deliveryAt) {
        this.deliveryAt = deliveryAt;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Long getLoadUid() {
        return loadUid;
    }

    public void setLoadUid(Long loadUid) {
        this.loadUid = loadUid;
    }
}
