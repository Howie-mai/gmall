package com.zhku.mh.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.OmsCartItem;
import com.zhku.mh.gmall.cart.mapper.OmsCartItemMapper;
import com.zhku.mh.gmall.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.zhku.mh.gmall.service.util.RedisUtil;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/7/5 15:44
 * Description：
 * Author： mh
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        //
        OmsCartItem paramItem = new OmsCartItem();
        paramItem.setMemberId(memberId);
        paramItem.setProductSkuId(skuId);
        return omsCartItemMapper.selectOne(paramItem);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if(StringUtils.isNoneBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);
        }

    }

    @Override
    public void updateCart(OmsCartItem omsCartItem) {

        omsCartItemMapper.updateByPrimaryKeySelective(omsCartItem);
    }

    @Override
    public List<OmsCartItem> flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> cartList = omsCartItemMapper.select(omsCartItem);

        Map<String,String> map = new HashMap<>();
        for (OmsCartItem item : cartList) {
            item.setTotalPrice(item.getPrice().multiply(item.getQuantity()));
            map.put(item.getProductSkuId(), JSON.toJSONString(item));
        }

        //同步到redis
        Jedis jedis = redisUtil.getJedis();
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:" + memberId + ":cart",map);

        jedis.close();
        return cartList;
    }

    @Override
    public List<OmsCartItem> getCartList(String memberId) {

        Jedis jedis = redisUtil.getJedis();
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
        for (String hval : hvals) {
            OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
            omsCartItems.add(omsCartItem);
        }


        jedis.close();
        return omsCartItems;
    }

    @Override
    public List<OmsCartItem> checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);

        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);
        // 缓存同步
        return flushCartCache(omsCartItem.getMemberId());
    }
}
