package com.siamese.bri.predicate;

public enum PredicateMode {
    /**
     * 父级Class的断言可以判断子类
     */
    INHERIT("inherit"),

    /**
     * 父级Class的断言可以不可以判断子类
     */
    STRICT("strict");


    String code;

    PredicateMode(String code){
        this.code = code;
    }

    static PredicateMode getMode(String code){
        for(PredicateMode predicateMode:values()) {
            if(predicateMode.code.equals(code)) {
                return predicateMode;
            }
        }
        return null;
    }

}
