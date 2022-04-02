package com.siamese.bri.cache;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.cache.collector.TargetMethodCollector;
import com.siamese.bri.common.util.InterceptorUtils;
import com.siamese.bri.metadata.InterceptorMetadata;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FallbackMethodLazyCacheMapping extends FallbackMethodCacheMapping {

    @Override
    public Map<String, InterceptorMetadata> initMapping(List<Method> targetMethods) {
        return new ConcurrentHashMap<>(256);
    }

    public FallbackMethodLazyCacheMapping(TargetMethodCollector collector) {
        super(collector);
    }

    @Override
    public InterceptorMetadata get(BadRequestInterceptor interceptor, Class<?>[] params) throws NoSuchMethodException, ClassNotFoundException {
        String fallbackMappingName = InterceptorUtils.getFallbackMappingName(interceptor.fallback(), params);
        if(getFallbackMethodMapping().containsKey(fallbackMappingName)){
            return getFallbackMethodMapping().get(fallbackMappingName);
        }
        Method fallbackMethod = InterceptorUtils.getFallbackMethod(interceptor.fallback(), params);
        InterceptorMetadata metadata = InterceptorMetadata.analyze(fallbackMethod, params, interceptor);
        getFallbackMethodMapping().putIfAbsent(fallbackMappingName,metadata);
        return metadata;
    }

    @Override
    public void lock(Map<String, InterceptorMetadata> mapping) {
    }
}
