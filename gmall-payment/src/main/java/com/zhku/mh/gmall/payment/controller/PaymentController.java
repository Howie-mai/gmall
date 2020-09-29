package com.zhku.mh.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zhku.mh.gmall.bean.OmsOrder;
import com.zhku.mh.gmall.bean.PaymentInfo;
import com.zhku.mh.gmall.payment.config.AlipayConfig;
import com.zhku.mh.gmall.payment.service.PaymentService;
import com.zhku.mh.gmall.service.OrderService;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/8/20 21:45
 * Description：
 * Author： mh
 */
@Controller
public class PaymentController {

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;

    @RequestMapping("alipay/callback/return")
    @LoginRequired
    public String alipayCallbackReturn(HttpServletRequest request,Model model){

        // 回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        /**
         * call_back_content内容
         * charset=utf-8&
         * out_trade_no=atguigu201903201551591553068319124&
         * method=alipay.trade.page.pay.return&
         * total_amount=0.01&
         * sign=JHMpfB9wv%2FOaNV9Cpjp7%2B6uY83ScfJ4YIG6dsDtrJlbbRJj6Z7%2FMlT3EazeB487wlKGPFim9L2xzIl8MwBpCwOc2qI95pCDwDrXgaO%2F2yA%2FlDp6bDkcRx84Lkm%2F2MNwZ%2FyFSW%2FyyDxWGEI3izHYMm1rf8T6nNDKvfuKTrKiKiIAGSv%2FJX1z7InGW%2BgeWtLlYWdV9fS1aKDEUZwGJaKwQeGf0c2YpZ2u%2FPoBuT32IQTbACx60SO4Jdz4y%2BVjwF5UmvLZD6HP7n5hvcQE833r9FOCU3rOskdAWNWt4wEvaJ%2FAuq%2BFAg6xHTDk1E2iDwkLVjumnYM%2FUcpw6G6Yu60nVtQ%3D%3D&
         * trade_no=2019032022001409701031928056&
         * auth_app_id=2018020102122556&
         * version=1.0&
         * app_id=2018020102122556&
         * sign_type=RSA2&
         * seller_id=2088921750292524&
         * timestamp=2019-03-20+15%3A52%3A40
         */
        String call_back_content = request.getQueryString();

        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            // 验签成功
            // 更新用户的支付状态

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());

            paymentService.updatePayment(paymentInfo);
        }

        return "finish";
    }


    @RequestMapping("alipay/submit")
    @LoginRequired
    public String AlipaySubmit(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, Model model){

        // 获得一个支付宝请求的客户端(它并不是一个链接，而是一个封装好的http的表单请求)
        String form = null;
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        // 回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code", AlipayConfig.productCode);
        map.put("total_amount",totalAmount);
        // 订单标题
        map.put("subject","gmall订单");

        alipayRequest.setBizContent(JSON.toJSONString(map));

        try {
            //调用SDK生成表单
            // form为阿里云支付的页面
            form = alipayClient.pageExecute(alipayRequest).getBody();
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("gmall订单");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);

        // 向消息中间件发送一个检查支付状态(支付服务消费)的延迟消息队列
        // 解决问题：定时任务。
        // 在提交支付后，消息队列发送一个延迟执行的消息任务，
        // 当该任务被支付服务执行时，在消费任务的程序中去查询当前交易的交易状态，
        // 根据交易状态（支付结束）决定解除延迟任务还是继续再设置新的延迟惹任务
        // 开启延迟队列配置： activemq.xml-> schedulerSupport=true
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,5);

        return form;
    }

    @RequestMapping("index")
    @LoginRequired
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, Model model){
        String memberId = String.valueOf(request.getSession().getAttribute("memberId"));
        String nickName = String.valueOf(request.getSession().getAttribute("nickName"));
        if ("null".equals(memberId) || "null".equals(nickName)) {
            return "redirect:http://localhost:8085/index?returnUrl=" + request.getRequestURL();
        }
        model.addAttribute("nickName",nickName);
        model.addAttribute("outTradeNo",outTradeNo);
        model.addAttribute("totalAmount",totalAmount);
        return "index";
    }
}
