package com.zhku.mh.gmall.service.config;

import com.zhku.mh.gmall.service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName：
 * Time：2020/1/5 22:43
 * Description：
 * Author： mh
 */
@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port ;
    @Value("${spring.redis.password}")
    private String password ;
    @Value("${spring.redis.database:0}")
    private int database;
    @Bean
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
        redisUtil.initPool(host,port,password,database);
        return redisUtil;
    }
}
