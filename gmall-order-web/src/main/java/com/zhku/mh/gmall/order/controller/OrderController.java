package com.zhku.mh.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.OmsCartItem;
import com.zhku.mh.gmall.bean.OmsOrder;
import com.zhku.mh.gmall.bean.OmsOrderItem;
import com.zhku.mh.gmall.bean.UmsMemberReceiveAddress;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.OrderService;
import com.zhku.mh.gmall.service.SkuService;
import com.zhku.mh.gmall.service.UserService;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    @Reference
    private OrderService orderService;

    @Reference
    private SkuService skuService;

    @RequestMapping("/submitOrder")
    public String submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode,
                              HttpServletRequest request,Model model) {

        String memberId = String.valueOf(request.getSession().getAttribute("memberId"));
        String nickName = String.valueOf(request.getSession().getAttribute("nickName"));
        if ("null".equals(memberId) || "null".equals(nickName)) {
            return "redirect:http://localhost:8085/index?returnUrl=" + request.getRequestURL();
        }

        String success = orderService.checkTradeCode(memberId,tradeCode);
        if("success".equals(success)){
            Date now = new Date();
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();

            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            // 7天自动收货
            omsOrder.setAutoConfirmDay(7);
            // 确认收货
            omsOrder.setConfirmStatus(0);
            omsOrder.setCreateTime(now);
            // 运费，支付后，在生成物流信息时
            //omsOrder.setFreightAmount();
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickName);

            // 外部订单号，用来和其他系统进行交互，防止重复 。 例如 ：支付宝的支付订单号
            StringBuilder outTradeNo = new StringBuilder("gmall");
            outTradeNo.append(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo.append(sdf.format(now));
            omsOrder.setOrderSn(outTradeNo.toString());

            omsOrder.setPayAmount(totalAmount);
            // 收货时间
            UmsMemberReceiveAddress memberReceiveAddress = userService.getAddressByReceiveAddressId(receiveAddressId);
            omsOrder.setReceiverCity(memberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(memberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(memberReceiveAddress.getName());
            omsOrder.setReceiverPhone(memberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(memberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(memberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(memberReceiveAddress.getRegion());

            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            // 订单类型
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);

            List<OmsCartItem> cartList = cartService.getCartList(memberId);
            for (OmsCartItem cartItem : cartList) {
                if("1".equals(cartItem.getIsChecked())){
                    // 获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    if(!skuService.checkPrice(cartItem.getProductSkuId(),cartItem.getPrice())){
                        return "tradeFail";
                    }
                    // 验库存,远程调用库存系统
                    omsOrderItem.setProductPic(cartItem.getProductPic());
                    omsOrderItem.setProductName(cartItem.getProductName());
                    omsOrderItem.setOrderSn(outTradeNo.toString());
                    omsOrderItem.setProductCategoryId(cartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(cartItem.getPrice());
                    omsOrderItem.setRealAmount(cartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(cartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                    omsOrderItem.setProductId(cartItem.getProductId());
                    // 在仓库中的skuId
                    omsOrderItem.setProductSn("仓库对应的商品编号");

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            orderService.saveOrder(omsOrder);

            // 重定向到支付系统
            model.addAttribute("outTradeNo",outTradeNo.toString());
            model.addAttribute("totalAmount",totalAmount);
            return "redirect:http://localhost:8087";
        }

        return "tradeFail";
    }

    @RequestMapping("/toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, Model model) {

        String memberId = String.valueOf(request.getSession().getAttribute("memberId"));
        String nickname = String.valueOf(request.getSession().getAttribute("nickname"));
        if (StringUtils.isBlank(memberId) || StringUtils.isBlank(nickname)) {
            return "redirect:http://localhost:8085/index";
        }

        // 收货地址
        List<UmsMemberReceiveAddress> userAddressList = userService.getReceiveAddressByMemberId(memberId);

        List<OmsCartItem> omsCartItemList = cartService.getCartList(memberId);

        List<OmsOrderItem> omsOrderItemList = new ArrayList<>();

        for (OmsCartItem cartItem : omsCartItemList) {
            if ("0".equals(cartItem.getIsChecked())) {
                continue;
            }
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setProductPic(cartItem.getProductPic());
            omsOrderItemList.add(orderItem);
        }

        model.addAttribute("nickName", nickname);
        model.addAttribute("userAddressList", userAddressList);
        model.addAttribute("omsOrderItems", omsOrderItemList);
        model.addAttribute("totalAmount", getTotalAmount(omsCartItemList));
        model.addAttribute("tradeCode", orderService.getTradeCode(memberId));
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
