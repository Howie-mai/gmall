package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.PmsSearchParam;
import com.zhku.mh.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * ClassName：
 * Time：2020/5/25 15:56
 * Description：
 * Author： mh
 */
public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam param);
}
