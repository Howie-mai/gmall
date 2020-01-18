package com.zhku.mh.gmall.manage.mapper;

import com.zhku.mh.gmall.bean.PmsSkuImage;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:08
 * Description：
 * Author： mh
 */
public interface PmsSkuImageMapper extends Mapper<PmsSkuImage> {
    int batchInsertSkuImage(@Param("list") List<PmsSkuImage> list);
}
