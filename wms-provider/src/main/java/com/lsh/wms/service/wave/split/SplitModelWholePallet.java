package com.lsh.wms.service.wave.split;

import com.lsh.base.common.utils.ObjUtils;
import com.lsh.wms.core.constant.LocationConstant;
import com.lsh.wms.core.constant.PickConstant;
import com.lsh.wms.core.service.utils.PackUtil;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.wave.WaveDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengwenjun on 16/11/11.
 */
public class SplitModelWholePallet extends SplitModel {
    private static Logger logger = LoggerFactory.getLogger(SplitModelWholePallet.class);

    @Override
    public void split(List<SplitNode> stopNodes) {
        if (model.getStrategyWholePallet() != 1) {
            this.skipSplit();
            return;
        }
        for (SplitNode node : this.oriNodes) {
            List<WaveDetail> details = new ArrayList<WaveDetail>();
            for (WaveDetail detail : node.details) {
                //如果单品的数量大于整托的数量,则直接拆出来搞起
                BaseinfoItem item = mapItems.get(detail.getItemId());
                final BigDecimal palletQty = item.getPackUnit().multiply(BigDecimal.valueOf(item.getPileX() * item.getPileY() * item.getPileZ()));
                logger.info(String.format("item %d[%s] palletQty[%s] allocQty[%s] unitname[%s]",
                        item.getItemId(),
                        item.getSkuName(),
                        palletQty.toString(),
                        detail.getAllocQty().toString(),
                        detail.getAllocUnitName()));
                if (palletQty.compareTo(BigDecimal.ZERO) > 0
                        //&& detail.getAllocUnitName() != "EA"
                        && (detail.getPickArea().getRegionType() == LocationConstant.SHELFS
                            || detail.getPickArea().getRegionType() == LocationConstant.FLOOR)
                        && detail.getAllocQty().compareTo(palletQty) >= 0) {
                    int num = detail.getAllocQty().divide(palletQty, 0, BigDecimal.ROUND_FLOOR).intValue();
                    logger.info(String.format("GODDAMMIT PALLET item %d[%s] palletQty[%s] qty[%s] splitPalletNum[%d]",
                            item.getItemId(),
                            item.getSkuName(),
                            palletQty.toString(),
                            detail.getAllocQty().toString(),
                            num));
                    for (int i = 0; i < num; ++i) {
                        SplitNode newNode = new SplitNode();
                        WaveDetail newDetail = new WaveDetail();
                        ObjUtils.bean2bean(detail, newDetail);
                        newDetail.setAllocQty(palletQty);
                        newDetail.setAllocUnitQty(PackUtil.EAQty2UomQty(palletQty, detail.getAllocUnitName()));
                        newNode.details.add(newDetail);
                        newNode.iPickType = PickConstant.SHELF_PALLET_TASK_TYPE;//呵呵,找日的代码.
                        stopNodes.add(newNode);
                    }
                    if (detail.getAllocQty().compareTo(palletQty.multiply(BigDecimal.valueOf((long) num))) > 0) {
                        BigDecimal leftQty = detail.getAllocQty().subtract(palletQty.multiply(BigDecimal.valueOf((long) num)));
                        detail.setAllocQty(leftQty);
                        detail.setAllocUnitQty(PackUtil.EAQty2UomQty(leftQty, detail.getAllocUnitName()));
                        details.add(detail);
                    }
                } else {
                    details.add(detail);
                }
            }
            if (details.size() > 0) {
                node.details = details;
                this.dstNodes.add(node);
            }
        }
    }
}

