package com.siamese.bri.common.enumeration;

import org.springframework.util.StringUtils;

public enum StorageKeyGeneratePolicyEnum {

    HASH("hash"),

    STRING("string");

    private String code;

    StorageKeyGeneratePolicyEnum(java.lang.String code) {
        this.code = code;
    }

    public static StorageKeyGeneratePolicyEnum getPolicy(String code){
        if(StringUtils.hasText(code)){
            for(StorageKeyGeneratePolicyEnum policyEnum:values()) {
                if(policyEnum.code.equals(code.toLowerCase())) {
                    return policyEnum;
                }
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
