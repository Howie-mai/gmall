package com.zhku.mh.gmall.order.mq;

import com.zhku.mh.gmall.bean.OmsOrder;
import com.zhku.mh.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * ClassName：
 * Time：2020/9/14 21:53
 * Description：
 * Author： mh
 */

@Component
public class OrderProducerListener {

    @Autowired
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE"
            ,containerFactory = "jmsQueueListener")
    public void updateProcessStatus(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("out_trade_no");

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);

        orderService.updateOrder(omsOrder);

    }
}
