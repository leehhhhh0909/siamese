package com.siamese.bri.aspectj;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.cache.FallbackMethodCacheMapping;
import com.siamese.bri.common.app.ApplicationContextHolder;
import com.siamese.bri.common.util.InterceptorUtils;
import com.siamese.bri.common.util.ReflectionUtils;
import com.siamese.bri.exception.InvalidFallbackException;
import com.siamese.bri.exception.NoSuchFallbackClassException;
import com.siamese.bri.handler.BadRequestHandler;
import com.siamese.bri.metadata.InterceptorMetadata;
import com.siamese.bri.predicate.BadRequestPredicateFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


@Aspect
public class BadRequestAspectJ extends ApplicationContextHolder {

    private BadRequestHandler handler;

    private BadRequestPredicateFactory factory;

    private FallbackMethodCacheMapping fallbackMapping;

    @Autowired
    public BadRequestAspectJ(BadRequestHandler handler,
                             BadRequestPredicateFactory factory,
                             FallbackMethodCacheMapping cacheMapping){
        this.handler = handler;
        this.factory = factory;
        this.fallbackMapping = cacheMapping;
    }

    @Around("@annotation(com.siamese.bri.annotation.BadRequestInterceptor)")
    public Object handleBadRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Method sourceMethod = ReflectionUtils.getSourceMethodByJoinPoint(joinPoint);
        BadRequestInterceptor interceptor = ReflectionUtils.getAnnotationFromMethod(sourceMethod, BadRequestInterceptor.class);
        if(handler.needIntercept(joinPoint)){
            return doIntercept(joinPoint,interceptor,sourceMethod);
        }
        return null;
    }



    private Object doIntercept(ProceedingJoinPoint joinPoint, BadRequestInterceptor interceptor,Method sourceMethod) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        if(InterceptorUtils.hasFallback(interceptor)){
            InterceptorMetadata metadata
                    = fallbackMapping.get(interceptor, sourceMethod.getParameterTypes());
            Method fallBackMethod = metadata.getFallBackMethod();
            Class<?> declaringClass = fallBackMethod.getDeclaringClass();
            Object invocationBean;
            try{
                invocationBean = getApplicationContext().getBean(declaringClass);
            }catch (NoSuchBeanDefinitionException e){
                invocationBean = ReflectionUtils.newInstance(declaringClass);
            }
            if(Objects.isNull(invocationBean)){
                throw new NoSuchFallbackClassException(String.format("fail to get an instance of class: %s",declaringClass.getName()));
            }
            handler.handleAfter(joinPoint);
            return fallBackMethod.invoke(invocationBean,joinPoint.getArgs());
        }
        if(String.class.equals(sourceMethod.getReturnType())){
            return interceptor.defaultMessage();
        }
        throw new InvalidFallbackException(String.format("the fallback and defaultMessage of method:[ %s ] is invalid",
                sourceMethod.getName()));
    }
}
