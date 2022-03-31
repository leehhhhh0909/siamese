package com.siamese.bri.cache;

import com.siamese.bri.annotation.BadRequestInterceptor;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

public interface BadRequestCacheMapping<T> extends InitializingBean {
    T get(BadRequestInterceptor interceptor, Class<?>[] params) throws NoSuchMethodException,
            ClassNotFoundException;

    void lock(Map<String,T> mapping);

    void doBuild() throws NoSuchMethodException, ClassNotFoundException;

    @Override
    default void afterPropertiesSet() throws Exception {
        doBuild();
    }
}
