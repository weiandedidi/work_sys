package com.lsh.wms.rf.service.filter;

import com.alibaba.dubbo.rpc.RpcContext;
import com.lsh.base.common.config.PropertyUtils;
import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.base.common.json.JsonUtils;
import com.lsh.base.common.utils.StrUtils;
import com.lsh.wms.api.model.base.ResUtils;
import com.lsh.wms.api.model.base.ResponseConstant;
import com.lsh.wms.api.service.request.RequestUtils;
import com.lsh.wms.core.constant.RedisKeyConstant;
import com.lsh.wms.core.dao.redis.RedisStringDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixin-mac on 16/8/3.
 */
@Aspect
@Component
public class FilterInterceptor{
    private static Logger logger = LoggerFactory.getLogger(FilterInterceptor.class);

    @Autowired
    private RedisStringDao redisStringDao;

    /**
     * 切入点表达式.
     */
    @Pointcut("execution(* com.lsh.wms.rf.service.*.*Service.*(..))")
    public void declareJointPointExpression(){}


    @Around("declareJointPointExpression()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("开始时间:"+sdf.format(new Date()));
        if("0".equals(PropertyUtils.getString("variable"))) {
            try {
               return pjp.proceed();
            } catch (Throwable ex) {
                throw ex;
            }
        }else{
            logger.info("method name ~~~~~~~~~~"+pjp.getSignature().getName() +"~~~~~~~~~~~~~~~~");
            if("userLogin".equals(pjp.getSignature().getName()) || "userLogout".equals(pjp.getSignature().getName())){
                try {
                  return pjp.proceed();
                } catch (Throwable ex) {
                    throw ex;
                }
            }else{
                logger.info("~~~~~~~~~~~~~~~~~~~~~~~~");
                HttpServletRequest request = (HttpServletRequest) RpcContext.getContext().getRequest();
                Map<String, String> map = new HashMap<String, String>();
                String utoken = request.getHeader("utoken");
                String uid =request.getHeader("uid");
                String key = StrUtils.formatString(RedisKeyConstant.USER_UID_TOKEN,uid);
                //redis中获取key
                String value = redisStringDao.get(key);
                //取出流水号
                String serialNumber = request.getHeader("serialNumber");
                if (value == null || !value.equals(utoken)) {
                    //return ResUtils.getResponse(ResponseConstant.RES_CODE_2660003, ResponseConstant.RES_MSG_ERROR, null);
                    return JsonUtils.PARAMETER_ERROR("2660003","Token校验失败,请重新登录");
                    //throw new BizCheckedException("2660003");
                }else{
                    //如果验证成功，说明此用户进行了一次有效操作，延长token的过期时间
                    redisStringDao.expire(key, PropertyUtils.getLong("tokenExpire"));
                    try {
                        if(serialNumber == null){
                            logger.info("结束时间:"+sdf.format(new Date()));
                            return pjp.proceed();
                        }
                        if(redisStringDao.get(serialNumber) == null){
                            //将结果放到redis中。
                            logger.info("1111111111111111111111111111~~~~~~serialNumber" + serialNumber);
                            String result = (String) pjp.proceed();
                            redisStringDao.set(serialNumber,result);
                            logger.info("结束时间:"+sdf.format(new Date()));
                            logger.info("2222222222222222222222222222~~~~~~~~~~result" + result);
                            return result;
                            //return pjp.proceed();
                        }
                        else{
                            logger.info("结束时间:"+sdf.format(new Date()));
                            return redisStringDao.get(serialNumber);
                        }

//                        return pjp.proceed();
                    } catch (Throwable ex) {
                        throw ex;

                    }
                }

            }

        }

    }

}
