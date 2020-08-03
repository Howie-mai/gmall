package com.zhku.mh.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.OmsCartItem;
import com.zhku.mh.gmall.bean.OmsOrderItem;
import com.zhku.mh.gmall.bean.UmsMemberReceiveAddress;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.UserService;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName：
 * Time：2020/8/2 20:43
 * Description：
 * Author： mh
 */
@Controller
public class OrderController {

    @Reference
    private CartService cartService;

    @Reference
    private UserService userService;

    @RequestMapping("/submitOrder")
    public String submitOrder(String receiveAddressId, BigDecimal totalAmount,HttpServletRequest request){



        return null;
    }

    @RequestMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, Model model){

        String memberId = String.valueOf(request.getSession().getAttribute("memberId"));
        String nickname = String.valueOf(request.getSession().getAttribute("nickname"));
        if(StringUtils.isBlank(memberId) || StringUtils.isBlank(nickname)){
            return "redirect:http://localhost:8085/index";
        }

        // 收货地址
        List<UmsMemberReceiveAddress> userAddressList = userService.getReceiveAddressByMemberId(memberId);

        List<OmsCartItem> omsCartItemList = cartService.getCartList(memberId);

        List<OmsOrderItem> omsOrderItemList  = new ArrayList<>();

        for (OmsCartItem cartItem : omsCartItemList) {
            if("0".equals(cartItem.getIsChecked())){
                continue;
            }
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setProductPic(cartItem.getProductPic());
            omsOrderItemList.add(orderItem);
        }

        model.addAttribute("nickName",nickname);
        model.addAttribute("userAddressList",userAddressList);
        model.addAttribute("omsOrderItems",omsOrderItemList);
        model.addAttribute("totalAmount", getTotalAmount(omsCartItemList));
        model.addAttribute("tradeCode", getTotalAmount(omsCartItemList));
        return "trade";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if ("1".equals(omsCartItem.getIsChecked())) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }
}
