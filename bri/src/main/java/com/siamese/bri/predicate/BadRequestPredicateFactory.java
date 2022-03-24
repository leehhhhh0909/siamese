package com.siamese.bri.predicate;

import com.siamese.bri.predicate.context.BadRequestPredicateContext;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface BadRequestPredicateFactory {

    void registerPredicate(BadRequestPredicate predicate);

    int lock();

    BadRequestPredicateContext getPredicatesContext(Class<?> clazz);
}
