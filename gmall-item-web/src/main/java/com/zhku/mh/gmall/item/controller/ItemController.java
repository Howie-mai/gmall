package com.zhku.mh.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.PmsProductSaleAttr;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import com.zhku.mh.gmall.service.SkuService;
import com.zhku.mh.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName：
 * Time：2019/12/29 14:26
 * Description：
 * Author： mh
 */
@Controller
public class ItemController {

    @Reference
    private SkuService skuService;

    @Reference
    private SpuService spuService;

    @RequestMapping("/{skuId}.html")
    public String itemIndex(@PathVariable(name = "skuId") String skuId, Model model) {

        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
//        map.put("skuInfo",skuInfo);
        model.addAttribute("skuInfo", skuInfo);

        //销售属性列表
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrListCheckBySku(skuInfo.getProductId(), skuId);
        model.addAttribute("spuSaleAttrListCheckBySku", spuSaleAttrList);

        //查询当前sku的spu的其他sku集合的hash集
        Map<String, String> map = new HashMap<>();
        List<PmsSkuInfo> skuInfoList = skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());

        skuInfoList.forEach(sku -> {
            StringBuffer k = new StringBuffer();
            String v = sku.getId();

            sku.getSkuSaleAttrValueList().forEach(value -> {
                k.append(value.getSaleAttrValueId()).append("|");
            });
            map.put(k.toString(), v);
        });
        String skuSaleAttrHashJsonStr = JSON.toJSONString(map);
        model.addAttribute("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);

        return "item";
    }

    @RequestMapping("/test/{skuId}")
    @ResponseBody
    public List<PmsProductSaleAttr> test(@PathVariable(name = "skuId") String skuId) {

        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrListCheckBySku(skuInfo.getProductId(), skuId);
        return spuSaleAttrList;
    }

    @RequestMapping("/test2/{skuId}")
    @ResponseBody
    public Object test2(@PathVariable(name = "skuId") String skuId) {
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        Map<String, String> map = new HashMap<>();
        List<PmsSkuInfo> skuInfoList = skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());

        skuInfoList.forEach(sku -> {
            StringBuffer k = new StringBuffer();
            String v = sku.getId();

            sku.getSkuSaleAttrValueList().forEach(value -> {
                k.append(value.getSaleAttrValueId()).append("|");
            });
            map.put(k.toString(), v);
        });
        return map;
    }
}
