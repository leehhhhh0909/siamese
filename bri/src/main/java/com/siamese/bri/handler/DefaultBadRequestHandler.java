package com.siamese.bri.handler;

import com.siamese.bri.cache.record.MemoryInterceptorCache;
import com.siamese.bri.common.model.StorageKey;
import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StringUtils;


public class DefaultBadRequestHandler extends AbstractBadRequestHandler {

    public DefaultBadRequestHandler(BadRequestStorageKeyGenerator generator) {
        super(generator);
        MemoryInterceptorCache.initialize();
    }


    @Override
    protected Object postHandle(ProceedingJoinPoint point) {
        return null;
    }

    @Override
    protected int getCurrentInterceptionCount(StorageKey storageKey) {
        if(!StringUtils.hasText(storageKey.getMethodKey())){
            return 0;
        }
        return MemoryInterceptorCache.getCount(storageKey.getMethodKey(),storageKey.getParamKey());
    }

    @Override
    protected Object increaseBy(StorageKey storageKey) {
        return MemoryInterceptorCache.increase(storageKey.getMethodKey(),storageKey.getParamKey());
    }

    @Override
    public Object flush() {
        return MemoryInterceptorCache.clearAll();
    }
}
