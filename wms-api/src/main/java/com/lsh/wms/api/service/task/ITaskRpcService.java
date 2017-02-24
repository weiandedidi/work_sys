package com.lsh.wms.api.service.task;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.seed.SeedingTaskHead;
import com.lsh.wms.model.stock.StockMove;
import com.lsh.wms.model.taking.StockTakingHead;
import com.lsh.wms.model.task.TaskEntry;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.task.TaskMsg;

import java.util.List;
import java.util.Map;

/**
 * Created by mali on 16/7/20.
 */
public interface ITaskRpcService {
    Long create(Long taskType, TaskEntry taskEntry) throws BizCheckedException;
    List<Long> batchCreate(Long taskType, List<TaskEntry> taskEntries) throws BizCheckedException;
    List<Long> batchCreate(StockTakingHead head, List<TaskEntry> taskEntries) throws BizCheckedException;
    Long getTaskTypeById(Long taskId) throws BizCheckedException;
    TaskEntry getTaskEntryById(Long taskId) throws BizCheckedException;
    TaskEntry getOldTaskEntryById(Long taskId) throws BizCheckedException;
    void assign(Long taskId, Long staffId) throws BizCheckedException;
    void assign(Long taskId, Long staffId, Long containerId) throws BizCheckedException;
    void assignMul(List<Map<String, Long>> params) throws BizCheckedException;
    void cancel(Long taskId) throws BizCheckedException;
    void hold(Long taskId) throws BizCheckedException;
    void batchHold(Long taskType, List<Long> taskIds) throws BizCheckedException;
    void cancel(Long taskId,StockTakingHead head) throws BizCheckedException;
    List<TaskEntry> getTaskList(Long taskType, Map<String, Object> mapQuery);
    int getTaskCount(Long taskType, Map<String, Object> mapQuery);
    List<TaskEntry> getTaskHeadList(Long taskType, Map<String, Object> mapQuery);
    void done(Long taskId) throws BizCheckedException;
    void done(TaskEntry entry) throws BizCheckedException;
    void done(Long taskId, Long locationId) throws BizCheckedException;
    void done(Long taskId, Long locationId, Long staffId) throws BizCheckedException;
    void done(Long taskId, List<StockMove> moveList) throws BizCheckedException;
    void update(Long taskType,TaskEntry entry) throws BizCheckedException;
    void batchAssign(Long taskType,List<Long> tasks,Long staffId) throws BizCheckedException;
    void batchCancel(Long taskType,List<Long> tasks) throws BizCheckedException;
    List<Map<String, Object>> getPerformance(Map<String, Object> condition) throws BizCheckedException;
    TaskInfo getTaskInfo(Long taskId) throws BizCheckedException;
    void processTaskMsg(Long id) throws BizCheckedException;
    int countTaskMsg(Map<String, Object> mapQuery) throws BizCheckedException;
    List<TaskMsg> getTaskMsgList(Map<String, Object> mapQuery) throws BizCheckedException;
    void createTask(StockTakingHead head,TaskEntry entry) throws BizCheckedException;
    void calcelTask(StockTakingHead head,List<TaskEntry> entries) throws BizCheckedException;

}
