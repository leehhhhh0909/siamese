package com.siamese.bri.property;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = "bri")
public class BadRequestProperties extends Properties {
    private String predicateMode;

    private String badRequestNamespace;

    private boolean methodMappingCacheLazily;

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
}
