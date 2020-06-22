package com.zhku.mh.gmall.service.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName：
 * Time：2020/2/20 15:47
 * Description：
 * Author： mh
 */
@Configuration
public class RedissonUtil {

    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port ;
    @Value("${spring.redis.password}")
    private String password ;
    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+ host + ":" + port).setPassword(password).setDatabase(database);
        RedissonClient client = Redisson.create(config);
        return client;
    }
}
