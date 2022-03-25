package com.siamese.bri.aspectj;

import com.siamese.bri.handler.BadRequestHandler;
import com.siamese.bri.predicate.BadRequestPredicateFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


@Aspect
public class BadRequestAspectJ implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private BadRequestHandler handler;

    private BadRequestPredicateFactory factory;

    @Autowired
    public BadRequestAspectJ(BadRequestHandler handler,
                                   BadRequestPredicateFactory factory){
        this.handler = handler;
        this.factory = factory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Around("@annotation(com.siamese.bri.exception.BadRequestException)")
    public Object handleBadRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        return null;
    }
}
