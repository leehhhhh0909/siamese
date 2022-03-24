package com.siamese.bri.predicate;

public interface BadRequestDecidable<T> {

    boolean isBadRequest(T result);
}
