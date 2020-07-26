package com.zhku.mh.gmall.web.config;

import com.zhku.mh.gmall.web.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName：
 * Time：2020/7/12 21:48
 * Description：
 * @author： mh
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        String [] excludePath ={
                "/css/**",
                "/img/**",
                "/js/**",
                "/error"
        };

        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**").excludePathPatterns(excludePath);
    }
}
