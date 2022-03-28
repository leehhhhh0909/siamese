package com.siamese.bri.cache;

import com.siamese.bri.annotation.BadRequestInterceptor;
import java.util.Map;

public interface BadRequestCacheMapping<T> {
    T get(BadRequestInterceptor interceptor, Class<?>[] params) throws NoSuchMethodException,
            ClassNotFoundException;

    void lock(Map<String,T> mapping);

    void doBuild() throws NoSuchMethodException, ClassNotFoundException;
}
