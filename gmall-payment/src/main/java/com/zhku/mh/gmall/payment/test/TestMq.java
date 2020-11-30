package com.zhku.mh.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMq {

    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            // 第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            // 开启事务，必须结合commit使用
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("drink");// 队列模式的消息，一次消费
            //Topic t = session.createTopic("");// 话题模式的消息

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("我渴了，谁能帮我打一杯水！");
            // 即便没有消费者，也会等待消费者到来消费，持久化
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            // 提交事务
            session.commit();
            //关闭链接
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
