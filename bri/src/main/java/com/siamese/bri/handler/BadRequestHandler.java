package com.siamese.bri.handler;
import com.siamese.bri.common.model.StorageKey;
import org.aspectj.lang.ProceedingJoinPoint;

public interface BadRequestHandler {

    boolean needIntercept(ProceedingJoinPoint point,int tolerance) throws IllegalAccessException;

    Object handleAfter(ProceedingJoinPoint point);

    Object record(ProceedingJoinPoint point,long expireTime) throws IllegalAccessException;

    Object flush();

    StorageKey getStorageKey(ProceedingJoinPoint point) throws IllegalAccessException;
}
