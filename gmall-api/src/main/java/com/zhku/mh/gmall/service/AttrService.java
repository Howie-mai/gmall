package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.PmsBaseAttrInfo;
import com.zhku.mh.gmall.bean.PmsBaseAttrValue;
import com.zhku.mh.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/17 23:49
 * Description：
 * Author： mh
 */
public interface AttrService {

    List<PmsBaseAttrInfo> queryAttrInfoList(String catalog3Id);

    List<PmsBaseAttrValue> queryAttrValueList(String attrId);

    String saveAttrInfo(PmsBaseAttrInfo attrInfo);

    String delAttrValue(String attrValueId);

    List<PmsBaseSaleAttr> queryBaseSaleAttrList();
}
