package com.siamese.bri.annotation;


import com.siamese.bri.exception.BadRequestException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BadRequestInterceptor {
    /**
     * 配置处理无效请求的方法(配置 全类名.方法名)
     * 声明callback方法的类必须被spring容器管理/有默认构造方法。
     * 且参数列表和原方法一致
     */
    String fallback() default "";

    /**
     * 当相同参数的请求失败多少次后才被认为是无效请求
     * 该参数没有默认值  要求必须配置
     */
    int tolerance();

    /**
     * 拦截到无效请求时默认返回值 当fallback有配置时此属性不生效
     * 只有当返回值类型为String时此配置才能有效返回  否则会抛异常
     */
    String defaultMessage() default "";

    /**
     * 当请求抛出哪些异常时记录该次请求无效
     * 要求目标方法必须抛出异常 且不在内部捕获处理该异常
     */
    Class<? extends Exception>[] targetException() default {BadRequestException.class};

    /**
     * 配置记录的过期时间
     */
    long expireTime() default 1000*60*60*24L;

}
