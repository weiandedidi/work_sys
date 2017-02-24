package com.lsh.wms.service.wave.split;

import com.lsh.wms.model.wave.WaveDetail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zengwenjun on 16/7/18.
 */
public abstract class SplitModelItem extends SplitModel{
    protected long itemThreshold = 0;
    @Override
    public void split(List<SplitNode> stopNodes) {
        if(this.model.getPickModel()!=3 || itemThreshold<=0){
            this.skipSplit();
            return;
        }
        for(SplitNode node : this.oriNodes){
            Map<Long, BigDecimal> mapQty = new HashMap<Long, BigDecimal>();
            for(WaveDetail detail : node.details){
                BigDecimal num = mapQty.get(detail.getSkuId()) == null ? BigDecimal.ZERO : mapQty.get(detail.getSkuId());
                num = num.add(detail.getAllocQty());
                mapQty.put(detail.getSkuId(), num);
            }
            Map<Long, SplitNode> mapNewStop = new HashMap<Long, SplitNode>();
            SplitNode newNode = new SplitNode();
            for(WaveDetail detail : node.details){
                if(mapQty.get(detail.getSkuId()).compareTo(BigDecimal.valueOf(itemThreshold))>0){
                    if(mapNewStop.get(detail.getSkuId())==null){
                        mapNewStop.put(detail.getSkuId(), new SplitNode());
                    }
                    mapNewStop.get(detail.getSkuId()).details.add(detail);
                } else {
                    newNode.details.add(detail);
                }
            }
            if(this.dstNodes.size()>0) {
                this.dstNodes.add(newNode);
            }
            for(Long key : mapNewStop.keySet()){
                stopNodes.add(mapNewStop.get(key));
            }
        }
    }
}
