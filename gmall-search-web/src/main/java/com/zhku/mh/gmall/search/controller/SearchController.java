package com.zhku.mh.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.*;
import com.zhku.mh.gmall.service.AttrService;
import com.zhku.mh.gmall.service.SearchService;
import com.zhku.mh.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName：
 * Time：2020/3/1 15:40
 * Description：
 * Author： mh
 */
@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @RequestMapping("/list.html")
    public String list(PmsSearchParam param, Model model){

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(param);
        model.addAttribute("skuLsInfoList",pmsSearchSkuInfos);

        //检索结果的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        for(PmsSearchSkuInfo skuInfo:pmsSearchSkuInfos){
            List<PmsSkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
            for(PmsSkuAttrValue skuAttrValue:skuAttrValueList){
                String valueId = skuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId将属性列表查询
        List<PmsBaseAttrInfo> attrInfoList = attrService.getAttrValueListByValueIds(valueIdSet);

        //对平台属性集合进一步处理，去掉当前条件中ValueId所在的属性组
        String[] delValueIds = param.getValueId();
//        for(PmsBaseAttrInfo attrInfo:attrInfoList){
//            List<PmsBaseAttrValue> attrValueList = attrInfo.getAttrValueList();
//            for(PmsBaseAttrValue attrValue:attrValueList){
//                String valueId = attrValue.getId();
//                for (String delValueId : delValueIds) {
//                    if(valueId.equals(delValueId)){
//                        //删除当前ValueId所在的属性组
//                    }
//                }
//            }
//        }
        //保存筛选条件面包屑
        if(delValueIds != null){
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {
                //删除使用迭代器
                Iterator<PmsBaseAttrInfo> iterator = attrInfoList.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                // 生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(param, delValueId));
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            // 查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            model.addAttribute("attrValueSelectedList",pmsSearchCrumbs);
        }

        model.addAttribute("attrList",attrInfoList);

        String urlParam = getUrlParam(param,"");
        model.addAttribute("urlParam",urlParam);

        if(StringUtils.isNoneBlank(param.getKeyword())){
            model.addAttribute("keyword",param.getKeyword());
        }
        return "list";
    }

//    与getUrlParam合并
//    private String getUrlParamForCrumb(PmsSearchParam param, String delValueId) {
//        String keyword = param.getKeyword();
//        String catalog3Id = param.getCatalog3Id();
//        String[] skuAttrValueList = param.getValueId();
//
//        StringBuffer urlParam = new StringBuffer();
//
//        if(StringUtils.isNoneBlank(keyword)){
//            if(StringUtils.isNoneBlank(urlParam.toString())){
//                urlParam.append("&");
//            }
//            urlParam.append("keyword=").append(keyword);
//        }
//
//        if(StringUtils.isNoneBlank(catalog3Id)){
//            if(StringUtils.isNoneBlank(urlParam.toString())){
//                urlParam.append("&");
//            }
//            urlParam.append("catalog3Id=").append(catalog3Id);
//        }
//
//        if (skuAttrValueList != null) {
//            for (String valueId : skuAttrValueList) {
//                if (!valueId.equals(delValueId)) {
//                    urlParam.append("&valueId=").append(valueId);
//                }
//            }
//        }
//
//        return urlParam.toString();
//    }

    private String getUrlParam(PmsSearchParam param,String delValueId) {
        String keyword = param.getKeyword();
        String catalog3Id = param.getCatalog3Id();
        String[] skuAttrValues = param.getValueId();

        StringBuffer urlParam = new StringBuffer();
        if(StringUtils.isNoneBlank(keyword)){
            if(StringUtils.isNoneBlank(urlParam.toString())){
                urlParam.append("&");
            }
            urlParam.append("keyword=").append(keyword);
        }

        if(StringUtils.isNoneBlank(catalog3Id)){
            if(StringUtils.isNoneBlank(urlParam.toString())){
                urlParam.append("&");
            }
            urlParam.append("catalog3Id=").append(catalog3Id);
        }

        if(skuAttrValues != null){
            for(String valueId:skuAttrValues){
                if(StringUtils.isNoneBlank(delValueId) && valueId.equals(delValueId)){
                    continue;
                }
                urlParam.append("&valueId=").append(valueId);
            }
        }
        return urlParam.toString();
    }
}
