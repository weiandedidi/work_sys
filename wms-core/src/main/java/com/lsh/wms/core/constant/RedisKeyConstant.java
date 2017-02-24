package com.lsh.wms.core.constant;


public class RedisKeyConstant {

    /**
     * 升级-appkey与产品包id的对应关系
     */
    public static final String UP_PKG_KEY = "up:pkg:key:{0}";
    /**
     * 升级-产品包版本信息
     */
    public static final String UP_PKG_VER = "up:pkg:ver:{0}";
    /**
     * 升级-版本详细信息
     */
    public static final String UP_VER_INFO = "up:ver:info:{0}";
    /**
     * 升级-升级规则
     */
    public static final String UP_RULE = "up:rule:{0}";
    /**
     * 升级-版本规则列表
     */
    public static final String UP_VER_RULE_LIST = "up:ver:rule:list:{0}";
    /**
     * 升级-版本规则关联信息
     */
    public static final String UP_VER_RULE_INFO = "up:ver:rule:info:{0}";
    /**
     * 升级-版本规则条件列表
     */
    public static final String UP_VER_RULE_CONLIST = "up:ver:rule:conlist:{0}";
    /**
     * 升级-版本规则条件明细
     */
    public static final String UP_VER_RULE_CONINFO = "up:ver:rule:coninfo:{0}";
    /**
     * 用户登陆缓存token
     */
    public static final String USER_UID_TOKEN = "user:{0}:token";

    public static final String WM_BACK_TOKEN = "wm:token";

    /********媒资相关********/
    public static final String MD_VIDEO_INFO = "md:video:info:{0}";
    public static final String MD_IMAGE_INFO = "md:image:info:{0}";

    /**
     * so单库存占用redis key sort set
     */

    public static final String SO_SKU_INVENTORY_QTY = "so:sku:{0}:qty";
    /**
     * po单库存占用redis key   sort set
     */

    public static final String PO_SKU_INVENTORY_QTY = "po:sku:{0}:qty";


    /**
     * 库内库存占用redis key   sort set
     */

    public static final String WAREHOUSE_SKU_INVENTORY_QTY = "warehouse:{0}:sku:{1}:qty";

    /**
     * 库内可用库存redis key   sort set
     */

    public static final String WAREHOUSE_SKU_AVAILABLE_QTY = "wms:inventory:zone:{0}:warehouse:{1}:owner:{2}";

    /**
     * 库区区位占用resid key  hash
     */
    public static final String LOCATION_LOCATIONID = "location:locationId:{0}";

    /**
     * 库区区位占用resid key  string
     */
    public static final String LOCATION_CODE = "location:code:{0}";

    /**
     * 直流占用redis key   sort set
     */

    public static final String PO_STORE = "po_id:{0}:store_no:{1}";

    /**
     * 直流门店收货剩余数量redis key   sort set
     */

    public static final String STORE_QTY = "poId:{0}:containerId:{1}:barCode:{2}";
    /**
     * 门店收货托盘对应店铺 redis string,值为对应店铺ID
     */

    public static final String CONTAINER_STORE = "container_id:{0}";

    /**
     * 直流tu运单占用redis    key hash
     */
    public static final String TU_CONTAINER = "tu:containerId:{0}";


    /**
     * 日志信息redis key
     */
    public static final String SYS_MSG = "sysLog:logId:{0}";
    /**
     * 盘点key
     */
    public static final String TAKING_KEY = "taking:Id:{0}";

    /**
     * 波次预生成key
     */

    public static final String WAVE_PREVIEW_KEY = "wave:preview:static";


}
