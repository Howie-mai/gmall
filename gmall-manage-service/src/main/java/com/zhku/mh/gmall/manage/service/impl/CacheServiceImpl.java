package com.zhku.mh.gmall.manage.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.service.CacheService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 * ClassName：
 * Time：2020/1/5 23:42
 * Description：
 * Author： mh
 */
@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    private RedisUtil redisUtil;


    @Override
    public String ping() {
        Jedis jedis = redisUtil.getJedis();
        return jedis.ping();
    }
}
