package com.lsh.wms.integration.service.wave;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.wms.api.service.wave.IWaveRpcService;
import com.lsh.wms.api.service.wave.IWaveService;
import com.lsh.wms.core.constant.WaveConstant;
import com.lsh.wms.core.service.wave.WaveService;
import com.lsh.wms.model.wave.WaveHead;
import com.lsh.wms.model.wave.WaveRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zengwenjun on 16/9/6.
 */
@Service(protocol = "rest")
@Path("wave")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class IntegrationWaveService implements IWaveService {
    @Reference
    private IWaveRpcService iWaveRpcService;

    @Autowired
    private WaveService waveService;

    @POST
    @Path("createAndReleaseWave")
    public String createAndReleaseWave(WaveRequest request) throws BizCheckedException{
        Long waveId = 0L;
        String msg = "创建成功;释放成功";
        int ret = 0;
        try {
            //waveId = iWaveRpcService.createWave(request);
            waveId = iWaveRpcService.decorateCreateWave(request);
        }catch (BizCheckedException e){
            msg = "创建失败;"+e.getMessage();
            ret = -1;
        }
        //查询wave_head
        WaveHead head = waveService.getWave(waveId);
        //如果波次为新建状态的再执行释放波次
        if(ret == 0 && head.getStatus() == WaveConstant.STATUS_NEW) {
            try {
                iWaveRpcService.releaseWave(waveId, 0L);
            } catch (BizCheckedException e) {
                msg = "创建成功,释放失败 " + e.getMessage();
                //创建成功返回成功。
                ret = 0;
            }
        }
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("waveId", waveId);
        rst.put("msg", msg);
        rst.put("ret", ret);
        return JsonUtils.SUCCESS(rst);
    }
}
