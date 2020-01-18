package com.zhku.mh.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.bean.PmsBaseCatalog1;
import com.zhku.mh.gmall.bean.PmsBaseCatalog2;
import com.zhku.mh.gmall.bean.PmsBaseCatalog3;
import com.zhku.mh.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.zhku.mh.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.zhku.mh.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.zhku.mh.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/11 22:16
 * Description：
 * Author： mh
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private PmsBaseCatalog1Mapper catalog1Mapper;

    @Autowired
    private PmsBaseCatalog2Mapper catalog2Mapper;

    @Autowired
    private PmsBaseCatalog3Mapper catalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return catalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String id) {
        Example example = new Example(PmsBaseCatalog2.class);
        example.createCriteria().andEqualTo("catalog1Id",id);

        return catalog2Mapper.selectByExample(example);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        Example example = new Example(PmsBaseCatalog3.class);
        example.createCriteria().andEqualTo("catalog2Id",catalog2Id);

        return catalog3Mapper.selectByExample(example);
    }
}
