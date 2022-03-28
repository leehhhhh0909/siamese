package com.siamese.bri.predicate;

@FunctionalInterface
public interface BadRequestDecidable<T> {

    boolean isBadRequest(T result);
}
