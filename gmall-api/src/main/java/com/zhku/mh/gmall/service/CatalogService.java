package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.PmsBaseCatalog1;
import com.zhku.mh.gmall.bean.PmsBaseCatalog2;
import com.zhku.mh.gmall.bean.PmsBaseCatalog3;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/11 22:15
 * Description：
 * Author： mh
 */
public interface CatalogService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
