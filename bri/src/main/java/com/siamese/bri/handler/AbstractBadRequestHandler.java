package com.siamese.bri.handler;

import com.siamese.bri.common.model.StorageKey;
import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import java.util.Objects;


public abstract class AbstractBadRequestHandler implements BadRequestHandler{

    private BadRequestStorageKeyGenerator generator;

    private ThreadLocal<StorageKey> localStorageKey = new ThreadLocal<>();

    AbstractBadRequestHandler(BadRequestStorageKeyGenerator generator){
        this.generator = generator;
    }

    @Override
    public StorageKey getStorageKey(ProceedingJoinPoint point) throws IllegalAccessException {
        StorageKey storageKey = localStorageKey.get();
        if(Objects.nonNull(storageKey)) {
            return storageKey;
        }
        StorageKey key = generator.getStorageKey(((MethodSignature) point.getSignature()).getMethod(), point.getArgs());
        localStorageKey.set(key);
        return key;
    }

    @Override
    public Object handleAfter(ProceedingJoinPoint point, long expireTime) throws IllegalAccessException {
        this.resetExpireTime(point, expireTime);
        this.localStorageKey.remove();
        return postHandle(point);
    }


    @Override
    public Object resetExpireTime(ProceedingJoinPoint point, long expireTime) throws IllegalAccessException {
        return record(point, expireTime);
    }

    @Override
    public boolean needIntercept(ProceedingJoinPoint point,int tolerance) throws IllegalAccessException {
        StorageKey storageKey = getStorageKey(point);
        int count = getCurrentInterceptionCount(storageKey);
        return count >= tolerance;
    }

    @Override
    public Object record(ProceedingJoinPoint point,long expireTime) throws IllegalAccessException {
        StorageKey storageKey = getStorageKey(point);
        return increaseBy(storageKey,expireTime);
    }

    protected abstract Object increaseBy(StorageKey storageKey,long expireTime);

    protected abstract int getCurrentInterceptionCount(StorageKey storageKey);

    protected abstract Object postHandle(ProceedingJoinPoint point);
}
