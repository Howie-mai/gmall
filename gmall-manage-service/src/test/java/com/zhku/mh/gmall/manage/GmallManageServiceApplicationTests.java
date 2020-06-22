package com.zhku.mh.gmall.manage;

import com.zhku.mh.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.zhku.mh.gmall.service.CacheService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import com.zhku.mh.gmall.service.util.RedissonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import tk.mybatis.spring.annotation.MapperScan;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan(basePackages = "com.zhku.mh.gmall.manage.mapper")
@ComponentScan(basePackages = "com.zhku.mh.gmall")
public class GmallManageServiceApplicationTests {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void contextLoads() {
        //可重入锁
       RLock rLock = redissonClient.getLock("lock");//声明锁

       rLock.lock();//加锁
       try{
           //do something
       }finally {
           rLock.unlock();//解锁
       }
        System.out.println(rLock.getName());
    }

}
