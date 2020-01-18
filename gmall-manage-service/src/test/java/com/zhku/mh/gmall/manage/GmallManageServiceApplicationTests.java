package com.zhku.mh.gmall.manage;

import com.zhku.mh.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.zhku.mh.gmall.service.CacheService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private PmsProductSaleAttrMapper saleAttrMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CacheService cacheService;

    @Test
    public void contextLoads() {
//        List<PmsProductSaleAttr> list = saleAttrMapper.getSpuSaleAttrListCheckBySku("11","1");
//
//        System.out.println(list.size());
        System.out.println(cacheService.ping());
    }

}
