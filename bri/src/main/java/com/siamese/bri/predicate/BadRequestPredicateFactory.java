package com.siamese.bri.predicate;


@SuppressWarnings("rawtypes")
public interface BadRequestPredicateFactory {

    void registerPredicate(BadRequestPredicate predicate);

    int lock();

    BadRequestDecidable getBadRequestDecider(Class<?> clazz);
}
