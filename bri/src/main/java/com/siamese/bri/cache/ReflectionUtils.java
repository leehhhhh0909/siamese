package com.siamese.bri.cache;

import com.siamese.bri.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException("can not create an instance of utility class");
    }

    static <T extends Annotation> T getAnnotationFromMethod(Method method, Class<T> clazz){
        ObjectUtils.nonNull("method and the class of An annotation must be not null!",method,clazz);
        return method.getAnnotation(clazz);
    }


    static Class<?>[] getParameters(Method method){
        ObjectUtils.nonNull("method and the class of An annotation must be not null!",method);
        return method.getParameterTypes();
    }
}
