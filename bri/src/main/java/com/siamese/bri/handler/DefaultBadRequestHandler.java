package com.siamese.bri.handler;

import com.siamese.bri.cache.record.MemoryInterceptorCache;
import com.siamese.bri.common.model.StorageKey;
import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import com.siamese.bri.property.BadRequestProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StringUtils;


public class DefaultBadRequestHandler extends AbstractBadRequestHandler {

    private BadRequestProperties properties;

    public DefaultBadRequestHandler(BadRequestStorageKeyGenerator generator,
                                    BadRequestProperties properties) {
        super(generator);
        this.properties = properties;
        MemoryInterceptorCache.initialize(properties);
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
    protected Object increaseBy(StorageKey storageKey,long expireTime) {
        return MemoryInterceptorCache.increase(storageKey.getMethodKey(),storageKey.getParamKey(),expireTime);
    }

    @Override
    public Object flush() {
        return MemoryInterceptorCache.clearAll();
    }
}
