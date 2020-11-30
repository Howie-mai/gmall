package com.zhku.mh.gmall.seckill;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan("com.zhku.mh.gmall")
public class GmallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSeckillApplication.class, args);
    }

}
