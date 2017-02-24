package com.lsh.wms.worker.bootstrap;


import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by huangdong on 16/6/23.
 */
public class Bootstrap {

    public static void main(String[] args) {
        // 第一个参数用来标记程序名称
        String[] argAry = null;
        if (args != null && args.length > 1) {
            argAry = new String[args.length - 1];
            System.arraycopy(args, 1, argAry, 0, args.length - 1);
        }
        // http://127.0.0.1:9003/api/wms/java/v1/sms/sendMsg
        com.alibaba.dubbo.container.Main.main(argAry);
    }
}
