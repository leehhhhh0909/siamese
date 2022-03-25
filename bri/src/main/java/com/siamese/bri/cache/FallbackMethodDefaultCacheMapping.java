package com.siamese.bri.cache;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.exception.BadRequestException;
import com.siamese.bri.metadata.InterceptorMetadata;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FallbackMethodDefaultCacheMapping extends FallbackMethodCacheMapping {

    private final Object LOCK = new Object();

    @Override
    public Map<String, InterceptorMetadata> getMapping(List<Method> targetMethods) throws NoSuchMethodException, ClassNotFoundException {
        if(targetMethods == null||targetMethods.isEmpty()) return new HashMap<>(0);
        Map<String,InterceptorMetadata> tempMapping = new HashMap<>();
        synchronized (LOCK){
            for(Method method:targetMethods){
                BadRequestInterceptor interceptor = ReflectionUtils.getAnnotationFromMethod(method, BadRequestInterceptor.class);
                String fallback = interceptor.fallback();
                if(!tempMapping.containsKey(fallback)){
                    Class<?>[] parameters = ReflectionUtils.getParameters(method);
                    Method fallbackMethod = InterceptorUtils.getFallbackMethod(fallback, parameters);
                    InterceptorMetadata metadata = InterceptorMetadata.wrap(fallbackMethod,parameters,interceptor);
                    tempMapping.put(InterceptorUtils.getFallbackMappingName(fallback,parameters),metadata);
                }
            }
        }
        return tempMapping;
    }

    public FallbackMethodDefaultCacheMapping(TargetMethodCollector collector) {
        super(collector);
    }

    @Override
    InterceptorMetadata getMethod(String fallback,Class<?>...params) {
        throw new BadRequestException(String.format("the fallback method for： %s doesn't exist!",fallback));
    }
}
