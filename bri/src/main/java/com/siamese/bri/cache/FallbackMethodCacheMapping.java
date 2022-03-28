package com.siamese.bri.cache;

import com.siamese.bri.cache.collector.TargetMethodCollector;
import com.siamese.bri.metadata.InterceptorMetadata;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FallbackMethodCacheMapping implements BadRequestCacheMapping<InterceptorMetadata> {

    private Map<String,InterceptorMetadata> fallbackMethodMapping;

    private AtomicBoolean init = new AtomicBoolean(false);

    private AtomicBoolean locked = new AtomicBoolean(false);

    protected final Object LOCK = new Object();

    private TargetMethodCollector collector;

    public FallbackMethodCacheMapping(TargetMethodCollector collector) {
        this.collector = collector;
    }

    public void doBuild() throws NoSuchMethodException, ClassNotFoundException {
        if(!init.get()){
            synchronized (LOCK){
                if(!init.get()){
                    List<Method> targetMethod = collector.getTargetMethod();
                    fallbackMethodMapping = initMapping(targetMethod);
                    lock(fallbackMethodMapping);
                    init.set(true);
                    return;
                }
            }
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

    public Map<String, InterceptorMetadata> getFallbackMethodMapping() {
        return fallbackMethodMapping;
    }


    abstract Map<String,InterceptorMetadata> initMapping(List<Method> targetMethods) throws NoSuchMethodException, ClassNotFoundException;

}
