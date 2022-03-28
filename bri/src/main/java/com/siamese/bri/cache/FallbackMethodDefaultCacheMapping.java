package com.siamese.bri.cache;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.cache.collector.TargetMethodCollector;
import com.siamese.bri.common.util.InterceptorUtils;
import com.siamese.bri.common.util.ReflectionUtils;
import com.siamese.bri.metadata.InterceptorMetadata;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FallbackMethodDefaultCacheMapping extends FallbackMethodCacheMapping {

    @Override
    public Map<String, InterceptorMetadata> initMapping(List<Method> targetMethods) throws NoSuchMethodException, ClassNotFoundException {
        if(targetMethods == null||targetMethods.isEmpty()) return new HashMap<>(0);
        Map<String,InterceptorMetadata> tempMapping = new HashMap<>();
        synchronized (LOCK){
            for(Method method:targetMethods){
                BadRequestInterceptor interceptor = ReflectionUtils.getAnnotationFromMethod(method, BadRequestInterceptor.class);
                String fallback = interceptor.fallback();
                if(!fallback.isEmpty()){
                    Class<?>[] parameters = ReflectionUtils.getParameters(method);
                    String fallbackMappingName = InterceptorUtils.getFallbackMappingName(fallback, parameters);
                    if(!tempMapping.containsKey(fallbackMappingName)){
                        Method fallbackMethod = InterceptorUtils.getFallbackMethod(fallback, parameters);
                        InterceptorMetadata metadata = InterceptorMetadata.analyze(fallbackMethod,parameters,interceptor);
                        tempMapping.put(fallbackMappingName,metadata);
                    }
                }
            }
        }
        return tempMapping;
    }

    public FallbackMethodDefaultCacheMapping(TargetMethodCollector collector) {
        super(collector);
    }

    @Override
    public InterceptorMetadata get(BadRequestInterceptor interceptor,Class<?>[] params) {
        String fallbackMappingName = InterceptorUtils.getFallbackMappingName(interceptor.fallback(), params);
        return getFallbackMethodMapping().get(fallbackMappingName);
    }
}
