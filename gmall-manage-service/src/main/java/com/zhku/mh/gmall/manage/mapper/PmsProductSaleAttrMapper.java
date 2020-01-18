package com.zhku.mh.gmall.manage.mapper;

import com.zhku.mh.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:08
 * Description：
 * Author： mh
 */
public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId") String skuId);
}
