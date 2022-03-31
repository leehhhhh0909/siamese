package com.siamese.bri.common.model;

public class StorageKey {
    private String methodKey;

    private String paramKey;


    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public StorageKey() {
    }

    public StorageKey(String methodKey, String paramKey) {
        this.methodKey = methodKey;
        this.paramKey = paramKey;
    }
}
