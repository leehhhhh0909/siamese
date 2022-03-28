package com.siamese.bri.predicate;

@SuppressWarnings("rawtypes")
public class NonBadRequestDecider implements BadRequestDecidable {


    private static NonBadRequestDecider decider;

    private NonBadRequestDecider(){

    }

    public static NonBadRequestDecider getDecider(){
       if(decider == null){
           synchronized (NonBadRequestDecider.class){
               if(decider == null){
                   decider = new NonBadRequestDecider();
               }
           }
       }
       return decider;
    }

    @Override
    public boolean isBadRequest(Object result) {
        return false;
    }
}
