package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.PmsSkuInfo;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:07
 * Description：
 * Author： mh
 */
public interface SkuService {
    void saveSkuInfo(PmsSkuInfo skuInfo);

    PmsSkuInfo getSkuById(String skuId);

    PmsSkuInfo getSkuByIdFromDB(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
}
