package com.zhku.mh.gmall.payment;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDubbo
@MapperScan(basePackages = "com.zhku.mh.gmall.payment.mapper")
@ComponentScan(basePackages = "com.zhku.mh.gmall")
@SpringBootApplication
public class GmallPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPaymentApplication.class, args);
	}

}
