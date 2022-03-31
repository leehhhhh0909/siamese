package com.siamese.bri.common.model;

import java.util.Objects;

public class StorageKey {
    private String methodKey;

    private String paramKey;


    public String getMethodKey() {
        return methodKey;
    }


    public String getParamKey() {
        return paramKey;
    }


    public StorageKey() {
    }

    public StorageKey(String methodKey, String paramKey) {
        this.methodKey = methodKey;
        this.paramKey = paramKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StorageKey that = (StorageKey) o;
        return methodKey.equals(that.methodKey) &&
                paramKey.equals(that.paramKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodKey, paramKey);
    }


    @Override
    public String toString() {
        return "StorageKey{" +
                "methodKey='" + methodKey + '\'' +
                ", paramKey='" + paramKey + '\'' +
                '}';
    }
}
