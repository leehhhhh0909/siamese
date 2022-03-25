package com.siamese.bri.cache;

import com.siamese.bri.metadata.InterceptorMetadata;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FallbackMethodCacheMapping implements BadRequestCacheMapping<InterceptorMetadata> {

    private Map<String,InterceptorMetadata> fallbackMethodMapping;

    private AtomicBoolean init = new AtomicBoolean(false);

    private AtomicBoolean locked = new AtomicBoolean(false);

    private TargetMethodCollector collector;

    public FallbackMethodCacheMapping(TargetMethodCollector collector) {
        this.collector = collector;
    }

    @Override
    public InterceptorMetadata get(String fallback) {
        InterceptorMetadata methodMetadata = fallbackMethodMapping.get(fallback);
        if(Objects.nonNull(methodMetadata)) return methodMetadata;
        return getMethod(fallback);
    }

    public void doBuild() throws NoSuchMethodException, ClassNotFoundException {
        if(!init.get()){
            List<Method> targetMethod = collector.getTargetMethod();
            fallbackMethodMapping = getMapping(targetMethod);
            lock(fallbackMethodMapping);
            init.set(true);
            return;
        }
        throw new UnsupportedOperationException("cache mapping has been initialized!");
    }


    @Override
    public void lock(Map<String, InterceptorMetadata> mapping) {
        if(!locked.get()){
            fallbackMethodMapping = Collections.unmodifiableMap(fallbackMethodMapping);
            locked.set(true);
            return;
        }
        throw new UnsupportedOperationException("cache mapping has been locked!");
    }


    abstract InterceptorMetadata getMethod(String fallback,Class<?>...params);
}
