package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.OmsOrder;

/**
 * ClassName：
 * Time：2020/8/2 17:29
 * Description：
 * Author： mh
 */
public interface OrderService {
    String checkTradeCode(String memberId, String tradeCode);

    Object getTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);
}
