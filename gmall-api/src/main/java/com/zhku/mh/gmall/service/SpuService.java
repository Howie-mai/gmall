package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.PmsProductImage;
import com.zhku.mh.gmall.bean.PmsProductInfo;
import com.zhku.mh.gmall.bean.PmsProductSaleAttr;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:07
 * Description：
 * Author： mh
 */
public interface SpuService {
    List<PmsProductInfo> querySpuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo productInfo);

    List<PmsProductImage> getSpuImageList(String spuId);

    List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId);

    List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(String productId,String skuId);
}

