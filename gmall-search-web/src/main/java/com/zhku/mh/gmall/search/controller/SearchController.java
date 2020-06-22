package com.zhku.mh.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PmsSearchParam;
import com.zhku.mh.gmall.bean.PmsSearchSkuInfo;
import com.zhku.mh.gmall.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @RequestMapping("/list.html")
    public String list(PmsSearchParam param, Model model){

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(param);
        model.addAttribute("skuLsInfoList",pmsSearchSkuInfos);


        return "list";
    }
}
