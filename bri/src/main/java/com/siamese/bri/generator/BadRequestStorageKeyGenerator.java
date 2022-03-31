package com.siamese.bri.generator;

import com.siamese.bri.common.model.StorageKey;

import java.lang.reflect.Method;

public interface BadRequestStorageKeyGenerator {

    StorageKey getStorageKey(Method method, Object...params) throws IllegalAccessException;
}
