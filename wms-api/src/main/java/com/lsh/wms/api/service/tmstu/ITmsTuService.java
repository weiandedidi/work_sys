package com.lsh.wms.api.service.tmstu;

import com.lsh.base.common.exception.BizCheckedException;

/**
 * Created by fengkun on 2016/11/14.
 */
public interface ITmsTuService {
    String receiveTuHead() throws BizCheckedException;
    String superMarketUnloadList() throws BizCheckedException;
    String storeUnloadList() throws BizCheckedException;

}
