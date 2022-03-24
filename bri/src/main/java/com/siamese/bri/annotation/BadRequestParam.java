package com.siamese.bri.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标注该注解表示以这个参数作为判断是否为相同请求的依据
 * 如果参数列表中没有任何一个参数标注了该注解 则当无效请求达到次数上限时
 * 整个方法都会被fallback/defaultMessage取代
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BadRequestParam {
}
