package com.siamese.bri.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标注该注解表示以这个参数作为判断是否为相同请求的依据
 * 只有在参数列表中标注了 @PrimaryBadRequestParam 的参数内部的此注解才生效
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BadRequestProperty {
}
