package com.lsh.wms.api.service.pick;

import com.lsh.base.common.exception.BizCheckedException;

import java.util.List;
import java.util.Map;

/**
 * Created by zengwenjun on 16/11/12.
 */
public interface IPCPickRpcService {
    public Map<String,Object> getContainerGoods(Long contaienrId) throws BizCheckedException;
    public Map<String,Object> getContainerGoods(Long contaienrId, Long waveId) throws BizCheckedException;
}
