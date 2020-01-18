package com.zhku.mh.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PmsProductImage;
import com.zhku.mh.gmall.bean.PmsProductInfo;
import com.zhku.mh.gmall.bean.PmsProductSaleAttr;
import com.zhku.mh.gmall.service.SpuService;
import com.zhku.mh.web.util.PmsUploadUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:11
 * Description：
 * Author： mh
 */
@RestController
@CrossOrigin
public class SpuController {
    @Reference
    private SpuService spuService;

    @RequestMapping("/spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        return spuService.querySpuList(catalog3Id);
    }

    @RequestMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo productInfo) {
        spuService.saveSpuInfo(productInfo);
        return "success";
    }

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        return PmsUploadUtil.upload(multipartFile);
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){
        return spuService.getSpuImageList(spuId);
    }

    @RequestMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        return spuService.getSpuSaleAttrList(spuId);
    }
}
