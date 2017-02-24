package com.lsh.wms.api.model.wumart;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by lixin-mac on 2016/10/28.
 */
public class CreateObdDetail implements Serializable{
    /**sto obd order_other_id*/
    private String refDoc;

    /**sto obd detail detail_other_id*/
    private String refItem;
    /**实际已交货量（按销售单位）*/
    private BigDecimal dlvQty;
    /**销售单位 */
    private String salesUnit;

    /**物料号*/
    private String material;

    /**订单类型*/
    private Integer orderType;

    public CreateObdDetail(){}

    public CreateObdDetail(BigDecimal dlvQty, String material, Integer orderType, String refDoc, String refItem, String salesUnit) {
        this.dlvQty = dlvQty;
        this.material = material;
        this.orderType = orderType;
        this.refDoc = refDoc;
        this.refItem = refItem;
        this.salesUnit = salesUnit;
    }

    public BigDecimal getDlvQty() {
        return dlvQty;
    }

    public void setDlvQty(BigDecimal dlvQty) {
        this.dlvQty = dlvQty;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public String getRefDoc() {
        return refDoc;
    }

    public void setRefDoc(String refDoc) {
        this.refDoc = refDoc;
    }

    public String getRefItem() {
        return refItem;
    }

    public void setRefItem(String refItem) {
        this.refItem = refItem;
    }

    public String getSalesUnit() {
        return salesUnit;
    }

    public void setSalesUnit(String salesUnit) {
        this.salesUnit = salesUnit;
    }
}
