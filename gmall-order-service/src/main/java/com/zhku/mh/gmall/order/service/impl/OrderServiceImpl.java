package com.zhku.mh.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.bean.OmsOrder;
import com.zhku.mh.gmall.bean.OmsOrderItem;
import com.zhku.mh.gmall.order.mapper.OrderItemMapper;
import com.zhku.mh.gmall.order.mapper.OrderMapper;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.OrderService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

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
}
