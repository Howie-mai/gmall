package com.zhku.mh.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PmsBaseAttrInfo;
import com.zhku.mh.gmall.bean.PmsBaseAttrValue;
import com.zhku.mh.gmall.bean.PmsBaseSaleAttr;
import com.zhku.mh.gmall.service.AttrService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/17 23:48
 * Description：
 * Author： mh
 */
@RestController
@CrossOrigin
public class AttrController {

    @Reference
    private AttrService attrService;

    @RequestMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(@RequestParam String catalog3Id) {
        return attrService.queryAttrInfoList(catalog3Id);
    }

    @RequestMapping("/getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam String attrId) {
        return attrService.queryAttrValueList(attrId);
    }

    @RequestMapping("/saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo attrInfo){
        return attrService.saveAttrInfo(attrInfo);
    }

    @RequestMapping("/delAttrValue/{valueId}")
    public String delAttrValue(@PathVariable(name = "valueId") String valueId){
        return attrService.delAttrValue(valueId);
    }

    @RequestMapping("/baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        return attrService.queryBaseSaleAttrList();
    }
}
