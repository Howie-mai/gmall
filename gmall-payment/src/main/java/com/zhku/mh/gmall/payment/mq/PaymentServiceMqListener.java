package com.zhku.mh.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zhku.mh.gmall.bean.PaymentInfo;
import com.zhku.mh.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/9/26 22:15
 * Description：
 * Author： mh
 */
@Component
public class PaymentServiceMqListener {

    @Reference
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");

        // 调用支付宝请求的接口
        Map<String, Object> map = paymentService.checkAlipayPayment(outTradeNo);

        if (!map.isEmpty()) {
            // 交易状态 TRADE_CLOSE TRADE_SUCCESS TRADE_FINISHED
            String tradeStatus = (String) map.get("trade_status");
            map.get("trade_status");

            // 根据状态结果判断是否进行下一次的延迟任务还是支付成功更新数据和后续任务
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 支付成功 更新支付发送支付队列
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(outTradeNo);
                paymentService.updatePayment(paymentInfo);
            } else {
                // 继续发送延迟检查任务，计算延迟时间等
                count--;
                paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
            }
        }

        if(count>0){
            // 继续发送延迟检查任务，计算延迟时间等
            System.out.println("没有支付成功，检查剩余次数为"+count+",继续发送延迟检查任务");
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
        }else{
            System.out.println("检查剩余次数用尽，结束检查");
        }

    }
}
