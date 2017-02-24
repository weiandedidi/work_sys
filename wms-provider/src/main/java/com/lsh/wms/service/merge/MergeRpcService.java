package com.lsh.wms.service.merge;

import com.alibaba.dubbo.config.annotation.Service;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.utils.DateUtils;
import com.lsh.wms.api.service.merge.IMergeRpcService;
import com.lsh.wms.core.constant.CustomerConstant;
import com.lsh.wms.core.constant.ItemConstant;
import com.lsh.wms.core.constant.TaskConstant;
import com.lsh.wms.core.constant.TuConstant;
import com.lsh.wms.core.service.csi.CsiCustomerService;
import com.lsh.wms.core.service.item.ItemService;
import com.lsh.wms.core.service.location.LocationService;
import com.lsh.wms.core.service.stock.StockQuantService;
import com.lsh.wms.core.service.task.BaseTaskService;
import com.lsh.wms.core.service.tu.TuService;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoLocation;
import com.lsh.wms.model.csi.CsiCustomer;
import com.lsh.wms.model.stock.StockQuant;
import com.lsh.wms.model.task.TaskInfo;
import com.lsh.wms.model.tu.TuDetail;
import com.lsh.wms.model.tu.TuHead;
import com.lsh.wms.model.wave.WaveDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fengkun on 2016/10/20.
 */
@Service(protocol = "dubbo")
public class MergeRpcService implements IMergeRpcService {
    private static Logger logger = LoggerFactory.getLogger(MergeRpcService.class);

    @Autowired
    private CsiCustomerService csiCustomerService;
    @Autowired
    private BaseTaskService baseTaskService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private StockQuantService stockQuantService;
    @Autowired
    private WaveService waveService;
    @Autowired
    private TuService tuService;
    @Autowired
    private ItemService itemService;

    /**
     * 门店维度的未装车板数列表
     *
     * @param mapQuery
     * @return
     * @throws BizCheckedException
     */
    public List<Map<String, Object>> getMergeList(Map<String, Object> mapQuery) throws BizCheckedException {
        mapQuery.put("status", 1); // 生效状态的 TODO: 待改为constant
        mapQuery.put("customerType", CustomerConstant.SUPER_MARKET); // 大店 TODO: 这个地方是字符串,目前数据量小先这样了,理论上应该为数字或者全部取出后遍历
        List<CsiCustomer> customers = csiCustomerService.getCustomerList(mapQuery);
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (CsiCustomer customer : customers) {
            Integer totalMergedContainers = 0; // 未装车总板数
            Integer restMergedContainers = 0; // 未装车余货总板数
            String customerCode = customer.getCustomerCode();
            List<Long> countedContainerIds = new ArrayList<Long>();
            List<WaveDetail> waveDetails = this.getWaveDetailByCustomerCode(customerCode);
            if (waveDetails.size() > 0) {
                for (WaveDetail waveDetail : waveDetails) {
                    Long mergedContainerId = waveDetail.getMergedContainerId();
                    if (mergedContainerId.equals(0L)) {
                        continue;
                    }
                    if (!countedContainerIds.contains(mergedContainerId)) {
                        countedContainerIds.add(mergedContainerId);
                        List<TuDetail> tuDetails = tuService.getTuDeailListByMergedContainerId(mergedContainerId);
                        TaskInfo taskInfo = baseTaskService.getTaskInfoById(waveDetail.getMergeTaskId());
                        Integer containerIncrement = 1;
                        if (taskInfo != null) {
                            containerIncrement = taskInfo.getTaskBoardQty().intValue();
                        }
                        if (tuDetails.size() == 0) {
                            totalMergedContainers += containerIncrement;
                            // 是否是余货
                            if (waveDetail.getQcAt() < DateUtils.getTodayBeginSeconds()) {
                                restMergedContainers += containerIncrement;
                            }
                        } else {
                            Boolean needCount = true;
                            for (TuDetail tuDetail : tuDetails) {
                                String tuId = tuDetail.getTuId();
                                TuHead tuHead = tuService.getHeadByTuId(tuId);
                                if (!tuHead.getStatus().equals(TuConstant.SHIP_OVER)) {
                                    needCount = false;
                                    break;
                                }
                            }
                            if (needCount) {
                                totalMergedContainers += containerIncrement;
                                if (waveDetail.getQcAt() < DateUtils.getTodayBeginSeconds()) {
                                    restMergedContainers += containerIncrement;
                                }
                            }
                        }
                    }
                }
            }
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("customerCode", customer.getCustomerCode());
            result.put("customerName", customer.getCustomerName());
            result.put("address", customer.getAddress());
            result.put("totalMergedContainers", totalMergedContainers);
            result.put("restMergedContainers", restMergedContainers);
            results.add(result);
        }
        return results;
    }

    /**
     * 列表total
     *
     * @param mapQuery
     * @return
     * @throws BizCheckedException
     */
    public Integer countMergeList(Map<String, Object> mapQuery) throws BizCheckedException {
        mapQuery.put("status", 1); // 生效状态的
        mapQuery.put("customerCode", CustomerConstant.SUPER_MARKET); // 大店
        Integer total = csiCustomerService.getCustomerCount(mapQuery);
        return total;
    }

    /**
     * 通过osd获取组盘完成记录的统计数
     *
     * @param waveDetail
     * @return
     * @throws BizCheckedException
     */
    public Map<String, BigDecimal> getQcCountsByWaveDetail(WaveDetail waveDetail) throws BizCheckedException {
        Long qcTaskId = waveDetail.getQcTaskId();
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        TaskInfo qcTaskInfo = baseTaskService.getTaskInfoById(qcTaskId);
        if (qcTaskInfo == null || !qcTaskInfo.getStatus().equals(TaskConstant.Done)) {
            throw new BizCheckedException("2870003");
        }
        result.put("packCount", new BigDecimal(qcTaskInfo.getExt4())); // 总箱数TaskPackQty=boxNum
        result.put("turnoverBoxCount", new BigDecimal(qcTaskInfo.getExt3())); // 总周转箱数Ext3
        return result;
    }

    /**
     * 获取门店的合板详情
     *
     * @param customerCode
     * @return
     * @throws BizCheckedException
     */
    public Map<Long, Map<String, Object>> getMergeDetailByCustomerCode(String customerCode) throws BizCheckedException {
        Map<Long, Map<String, Object>> results = new HashMap<Long, Map<String, Object>>();
        List<Long> countedContainerIds = new ArrayList<Long>();
        List<WaveDetail> waveDetails = this.getWaveDetailByCustomerCode(customerCode);
        for (WaveDetail waveDetail : waveDetails) {
            if (!countedContainerIds.contains(waveDetail.getContainerId())) {
                Long containerId = waveDetail.getMergedContainerId();
                if (waveDetail.getMergedContainerId().equals(0L)) {
                    continue;
                }
                // 未装车的
                List<TuDetail> tuDetails = tuService.getTuDeailListByMergedContainerId(containerId);
                Boolean needCount = true;
                if (tuDetails.size() > 0) {
                    for (TuDetail tuDetail : tuDetails) {
                        String tuId = tuDetail.getTuId();
                        TuHead tuHead = tuService.getHeadByTuId(tuId);
                        if (!tuHead.getStatus().equals(TuConstant.SHIP_OVER)) { //未装车
                            needCount = false;
                            break;
                        }
                    }
                }
                if (!needCount) {
                    continue;
                }
                BaseinfoItem item = itemService.getItem(waveDetail.getItemId());
                if (null == item) {
                    throw new BizCheckedException("2870041");
                }
                boolean isExpensive = (ItemConstant.TYPE_IS_VALUABLE == item.getIsValuable());

                Map<String, BigDecimal> qcCounts = this.getQcCountsByWaveDetail(waveDetail);
                Map<String, Object> result = new HashMap<String, Object>();
                if (results.containsKey(containerId)) {
                    List<String> containersList = (ArrayList<String>) results.get(containerId).get("containersList");
                    containersList.add(waveDetail.getContainerId().toString());
                    result = results.get(containerId);
                    result.put("packCount", new BigDecimal(Double.valueOf(result.get("packCount").toString())).add(qcCounts.get("packCount")));
                    result.put("turnoverBoxCount", new BigDecimal(Double.valueOf(result.get("turnoverBoxCount").toString())).add(qcCounts.get("turnoverBoxCount")));
                    result.put("containerCount", Integer.valueOf(result.get("containerCount").toString()) + 1);
                    result.put("customerCode", customerCode);
                    result.put("containersList", containersList);
                    // 是否是余货
                    if (waveDetail.getQcAt() < DateUtils.getTodayBeginSeconds()) {
                        result.put("isRest", true);
                    }
                    //是否贵品
                    Boolean curIsExpensive = Boolean.valueOf(result.get("isExpensive").toString());
                    if (!curIsExpensive && isExpensive) { //不是贵品状态,转为贵品
                        result.put("isExpensive", isExpensive);
                    }

                } else {
                    TaskInfo taskInfo = baseTaskService.getTaskInfoById(waveDetail.getMergeTaskId());
                    List<String> containersList = new ArrayList<String>();
                    containersList.add(waveDetail.getContainerId().toString());
                    result.put("containerId", containerId);
                    result.put("markContainerId", waveDetail.getContainerId());  //当前作为查找板子码标识的物理托盘码,随机选的
                    result.put("containerCount", 1);
                    result.put("packCount", qcCounts.get("packCount"));
                    result.put("turnoverBoxCount", qcCounts.get("turnoverBoxCount"));
                    result.put("customerCode", customerCode);
                    //加入贵品的判断
                    result.put("isExpensive", isExpensive);

                    result.put("containersList", containersList);
                    if (waveDetail.getQcAt() < DateUtils.getTodayBeginSeconds()) {
                        result.put("isRest", true);
                    } else {
                        result.put("isRest", false);
                    }
                    if (taskInfo.getTaskBoardQty().compareTo(BigDecimal.ONE) > 0) {
                        result.put("isMulBoard", true);
                    } else {
                        result.put("isMulBoard", false);
                    }
                    result.put("mergedTime", waveDetail.getMergeAt());
                }
                results.put(containerId, result);
                countedContainerIds.add(waveDetail.getContainerId());
            }
        }
        return results;
    }

    /**
     * 通过用户编号获取osd
     *
     * @param customerCode
     * @return
     */
    public List<WaveDetail> getWaveDetailByCustomerCode(String customerCode) {
        //获取location的id
        CsiCustomer customer = csiCustomerService.getCustomerByCustomerCode(customerCode); // 门店对应的集货道
        if (null == customer) {
            throw new BizCheckedException("2180023");
        }
        if (null == customer.getCollectRoadId()) {
            throw new BizCheckedException("2180024");
        }
        BaseinfoLocation location = locationService.getLocation(customer.getCollectRoadId());

        List<WaveDetail> waveDetails = new ArrayList<WaveDetail>();

        List<StockQuant> quants = stockQuantService.getQuantsByLocationId(location.getLocationId());
        for (StockQuant quant : quants) {
            Long containerId = quant.getContainerId();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("containerId", containerId);
            params.put("type", TaskConstant.TYPE_QC);
            params.put("status", TaskConstant.Done);
            params.put("businessMode", TaskConstant.MODE_DIRECT);
            List<TaskInfo> taskInfos = baseTaskService.getTaskInfoList(params);
            if (taskInfos.size() > 0) {
                TaskInfo taskInfo = taskInfos.get(0);
                Long qcTaskId = taskInfo.getTaskId();
                waveDetails.addAll(waveService.getDetailsByQCTaskId(qcTaskId));
            }
        }
        return waveDetails;
    }
}
