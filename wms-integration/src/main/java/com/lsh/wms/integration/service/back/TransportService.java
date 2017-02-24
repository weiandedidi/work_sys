package com.lsh.wms.integration.service.back;

import com.alibaba.dubbo.config.annotation.Service;
import com.lsh.base.common.utils.StrUtils;
import com.lsh.wms.api.service.back.ITransportService;
import com.lsh.wms.core.constant.SysLogConstant;
import com.lsh.wms.core.service.system.SysLogService;
import com.lsh.wms.model.system.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by mali on 16/11/17.
 */
@Service(protocol = "dubbo")
public class TransportService implements ITransportService{
    private static final Logger logger = LoggerFactory.getLogger(TransportService.class);

    @Autowired
    private TransporterManager transporterManager;

    @Autowired
    private SysLogService sysLogService;

    public void dealOne(Long sysLogId) {

        logger.info(StrUtils.formatString("begin deal with sysLogId[{0}]]", sysLogId));
        SysLog sysLog = sysLogService.getSysLogById(sysLogId);
        try {
            if (sysLog == null) {
                logger.warn(StrUtils.formatString("cannot find syslog for log_id[{0}]", sysLogId));
                return;
            }

            logger.info(StrUtils.formatString("begin send data back [logId:{0},businessId:{1}]", sysLogId, sysLog.getBusinessId()));
            transporterManager.dealOne(sysLog);

            logger.info(StrUtils.formatString("begin update sysLogInfo[logId:{0},businessId:{1}] ", sysLogId, sysLog.getBusinessId()));
            sysLogService.updateSysLog(sysLog);
        } catch (Exception e) {
            //异常在transporterManager中处理了
            logger.error("Exception", e);
//            sysLog.setRetryTimes(sysLog.getRetryTimes() + 1);
            sysLog.setSysMessage("系统抛出异常:" + e.getMessage());
            sysLog.setSysCode("系统异常");
            sysLog.setLogCode("系统异常");
            sysLog.setLogMessage("查询对应单据是否存在");
            sysLog.setStatus(SysLogConstant.LOG_STATUS_FAILED);
            sysLogService.updateSysLog(sysLog);
            logger.error(StrUtils.formatString("Exception ocurred during deail with SysLog[{}]", sysLogId));
        }

    }
}
