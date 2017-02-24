package com.lsh.wms.api.service.sms;


import com.lsh.base.common.exception.BizCheckedException;

import java.math.BigDecimal;

public interface ISmsRestService {

    public String sendMsg(String phone, String msg) throws BizCheckedException;

    String inventory(Long ItemId, Long fromLocationId, Long toLocationId, BigDecimal qty) throws BizCheckedException;

    String initStock() throws BizCheckedException;

    String initTask() throws BizCheckedException;

    String alloc(String orderId) throws BizCheckedException;

    String diff(String orderId) throws BizCheckedException;

    String correctAvailQty() throws BizCheckedException;

    String mergeLocationQuant(String locationId) throws BizCheckedException;

    String taskDone() throws BizCheckedException;

    String moveByItemId() throws BizCheckedException;
}
