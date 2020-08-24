package com.zhku.mh.gmall.payment.service.impl;

import com.zhku.mh.gmall.bean.PaymentInfo;
import com.zhku.mh.gmall.payment.mapper.PaymentMapper;
import com.zhku.mh.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

/**
 * ClassName：
 * Time：2020/8/23 22:35
 * Description：
 * Author： mh
 */
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());

        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }
}
