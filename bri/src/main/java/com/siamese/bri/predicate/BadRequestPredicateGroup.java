package com.siamese.bri.predicate;


import java.util.List;

@SuppressWarnings({"rawtypes","unchecked"})
public class BadRequestPredicateGroup implements BadRequestDecidable {

    private List<BadRequestPredicate> predicates;

    public BadRequestPredicateGroup(List<BadRequestPredicate> predicates){
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
