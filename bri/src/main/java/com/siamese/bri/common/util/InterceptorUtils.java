package com.siamese.bri.common.util;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.common.constants.StringConstants;

import java.lang.reflect.Method;

public final class InterceptorUtils {
    private InterceptorUtils() {
        throw new UnsupportedOperationException("can not create an instance of utility class");
    }


    public static String getFallbackMappingName(String fallback,Class<?>[] parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder(fallback);
        for(Class<?> clazz:parameterTypes){
            stringBuilder.append(StringConstants.DOT).append(clazz.getSimpleName());
        }
        return stringBuilder.toString();
    }



    public static Method getFallbackMethod(String fallback,Class<?>[] parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        String[] split = fallback.split(StringConstants.DOT_TRANS);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i<split.length-1;i++){
            stringBuilder.append(split[i]).append(StringConstants.DOT);
        }
        String classPath = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        Class<?> clazz = Class.forName(classPath);
        return clazz.getMethod(split[split.length-1],parameterTypes);
    }




    public static boolean hasFallback(BadRequestInterceptor interceptor){
        return !interceptor.fallback().isEmpty();
    }
}
