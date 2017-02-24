package com.lsh.wms.rf.bootstrap;

/**
 * Project Name: lsh-wms
 * Created by fuhao
 * Date: 16/7/28
 * Time: 16/7/28.
 * 北京链商电子商务有限公司
 * Package name:com.lsh.wms.rf.service.
 * desc:类功能描述
 */
public class RfMain {
    public static void main(String[] args) {
        // 第一个参数用来标记程序名称
        String[] argAry = null;
        if (args != null && args.length > 1) {
            argAry = new String[args.length - 1];
            System.arraycopy(args, 1, argAry, 0, args.length - 1);
        }
        // http://127.0.0.1:8889/api/wms/java/v1/sms/sendMsg
        com.alibaba.dubbo.container.Main.main(argAry);

    }
}
