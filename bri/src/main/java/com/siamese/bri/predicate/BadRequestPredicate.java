package com.siamese.bri.predicate;


public interface BadRequestPredicate<T> extends BadRequestDecidable<T>{

    Class<T> getTargetClass();
}
