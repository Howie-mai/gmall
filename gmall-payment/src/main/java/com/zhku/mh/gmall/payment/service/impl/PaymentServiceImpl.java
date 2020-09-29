package com.zhku.mh.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.zhku.mh.gmall.bean.PaymentInfo;
import com.zhku.mh.gmall.payment.mapper.PaymentMapper;
import com.zhku.mh.gmall.payment.service.PaymentService;
import com.zhku.mh.gmall.service.mq.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/8/23 22:35
 * Description：
 * Author： mh
 */
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Transactional
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        if(session == null){
            return;
        }

        try {
            paymentMapper.updateByExampleSelective(paymentInfo,example);
            // 调用mq发送支付成功的消息
            Queue queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            // 字符串文本
//            TextMessage textMessage = new ActiveMQTextMessage();
            // hash结构
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            producer.send(mapMessage);
            session.commit();
        }catch (Exception e){
            // 消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }

    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo,Integer count) {
        Connection connection = null;
        Session session = null;
        try{
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue payhment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);
            //字符串文本
//            TextMessage textMessage=new ActiveMQTextMessage();
            // hash结构
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",outTradeNo);
            mapMessage.setInt("count",count);
            // 设置延迟
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000 * 30);
            producer.send(mapMessage);
            session.commit();
        }catch (Exception ex){
            // 消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String outTradeNo) {
        Map<String, Object> resultMap = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",outTradeNo);
        request.setBizContent(jsonObject.toJSONString());
        AlipayTradeQueryResponse reponse = null;
        try {
            reponse = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(reponse != null && reponse.isSuccess()){
            System.out.println("成功");
            resultMap.put("out_trade_no",reponse.getOutTradeNo());
            resultMap.put("trade_status",reponse.getTradeStatus());
            resultMap.put("trade_no",reponse.getTradeNo());
        }else {
            System.out.println("失败");
        }

        return resultMap;
    }
}
