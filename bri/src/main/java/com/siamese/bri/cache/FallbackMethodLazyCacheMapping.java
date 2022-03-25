package com.siamese.bri.cache;

import com.siamese.bri.metadata.InterceptorMetadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FallbackMethodLazyCacheMapping extends FallbackMethodCacheMapping {

    @Override
    public Map<String, InterceptorMetadata> getMapping(List<Method> targetMethods) {
        return new ConcurrentHashMap<>(256);
    }


    public FallbackMethodLazyCacheMapping(TargetMethodCollector collector) {
        super(collector);
    }

    @Override
    InterceptorMetadata getMethod(String fallback,Class<?>...params) {
        return null;
    }
}
