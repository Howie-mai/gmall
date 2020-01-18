package com.zhku.mh.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import com.zhku.mh.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName：
 * Time：2019/12/28 1:34
 * Description：
 * Author： mh
 */
@RestController
@CrossOrigin
public class SkuController {
    @Reference
    private SkuService skuService;

    @RequestMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo skuInfo){
        // 处理默认图片
        String skuDefaultImg = skuInfo.getSkuDefaultImg();
        if(StringUtils.isBlank(skuDefaultImg) && !CollectionUtils.isEmpty(skuInfo.getSkuImageList())){
            skuInfo.setSkuDefaultImg(skuInfo.getSkuImageList().get(0).getImgUrl());
        }

        skuService.saveSkuInfo(skuInfo);
        return "success";
    }
}
