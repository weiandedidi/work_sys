package com.lsh.wms.worker.service.wave;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsh.wms.api.service.wave.IWaveRpcService;
import com.taobao.pamirs.schedule.IScheduleTaskDealSingle;
import com.taobao.pamirs.schedule.TaskItemDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zengwenjun on 16/11/12.
 */
@Component
public class WaveAutoGenerator implements IScheduleTaskDealSingle<String> {
    @Reference
    IWaveRpcService iWaveRpcService;
    private static Logger logger = LoggerFactory.getLogger(WaveAutoGenerator.class);

    public boolean execute(String s, String s2) throws Exception {
        try {
            logger.info("in waveAutoGenerator:"+s);
            iWaveRpcService.runWaveGenerator();
        }catch (Exception e){
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    public List<String> selectTasks(String taskParameter, String ownSign, int taskItemNum, List<TaskItemDefine> taskItemList, int eachFetchDataNum) throws Exception{
        List<String> list = new ArrayList<String>();
        String id = taskItemList.get(0).getTaskItemId();
        list.add("[" + id + "]111");
        return list;
    }

    public Comparator<String> getComparator() {
        return null;
    }
}
