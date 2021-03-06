
package com.lsh.wms.integration.wumart.obd;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "ZMM_OUTB_OBD", targetNamespace = "urn:sap-com:document:sap:soap:functions:mc-style", wsdlLocation = "file:/home/work/wumart/obd.wsdl")
public class ZMMOUTBOBD_Service
    extends Service
{

    private final static URL ZMMOUTBOBD_WSDL_LOCATION;
    private final static WebServiceException ZMMOUTBOBD_EXCEPTION;
    private final static QName ZMMOUTBOBD_QNAME = new QName("urn:sap-com:document:sap:soap:functions:mc-style", "ZMM_OUTB_OBD");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/home/work/wumart/obd.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        ZMMOUTBOBD_WSDL_LOCATION = url;
        ZMMOUTBOBD_EXCEPTION = e;
    }

    public ZMMOUTBOBD_Service() {
        super(__getWsdlLocation(), ZMMOUTBOBD_QNAME);
    }

    public ZMMOUTBOBD_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), ZMMOUTBOBD_QNAME, features);
    }

    public ZMMOUTBOBD_Service(URL wsdlLocation) {
        super(wsdlLocation, ZMMOUTBOBD_QNAME);
    }

    public ZMMOUTBOBD_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, ZMMOUTBOBD_QNAME, features);
    }

    public ZMMOUTBOBD_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ZMMOUTBOBD_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns ZMMOUTBOBD
     */
    @WebEndpoint(name = "binding")
    public ZMMOUTBOBD getBinding() {
        return super.getPort(new QName("urn:sap-com:document:sap:soap:functions:mc-style", "binding"), ZMMOUTBOBD.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ZMMOUTBOBD
     */
    @WebEndpoint(name = "binding")
    public ZMMOUTBOBD getBinding(WebServiceFeature... features) {
        return super.getPort(new QName("urn:sap-com:document:sap:soap:functions:mc-style", "binding"), ZMMOUTBOBD.class, features);
    }

    /**
     * 
     * @return
     *     returns ZMMOUTBOBD
     */
    @WebEndpoint(name = "binding_SOAP12")
    public ZMMOUTBOBD getBindingSOAP12() {
        return super.getPort(new QName("urn:sap-com:document:sap:soap:functions:mc-style", "binding_SOAP12"), ZMMOUTBOBD.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ZMMOUTBOBD
     */
    @WebEndpoint(name = "binding_SOAP12")
    public ZMMOUTBOBD getBindingSOAP12(WebServiceFeature... features) {
        return super.getPort(new QName("urn:sap-com:document:sap:soap:functions:mc-style", "binding_SOAP12"), ZMMOUTBOBD.class, features);
    }

    private static URL __getWsdlLocation() {
        if (ZMMOUTBOBD_EXCEPTION!= null) {
            throw ZMMOUTBOBD_EXCEPTION;
        }
        return ZMMOUTBOBD_WSDL_LOCATION;
    }

}
