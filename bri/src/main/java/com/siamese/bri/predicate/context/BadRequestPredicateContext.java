package com.siamese.bri.predicate.context;

import com.siamese.bri.predicate.BadRequestDecidable;
import com.siamese.bri.predicate.BadRequestPredicate;

import java.util.List;

@SuppressWarnings({"rawtypes","unchecked"})
public class BadRequestPredicateContext implements BadRequestDecidable {

    private List<BadRequestPredicate> predicates;

    public BadRequestPredicateContext(List<BadRequestPredicate> predicates){
        this.predicates = predicates;
    }

    public List<BadRequestPredicate> getPredicates(){
        return this.predicates;
    }

    @Override
    public boolean isBadRequest(Object result){
        if(this.predicates == null || this.predicates.isEmpty()) return false;
        for(BadRequestPredicate predicate:predicates){
            if(predicate.isBadRequest(result)) return true;
        }
        return false;
    };
}
