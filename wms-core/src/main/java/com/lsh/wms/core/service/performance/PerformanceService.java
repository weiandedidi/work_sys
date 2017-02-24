package com.lsh.wms.core.service.performance;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.BeanMapTransUtils;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.core.constant.TaskConstant;
import com.lsh.wms.core.dao.system.StaffPerformanceDao;
import com.lsh.wms.core.dao.task.TaskInfoDao;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.system.StaffPerformance;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.wave.WaveDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by lixin-mac on 16/8/24.
 */
@Component
@Transactional(readOnly = true)
public class PerformanceService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceService.class);

    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private WaveService waveService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private StaffPerformanceDao staffPerformanceDao;


    /*public List<Map<String, Object>> getPerformance(Map<String, Object> condition) {
        List<Map<String, Object>> taskInfoList = taskInfoDao.getPerformance(condition);
        List<Map<String, Object>> newTaskInfoList = new ArrayList<Map<String, Object>>();
        for (Map<String,Object> map : taskInfoList){
            Long type = (Long) map.get("type");
            //拣货跟QC统计一个任务中商品的个数
            if(type == TaskConstant.TYPE_QC || type == TaskConstant.TYPE_PICK){
                Map<String,Object> mapQuery = new HashMap<String, Object>();
                mapQuery.put("operator",map.get("uid"));
                mapQuery.put("date",map.get("date"));
                mapQuery.put("type",map.get("type"));
                mapQuery.put("subType",map.get("sub_type"));
                List<TaskInfo> taskInfos = taskInfoDao.getTaskInfoList(mapQuery);
                Set<Long> itemSet = new HashSet<Long>();
                int skuCount = 1;
                for (TaskInfo taskInfo : taskInfos){
                    Long taskId = taskInfo.getTaskId();
                    Map<String,Object> waveMap = new HashMap<String, Object>();
                    if(taskInfo.getType() == TaskConstant.TYPE_PICK) {
                        waveMap.put("pickTaskId",taskId);
                    }else{
                        waveMap.put("qcTaskId",taskId);
                    }
                    List<WaveDetail> waveDetails = waveService.getWaveDetails(waveMap);
                    if(waveDetails == null || waveDetails.size() <= 0){
                        skuCount = 1;
                    }else{
                        for (WaveDetail waveDetail : waveDetails) {
                            itemSet.add(waveDetail.getItemId());
                        }
                        skuCount = itemSet.size();
                    }
                }
                map.put("skuCount",skuCount);
            }else{
                map.put("skuCount" ,1);
            }
            newTaskInfoList.add(map);
        }
        //return taskInfoList;
        return newTaskInfoList;
    }*/
    //写入绩效统计结果
    @Transactional(readOnly = false)
    public void createPerformance(Map<String, Object> condition){
        List<StaffPerformance> oldPerformanceList = staffPerformanceDao.getStaffPerformanceList(condition);
        if(oldPerformanceList != null && oldPerformanceList.size() > 0){
            //已生成绩效统计结果
            logger.info("已生成历史绩效统计结果");
            return;
        }
        List<Map<String, Object>> getPerformanceList = this.getPerformance(condition);
        if(getPerformanceList == null || getPerformanceList.size() == 0) {
            //绩效统计结果为空
            logger.info("历史绩效统计结果为空");
            return;
        }
        List<StaffPerformance> performanceList = new ArrayList<StaffPerformance>();
        for(Map<String, Object> map : getPerformanceList){
            StaffPerformance staffPerformance = BeanMapTransUtils.map2Bean(map,StaffPerformance.class);
            staffPerformance.setSubType(Long.parseLong(map.get("sub_type").toString()));
            staffPerformance.setBusinessMode(Integer.parseInt(map.get("business_mode").toString()));
            staffPerformance.setOperator(Long.parseLong(map.get("uid").toString()));
            staffPerformance.setCreatedAt(DateUtils.getCurrentSeconds());
            staffPerformance.setUpdatedAt(DateUtils.getCurrentSeconds());
            performanceList.add(staffPerformance);
        }
        staffPerformanceDao.batchinsert(performanceList);
    }
    //获取绩效,历史绩效从绩效统计表里查
    public List<StaffPerformance> getStaffPerformance(Map<String, Object> condition){
        List<StaffPerformance> performanceList = new ArrayList<StaffPerformance>();
        Long todayBeginSeconds = DateUtils.getTodayBeginSeconds();
        boolean isSearchCurrentDay = true;//是否只查询当天实时绩效

        Long startDate = Long.parseLong(condition.get("startDate").toString());
        Long endDate = Long.parseLong(condition.get("startDate").toString());

        if(startDate <  todayBeginSeconds){
            isSearchCurrentDay = false;
        }

        //获取当天绩效
        if(isSearchCurrentDay) {
            condition.put("startDate", DateUtils.getTodayBeginSeconds());
            condition.put("endDate", DateUtils.getTodayBeginSeconds());
            List<Map<String, Object>> currentDayperformanceList = this.getPerformance(condition);
            if (currentDayperformanceList != null && currentDayperformanceList.size() > 0) {
                for (Map<String, Object> map1 : currentDayperformanceList) {
                    StaffPerformance staffPerformance = BeanMapTransUtils.map2Bean(map1, StaffPerformance.class);
                    staffPerformance.setSubType(Long.parseLong(map1.get("sub_type").toString()));
                    staffPerformance.setBusinessMode(Integer.parseInt(map1.get("business_mode").toString()));
                    staffPerformance.setOperator(Long.parseLong(map1.get("uid").toString()));
                    performanceList.add(staffPerformance);
                }
            }
        }else{
            //获取历史绩效
            List<StaffPerformance> oldPerformanceList = staffPerformanceDao.getStaffPerformanceList(condition);
            if(oldPerformanceList != null && oldPerformanceList.size() > 0){
                performanceList.addAll(oldPerformanceList);
            }
        }

        return performanceList;
    }

    /*
    获取绩效,直接统计
     */
    public List<Map<String, Object>> getPerformance(Map<String, Object> condition) {
        List<Map<String, Object>> taskInfoList = taskInfoDao.getPerformance(condition);
        List<Map<String, Object>> newTaskInfoList = new ArrayList<Map<String, Object>>();
        List<Long> pickTaskIdList = new ArrayList<Long>();
        List<Long> qcTaskIdList = new ArrayList<Long>();
        //获取任务中所有的拣货任务ID和QC任务ID
        for (Map<String,Object> map : taskInfoList){
            Long type = (Long) map.get("type");
            if(type == TaskConstant.TYPE_QC || type == TaskConstant.TYPE_PICK) {
                //拣货跟QC统计一个任务中商品的个数
                String taskInfos = map.get("taskIds").toString();
                logger.info("[getPerformance]" + taskInfos);
                String taskInfoArr [] = taskInfos.split(",");
                for (String taskInfoIds : taskInfoArr){
                    Long taskId = Long.parseLong(taskInfoIds);
                    if(type == TaskConstant.TYPE_PICK) {
                        pickTaskIdList.add(taskId);
                    }else{
                        qcTaskIdList.add(taskId);
                    }
                }
            }
            map.put("skuCount",1);
            newTaskInfoList.add(map);
        }
        List<WaveDetail> pickWaveDetailList = new ArrayList<WaveDetail>();
        if(pickTaskIdList != null && pickTaskIdList.size() > 0){
            Map<String,Object> waveMap = new HashMap<String, Object>();
            waveMap.put("pickTaskIds",pickTaskIdList);
            pickWaveDetailList = waveService.getWaveDetails(waveMap);
        }
        List<WaveDetail> qcWaveDetailList = new ArrayList<WaveDetail>();
        if(qcTaskIdList != null && qcTaskIdList.size() >0){
            Map<String,Object> waveMap = new HashMap<String, Object>();
            waveMap.put("qcTaskIds",qcTaskIdList);
            qcWaveDetailList = waveService.getWaveDetails(waveMap);
        }
        //改为list
        //Map<Long,Set<Long>> itemSetByTaskId = new HashMap<Long, Set<Long>>();
        Map<Long,List<Long>> itemListByTaskId = new HashMap<Long, List<Long>>();
        Map<Long,BigDecimal> packTotalByTaskId = new HashMap<Long, BigDecimal>();
        Map<Long,BaseinfoItem> baseinfoItems = new HashMap<Long, BaseinfoItem>();
        //拣货箱数 取wave_detail 中的AllocUnitQty
        //统计每个拣货任务中的商品数
        BigDecimal sumPack = BigDecimal.ZERO;
        for (WaveDetail waveDetail : pickWaveDetailList) {
            Long taskId = waveDetail.getPickTaskId();
            if(itemListByTaskId.get(taskId) == null){
                itemListByTaskId.put(taskId,new ArrayList<Long>());
            }
            if(packTotalByTaskId.get(taskId) == null){
                packTotalByTaskId.put(taskId,BigDecimal.ZERO);
            }
            //每个任务中的总箱数
            BigDecimal packUnit = BigDecimal.ONE;
            if(baseinfoItems.get(waveDetail.getItemId()) == null){
                BaseinfoItem item = itemService.getItem(waveDetail.getItemId());
                if(item == null){
                    throw new BizCheckedException("2020022");
                }
                packUnit = item.getPackUnit();
                baseinfoItems.put(waveDetail.getItemId(),item);
            }else{
                packUnit = baseinfoItems.get(waveDetail.getItemId()).getPackUnit();
            }

            sumPack = packTotalByTaskId.get(taskId).add(waveDetail.getPickQty().divide(packUnit,2, RoundingMode.HALF_UP));
            packTotalByTaskId.put(taskId,sumPack);

            //每个任务中的商品数
            List<Long> itemList = itemListByTaskId.get(taskId);
            itemList.add(waveDetail.getItemId());
            itemListByTaskId.put(taskId,itemList);
        }
        //统计每个QC任务中的商品条数
        for (WaveDetail waveDetail : qcWaveDetailList) {
            Long taskId = waveDetail.getQcTaskId();
            if(itemListByTaskId.get(taskId) == null){
                itemListByTaskId.put(taskId,new ArrayList<Long>());
            }
            List<Long> itemList = itemListByTaskId.get(taskId);
            itemList.add(waveDetail.getItemId());
            itemListByTaskId.put(taskId,itemList);
        }
        if(itemListByTaskId.size() > 0){
            //将商品数匹配到每条绩效记录中
            for (Map<String,Object> map : taskInfoList) {
                String taskInfos = map.get("taskIds").toString();
                String taskInfoArr [] = taskInfos.split(",");
                Long type = (Long) map.get("type");
                if(type != TaskConstant.TYPE_QC && type != TaskConstant.TYPE_PICK) {
                    continue;
                }

                List<Long> itemList = new ArrayList<Long>();//统计每条绩效的商品sku数
                BigDecimal packTotal = BigDecimal.ZERO;
                for(String taskIds : taskInfoArr){
                    Long taskId = Long.parseLong(taskIds);
                    if(itemListByTaskId.get(taskId) != null){
                        itemList.addAll(itemListByTaskId.get(taskId));

                    }
                    if(packTotalByTaskId.get(taskId) != null){
                        packTotal = packTotal.add(packTotalByTaskId.get(taskId));
                    }
                }
                if(type == TaskConstant.TYPE_PICK){
                    map.put("taskPackQty",packTotal);
                }
                map.put("skuCount",itemList.size());
            }
        }
        return newTaskInfoList;
    }

    //获取总数
    public Integer getPerformanceCount(Map<String, Object> condition){
        Long startDate = Long.parseLong(condition.get("startDate").toString());
        Long endDate = Long.parseLong(condition.get("startDate").toString());
        Long todayBeginSeconds = DateUtils.getTodayBeginSeconds();
        boolean isSearchCurrentDay = true;//是否只查询当天实时绩效
        if(startDate <  todayBeginSeconds){
            isSearchCurrentDay = false;
        }
        if(isSearchCurrentDay){
            return taskInfoDao.getPerformanceCount(condition);
        }else{
            //查询历史绩效
            return staffPerformanceDao.getStaffPerformanceCount(condition);
        }

    }

    public List<TaskInfo> getPerformaceDetaile(Map<String,Object> mapQuery){
        // 根据收货类型,日期,员工确定明细
        List<TaskInfo> taskInfos = taskInfoDao.getTaskInfoList(mapQuery);
        return taskInfos;
    }

    public Set<Long> getItemSet(){
        return null;
    }


}