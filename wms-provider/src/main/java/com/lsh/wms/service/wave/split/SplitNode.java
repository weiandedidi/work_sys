package com.lsh.wms.service.wave.split;

import com.lsh.wms.model.wave.WaveDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengwenjun on 16/7/15.
 */
public class SplitNode {
    public List<WaveDetail> details = new ArrayList<WaveDetail>();
    public int iStopType = 0;
    public long iPickType = 0;
}
