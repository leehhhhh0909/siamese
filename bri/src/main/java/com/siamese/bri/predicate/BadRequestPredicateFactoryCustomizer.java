package com.siamese.bri.predicate;

@FunctionalInterface
public interface BadRequestPredicateFactoryCustomizer {

    void customize(BadRequestPredicateFactory predicateFactory);
}
