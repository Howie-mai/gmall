package com.zhku.mh.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.bean.PmsBaseAttrInfo;
import com.zhku.mh.gmall.bean.PmsBaseAttrValue;
import com.zhku.mh.gmall.bean.PmsBaseSaleAttr;
import com.zhku.mh.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.zhku.mh.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.zhku.mh.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.zhku.mh.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/17 23:51
 * Description：
 * Author： mh
 */
@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    private PmsBaseAttrInfoMapper attrInfoMapper;

    @Autowired
    private PmsBaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private PmsBaseSaleAttrMapper saleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> queryAttrInfoList(String catalog3Id) {
//        Example example = new Example(PmsBaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        PmsBaseAttrInfo attrInfo = new PmsBaseAttrInfo();
        attrInfo.setCatalog3Id(catalog3Id);

        List<PmsBaseAttrInfo> attrInfoList = attrInfoMapper.select(attrInfo);

        for (PmsBaseAttrInfo pmsBaseAttrInfo : attrInfoList) {

            PmsBaseAttrValue baseAttrValue = new PmsBaseAttrValue();
            baseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            List<PmsBaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
            pmsBaseAttrInfo.setAttrValueList(attrValueList);
        }
        return attrInfoList;
    }

    @Override
    public List<PmsBaseAttrValue> queryAttrValueList(String attrId) {
        PmsBaseAttrValue attrValue = new PmsBaseAttrValue();
        attrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(attrValue);
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo attrInfo) {
        if(StringUtils.isBlank(attrInfo.getId())){
            /**
             * id为空，为添加
             */
            attrInfoMapper.insert(attrInfo);
            String attrId = attrInfo.getId();
            if(!CollectionUtils.isEmpty(attrInfo.getAttrValueList())){
                attrInfo.getAttrValueList().parallelStream().forEach(item -> {
                    item.setAttrId(attrId);
                    try {
                        baseAttrValueMapper.insertSelective(item);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
            return "success";
        }else {
            /**
             * 编辑
             */
            attrInfoMapper.updateByPrimaryKey(attrInfo);

            /**
             * 先把之前的删除
             */
//            PmsBaseAttrValue attrValue = new PmsBaseAttrValue();
//            attrValue.setAttrId(attrInfo.getId());
//            attrValueMapper.delete(attrValue);

            /**
             * 再插入
             */
            if(!CollectionUtils.isEmpty(attrInfo.getAttrValueList())){
                attrInfo.getAttrValueList().parallelStream().forEach(item -> {
                    if(StringUtils.isBlank(item.getId())){
                        //add
                        item.setAttrId(attrInfo.getId());
                        try {
                            baseAttrValueMapper.insertSelective(item);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            baseAttrValueMapper.updateByPrimaryKey(item);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        return "success";
    }

    @Override
    public String delAttrValue(String attrValueId) {
        PmsBaseAttrValue attrValue = new PmsBaseAttrValue();
        attrValue.setId(attrValueId);
        baseAttrValueMapper.delete(attrValue);
        return "success";
    }

    @Override
    public List<PmsBaseSaleAttr> queryBaseSaleAttrList() {
        return saleAttrMapper.selectAll();
    }
}
