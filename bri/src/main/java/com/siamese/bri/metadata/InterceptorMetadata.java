package com.siamese.bri.metadata;

import com.siamese.bri.annotation.BadRequestInterceptor;
import org.springframework.util.StringUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class InterceptorMetadata{

    private Method fallBackMethod;

    private int tolerance;

    private String defaultMessage;

    private Class<? extends Exception>[] interceptFor;

    private Class<?>[] parameterTypes;

    private long expireTime;

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Method getFallBackMethod() {
        return fallBackMethod;
    }

    public void setFallBackMethod(Method fallBackMethod) {
        this.fallBackMethod = fallBackMethod;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public Class<? extends Exception>[] getInterceptFor() {
        return interceptFor;
    }

    public void setInterceptFor(Class<? extends Exception>[] interceptFor) {
        this.interceptFor = interceptFor;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    private InterceptorMetadata() {
    }

    private InterceptorMetadata(int tolerance, String defaultMessage,
                                Class<? extends Exception>[] interceptFor,
                                long expireTime) {
        this.tolerance = tolerance;
        this.defaultMessage = defaultMessage;
        this.interceptFor = interceptFor;
        this.expireTime = expireTime;
    }

    private InterceptorMetadata(Method fallBackMethod, Class<?>[] parameterTypes,
                                int tolerance, Class<? extends Exception>[] interceptFor,
                                long expireTime) {
        this.fallBackMethod = fallBackMethod;
        this.tolerance = tolerance;
        this.interceptFor = interceptFor;
        this.expireTime = expireTime;
        this.parameterTypes = parameterTypes;
    }

    public static InterceptorMetadata analyze(Method fallbackMethod,Class<?>[] parameterTypes,BadRequestInterceptor interceptor) {
        if(StringUtils.hasText(interceptor.fallback()) && Objects.nonNull(fallbackMethod)){
            return  new InterceptorMetadata(fallbackMethod, parameterTypes,interceptor.tolerance(),interceptor.targetException(),
                    interceptor.expireTime());
        }
        return new InterceptorMetadata(interceptor.tolerance(),interceptor.defaultMessage(),interceptor.targetException(),
                interceptor.expireTime());
    }

    public BadRequestInterceptor transfer(){
        return new BadRequestInterceptor(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return BadRequestInterceptor.class;
            }

            @Override
            public String fallback() {
                return Objects.isNull(fallBackMethod) ? null:fallBackMethod.getDeclaringClass().getName()+
                        "."+fallBackMethod.getName();
            }

            @Override
            public int tolerance() {
                return tolerance;
            }

            @Override
            public String defaultMessage() {
                return defaultMessage;
            }

            @Override
            public Class<? extends Exception>[] targetException() {
                return interceptFor;
            }

            @Override
            public long expireTime() {
                return expireTime;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterceptorMetadata metadata = (InterceptorMetadata) o;
        if(tolerance == metadata.tolerance &&
                expireTime == metadata.expireTime &&
                Arrays.equals(interceptFor, metadata.interceptFor) &&
                Arrays.equals(parameterTypes, metadata.parameterTypes)){
            if(Objects.nonNull(fallBackMethod) && Objects.nonNull(metadata.getFallBackMethod())){
                return fallBackMethod.equals(metadata.getFallBackMethod());
            }
            if(Objects.isNull(fallBackMethod) && Objects.isNull(metadata.getFallBackMethod())){
                return Objects.equals(defaultMessage,metadata.getDefaultMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.isNull(fallBackMethod) ? Objects.hash(tolerance, defaultMessage, expireTime) :
                Objects.hash(fallBackMethod, tolerance, expireTime);
        result = 31 * result + Arrays.hashCode(interceptFor);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }
}
