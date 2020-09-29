package com.zhku.mh.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.OmsOrder;
import com.zhku.mh.gmall.bean.OmsOrderItem;
import com.zhku.mh.gmall.order.mapper.OrderItemMapper;
import com.zhku.mh.gmall.order.mapper.OrderMapper;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.OrderService;
import com.zhku.mh.gmall.service.mq.ActiveMQUtil;
import com.zhku.mh.gmall.service.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ClassName：
 * Time：2020/7/5 15:44
 * Description：
 * Author： mh
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Reference
    private CartService cartService;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
//            String tradeCodeFromCache = jedis.get(tradeKey);
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // KEYS[1]: tradeKey 。 ARGV[1]: tradeCode
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));

            // 成功的eval值为1
            if (eval != null && eval != 0) {
                jedis.del(tradeKey);
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return "fail";
    }

    @Override
    public Object getTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + memberId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeKey, 60 * 30, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        try {
            orderMapper.insertSelective(omsOrder);

            String orderId = omsOrder.getId();
            List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
            if(CollectionUtils.isNotEmpty(omsOrderItems)){
                for (OmsOrderItem orderItem : omsOrderItems) {
                    orderItem.setOrderId(orderId);
                    orderItemMapper.insertSelective(orderItem);
                    // 删除购物车
//                    cartService.delCart(orderItem.getProductSkuId());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder order = new OmsOrder();
        order.setOrderSn(outTradeNo);
        return orderMapper.selectOne(order);
    }

    @Transactional
    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        omsOrder.setStatus(1);

        // 支付成功 -> 更改支付信息状态 -> 更新订单状态 -> 库存
        // 发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try{
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue payhment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);
            //字符串文本
            TextMessage textMessage=new ActiveMQTextMessage();
            // hash结构
            //MapMessage mapMessage = new ActiveMQMapMessage();

            // 查询订单的对象，转化成json字符串，存入ORDER_PAY_QUEUE的消息队列
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = orderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> select = orderItemMapper.select(omsOrderItemParam);
            omsOrderResponse.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));

            orderMapper.updateByExampleSelective(omsOrder,example);
            producer.send(textMessage);
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
}
