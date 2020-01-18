package com.zhku.mh.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PmsBaseCatalog1;
import com.zhku.mh.gmall.bean.PmsBaseCatalog2;
import com.zhku.mh.gmall.bean.PmsBaseCatalog3;
import com.zhku.mh.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/11 22:04
 * Description：
 * Author： mh
 */
@RestController
@CrossOrigin
public class CatalogController {

    @Reference
    private CatalogService catalogService;

    @RequestMapping("/getCatalog1")
    public List<PmsBaseCatalog1> getCategory1(){

        return catalogService.getCatalog1();
    }

    @RequestMapping("/getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam String catalog1Id){

        return catalogService.getCatalog2(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam String catalog2Id){

        return catalogService.getCatalog3(catalog2Id);
    }
}
