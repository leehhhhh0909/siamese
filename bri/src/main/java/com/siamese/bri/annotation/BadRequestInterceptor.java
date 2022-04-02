package com.siamese.bri.annotation;


import com.siamese.bri.exception.BadRequestException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BadRequestInterceptor {

    String fallback() default "";

    int tolerance();

    String defaultMessage() default "";

    Class<? extends Exception>[] targetException() default {BadRequestException.class};

    long expireTime() default 1000*60*60*24L;

}
