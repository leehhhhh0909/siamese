package com.siamese.bri.common.enumeration;


import org.springframework.util.StringUtils;


public enum PredicateModeEnum {

    INHERIT("inherit"),


    STRICT("strict");


    private String code;

    PredicateModeEnum(String code){
        this.code = code;
    }

    public static PredicateModeEnum getMode(String code){
        if(StringUtils.hasText(code)){
            for(PredicateModeEnum predicateMode:values()) {
                if(predicateMode.code.equals(code.toLowerCase())) {
                    return predicateMode;
                }
            }
        }
        return null;
    }

}
