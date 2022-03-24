package com.siamese.bri.predicate;


public interface BadRequestPredicate<T> {

    Class<T> getTargetClass();

    boolean isBadRequest(T result);
}
