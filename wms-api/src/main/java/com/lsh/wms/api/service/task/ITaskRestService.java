package com.lsh.wms.api.service.task;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.model.task.TaskMsg;

import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Map;

/**
 * Created by zengwenjun on 16/7/24.
 */
public interface ITaskRestService {
    String getTaskList(Map<String, Object> mapQuery);
    String getTaskCount(Map<String, Object> mapQuery);
    String getTaskHeadList(Map<String, Object> mapQuery);
    String getTask(long taskId) throws BizCheckedException;
    String getTaskType(long taskId) throws BizCheckedException;
    String getOldTask(long taskId) throws BizCheckedException;
    String getTaskMove(long taskId) throws BizCheckedException;
    String done(long taskId) throws BizCheckedException;
    String getPerformance(Map<String, Object> condition) throws BizCheckedException;
    String sendMsg(Map<String, Object> mapQuery) throws BizCheckedException;
    String getTaskInfo(long taskId) throws BizCheckedException;
    String processTaskMsg(Long id) throws BizCheckedException;
    String countTaskMsg(Map<String, Object> mapQuery) throws BizCheckedException;
    String getTaskMsgList(Map<String, Object> mapQuery) throws BizCheckedException;
    String getDoneTasksByIds(Map<String, Object> mapQuery) throws BizCheckedException;
}
