package com.siamese.bri.property;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = "bri")
public class BadRequestProperties extends Properties {
    /**
     * 可选值: inherit/strict(默认)
     */
    private String predicateMode;

    private String badRequestNamespace;

    private boolean methodMappingCacheLazily;

    /**
     * 可选值: hash/string(默认)
     */
    private String keyGenePolicy;



    public boolean isMethodMappingCacheLazily() {
        return methodMappingCacheLazily;
    }

    public void setMethodMappingCacheLazily(boolean methodMappingCacheLazily) {
        this.methodMappingCacheLazily = methodMappingCacheLazily;
    }

    public String getBadRequestNamespace() {
        return badRequestNamespace;
    }

    public void setBadRequestNamespace(String badRequestNamespace) {
        this.badRequestNamespace = badRequestNamespace;
    }

    public String getPredicateMode() {
        return predicateMode;
    }

    public void setPredicateMode(String predicateMode) {
        this.predicateMode = predicateMode;
    }

    public String getKeyGenePolicy() {
        return keyGenePolicy;
    }

    public void setKeyGenePolicy(String keyGenePolicy) {
        this.keyGenePolicy = keyGenePolicy;
    }
}
