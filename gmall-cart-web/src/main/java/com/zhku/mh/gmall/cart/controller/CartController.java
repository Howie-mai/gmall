package com.zhku.mh.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.OmsCartItem;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.SkuService;
import com.zhku.mh.web.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClassName：
 * Time：2020/7/4 17:29
 * Description：
 * Author： mh
 */
@Controller
public class CartController {

    @Reference
    private SkuService skuService;

    @Reference
    private CartService cartService;

    @RequestMapping("addToCart")
    public String addToCart(String skuId, int num,
                            HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //讲商品信息封装成购物车信息
        Date now = new Date();
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(now);
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(now);
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(num));

        //判断用户是否登录
        String memberId = "1";

        if (StringUtils.isBlank(memberId)) {
            //未登录，走cookie
            //Cookie : response.addCookie(cookie) request.getCookies;
            //跨域问题：setDomain，getDomain()

            List<OmsCartItem> omsCartItems = new ArrayList<>();
            //先拿Cookie的值
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)) {
                omsCartItems.add(omsCartItem);
            } else {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                if (ifCartExit(omsCartItems, skuId)) {
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(skuId)) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
//                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                    System.out.println("存在skuId:" + skuId);
                } else {
                    omsCartItems.add(omsCartItem);
                    System.out.println("不存在skuId:" + skuId);
                }
            }

            //3天过期
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        } else {
            //已登录，走DB + cache
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId, skuId);

            if (omsCartItemFromDb == null) {
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test_howie");
                omsCartItem.setQuantity(new BigDecimal(num));
                cartService.addCart(omsCartItem);
            } else {
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                //添加过
                cartService.updateCart(omsCartItemFromDb);
            }

            //刷新同步缓存
            cartService.flushCartCache(memberId);
        }
        return "redirect:success";
    }

    private boolean ifCartExit(List<OmsCartItem> omsCartItems, String skuId) {
        if (CollectionUtils.isEmpty(omsCartItems)) {
            return false;
        }

        for (OmsCartItem item : omsCartItems) {
            if (item.getProductSkuId().equals(skuId)) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping("/cartList")
    public String cartList(String memberId, ModelMap map, HttpServletRequest request) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if (StringUtils.isBlank(memberId)) {
            //未登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNoneBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }else {
               omsCartItems = cartService.getCartList(memberId);
               if(CollectionUtils.isEmpty(omsCartItems)){
                   omsCartItems = cartService.flushCartCache(memberId);
               }
        }

        if(!CollectionUtils.isEmpty(omsCartItems)){
            for (OmsCartItem omsCartItem : omsCartItems) {
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
        }

        map.put("cartList", omsCartItems);
        // 购物车勾选总价格
        BigDecimal totalAmount =  getTotalAmount(omsCartItems);
        map.put("totalAmount",totalAmount);

        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked() != null && omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }

        return totalAmount;
    }

    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = "1";

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);

        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.checkCart(omsCartItem);
        modelMap.put("cartList",omsCartItems);
        // 购物车勾选总价格
        BigDecimal totalAmount =  getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    @RequestMapping("success")
    public String success() {
        return "success";
    }
}
