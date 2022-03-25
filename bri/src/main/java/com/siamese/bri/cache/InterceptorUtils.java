package com.siamese.bri.cache;

import java.lang.reflect.Method;

public final class InterceptorUtils {
    private InterceptorUtils() {
        throw new UnsupportedOperationException("can not create an instance of utility class");
    }


    static String getFallbackMappingName(String fallback,Class<?>[] parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder(fallback);
        for(Class<?> clazz:parameterTypes){
            stringBuilder.append(".").append(clazz.getSimpleName());
        }
        return stringBuilder.toString();
    }



    static Method getFallbackMethod(String fallback,Class<?>[] parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        String[] split = fallback.split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i<split.length-1;i++){
            stringBuilder.append(split[i]).append(".");
        }
        String classPath = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        Class<?> clazz = Class.forName(classPath);
        return clazz.getMethod(split[split.length-1],parameterTypes);
    }
}
