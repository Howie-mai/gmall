package com.zhku.mh.gmall.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName：
 * Time：2020/7/12 22:19
 * Description：
 * @author： mh
 */

//用于方法上

@Target(ElementType.METHOD)
//生效范围
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    boolean loginSuccess() default true;
}
