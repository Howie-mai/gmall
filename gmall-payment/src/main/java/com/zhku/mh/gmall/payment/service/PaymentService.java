package com.zhku.mh.gmall.payment.service;

import com.zhku.mh.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * ClassName：
 * Time：2020/8/23 22:35
 * Description：
 * Author： mh
 */
public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNo,Integer count);

    Map<String, Object> checkAlipayPayment(String outTradeNo);
}
