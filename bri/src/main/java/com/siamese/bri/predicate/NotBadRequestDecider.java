package com.siamese.bri.predicate;

@SuppressWarnings("rawtypes")
public class NotBadRequestDecider implements BadRequestDecidable {


    private static NotBadRequestDecider decider;

    private NotBadRequestDecider(){

    }

    public static NotBadRequestDecider getDecider(){
       if(decider == null){
           synchronized (NotBadRequestDecider.class){
               if(decider == null){
                   decider = new NotBadRequestDecider();
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
