package com.siamese.bri.handler;
import org.aspectj.lang.ProceedingJoinPoint;

public interface BadRequestHandler {
    boolean needIntercept(ProceedingJoinPoint point);

    Object handleAfter(ProceedingJoinPoint point);

    Object record(ProceedingJoinPoint point) throws IllegalAccessException;

    Object flush();
}
