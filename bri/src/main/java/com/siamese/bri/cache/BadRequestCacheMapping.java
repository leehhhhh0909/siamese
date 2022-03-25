package com.siamese.bri.cache;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface BadRequestCacheMapping<T> {

    T get(String key);

    Map<String,T> getMapping(List<Method> targetMethods) throws NoSuchMethodException, ClassNotFoundException;

    void lock(Map<String,T> mapping);
}
