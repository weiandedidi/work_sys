package com.lsh.wms.api.service.item;

import com.lsh.base.common.exception.BizCheckedException;
import com.lsh.wms.model.baseinfo.BaseinfoItem;
import com.lsh.wms.model.baseinfo.BaseinfoItemLocation;
import com.lsh.wms.model.baseinfo.BaseinfoItemRequest;

import java.util.Map;

/**
 * Created by zengwenjun on 16/7/8.
 */
public interface IItemRestService {
    public String getItem(long itemId);
    public String getSku(long iSkuId);
    public String getSkuByCode(int iCodeType, String sCode);
    public String getItemsBySkuCode(long iOwnerId, String sSkuCode);
    public String searchItem(Map<String, Object> mapQuery) throws BizCheckedException;
    String countItem(Map<String, Object> mapQuery);

    public String insertItem(BaseinfoItem item) throws BizCheckedException;
    String insertItems(BaseinfoItemRequest request) throws BizCheckedException;
    String updateItem(BaseinfoItem item) throws BizCheckedException;


    String getItemLocation(long iItemId);
    String getItemLocationByLocationID(long iLocationId);
    String insertItemLocation(BaseinfoItemLocation itemLocation) throws BizCheckedException;
    String updateItemLocation(BaseinfoItemLocation itemLocation)  throws BizCheckedException ;
    String updateItemLocationById(BaseinfoItemLocation itemLocation)  throws BizCheckedException ;
    String setStatus(long iItemId,long iStatus);

    String deleteItemLocation(BaseinfoItemLocation itemLocation)  throws BizCheckedException ;

    String updateBarcode() throws BizCheckedException;

    String showCodes(Long itemId);

}
