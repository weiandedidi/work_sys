
package com.lsh.wms.integration.wumart.obd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CreatedItems" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapidlvitemcreated" minOccurs="0"/>
 *         &lt;element name="DebugFlg" type="{urn:sap-com:document:sap:rfc:functions}char1" minOccurs="0"/>
 *         &lt;element name="Deliveries" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapishpdelivnumb" minOccurs="0"/>
 *         &lt;element name="DueDate" type="{urn:sap-com:document:sap:rfc:functions}date" minOccurs="0"/>
 *         &lt;element name="ExtensionIn" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapiparex" minOccurs="0"/>
 *         &lt;element name="ExtensionOut" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapiparex" minOccurs="0"/>
 *         &lt;element name="NoDequeue" type="{urn:sap-com:document:sap:rfc:functions}char1" minOccurs="0"/>
 *         &lt;element name="Return" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapiret2" minOccurs="0"/>
 *         &lt;element name="SerialNumbers" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapidlvserialnumber" minOccurs="0"/>
 *         &lt;element name="ShipPoint" type="{urn:sap-com:document:sap:rfc:functions}char4" minOccurs="0"/>
 *         &lt;element name="StockTransItems" type="{urn:sap-com:document:sap:soap:functions:mc-style}TableOfBapidlvreftosto"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "createdItems",
    "debugFlg",
    "deliveries",
    "dueDate",
    "extensionIn",
    "extensionOut",
    "noDequeue",
    "_return",
    "serialNumbers",
    "shipPoint",
    "stockTransItems"
})
@XmlRootElement(name = "ZBapiOutbCreateObd")
public class ZBapiOutbCreateObd {

    @XmlElement(name = "CreatedItems")
    protected TableOfBapidlvitemcreated createdItems;
    @XmlElement(name = "DebugFlg")
    protected String debugFlg;
    @XmlElement(name = "Deliveries")
    protected TableOfBapishpdelivnumb deliveries;
    @XmlElement(name = "DueDate")
    protected XMLGregorianCalendar dueDate;
    @XmlElement(name = "ExtensionIn")
    protected TableOfBapiparex extensionIn;
    @XmlElement(name = "ExtensionOut")
    protected TableOfBapiparex extensionOut;
    @XmlElement(name = "NoDequeue")
    protected String noDequeue;
    @XmlElement(name = "Return")
    protected TableOfBapiret2 _return;
    @XmlElement(name = "SerialNumbers")
    protected TableOfBapidlvserialnumber serialNumbers;
    @XmlElement(name = "ShipPoint")
    protected String shipPoint;
    @XmlElement(name = "StockTransItems", required = true)
    protected TableOfBapidlvreftosto stockTransItems;

    /**
     * Gets the value of the createdItems property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapidlvitemcreated }
     *     
     */
    public TableOfBapidlvitemcreated getCreatedItems() {
        return createdItems;
    }

    /**
     * Sets the value of the createdItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapidlvitemcreated }
     *     
     */
    public void setCreatedItems(TableOfBapidlvitemcreated value) {
        this.createdItems = value;
    }

    /**
     * Gets the value of the debugFlg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDebugFlg() {
        return debugFlg;
    }

    /**
     * Sets the value of the debugFlg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDebugFlg(String value) {
        this.debugFlg = value;
    }

    /**
     * Gets the value of the deliveries property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapishpdelivnumb }
     *     
     */
    public TableOfBapishpdelivnumb getDeliveries() {
        return deliveries;
    }

    /**
     * Sets the value of the deliveries property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapishpdelivnumb }
     *     
     */
    public void setDeliveries(TableOfBapishpdelivnumb value) {
        this.deliveries = value;
    }

    /**
     * Gets the value of the dueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDueDate() {
        return dueDate;
    }

    /**
     * Sets the value of the dueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDueDate(XMLGregorianCalendar value) {
        this.dueDate = value;
    }

    /**
     * Gets the value of the extensionIn property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapiparex }
     *     
     */
    public TableOfBapiparex getExtensionIn() {
        return extensionIn;
    }

    /**
     * Sets the value of the extensionIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapiparex }
     *     
     */
    public void setExtensionIn(TableOfBapiparex value) {
        this.extensionIn = value;
    }

    /**
     * Gets the value of the extensionOut property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapiparex }
     *     
     */
    public TableOfBapiparex getExtensionOut() {
        return extensionOut;
    }

    /**
     * Sets the value of the extensionOut property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapiparex }
     *     
     */
    public void setExtensionOut(TableOfBapiparex value) {
        this.extensionOut = value;
    }

    /**
     * Gets the value of the noDequeue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoDequeue() {
        return noDequeue;
    }

    /**
     * Sets the value of the noDequeue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoDequeue(String value) {
        this.noDequeue = value;
    }

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapiret2 }
     *     
     */
    public TableOfBapiret2 getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapiret2 }
     *     
     */
    public void setReturn(TableOfBapiret2 value) {
        this._return = value;
    }

    /**
     * Gets the value of the serialNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapidlvserialnumber }
     *     
     */
    public TableOfBapidlvserialnumber getSerialNumbers() {
        return serialNumbers;
    }

    /**
     * Sets the value of the serialNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapidlvserialnumber }
     *     
     */
    public void setSerialNumbers(TableOfBapidlvserialnumber value) {
        this.serialNumbers = value;
    }

    /**
     * Gets the value of the shipPoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShipPoint() {
        return shipPoint;
    }

    /**
     * Sets the value of the shipPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShipPoint(String value) {
        this.shipPoint = value;
    }

    /**
     * Gets the value of the stockTransItems property.
     * 
     * @return
     *     possible object is
     *     {@link TableOfBapidlvreftosto }
     *     
     */
    public TableOfBapidlvreftosto getStockTransItems() {
        return stockTransItems;
    }

    /**
     * Sets the value of the stockTransItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableOfBapidlvreftosto }
     *     
     */
    public void setStockTransItems(TableOfBapidlvreftosto value) {
        this.stockTransItems = value;
    }

}
