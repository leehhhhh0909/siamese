package com.siamese.bri.property;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "bri")
public class BadRequestProperties{

    private String predicateMode = "strict";

    private String badRequestNamespace = "BRI_"+ UUID.randomUUID().toString().replaceAll("-","")+"_";

    private boolean methodMappingCacheLazily = false;

    private String keyGenePolicy = "string";

    private long flushWaitTime = 10000L;

    private boolean resetExpireTimeOnBadRequest = false;

    private long checkIntervalOnMemoryCache = 10000L;

    public String getPredicateMode() {
        return predicateMode;
    }

    public void setPredicateMode(String predicateMode) {
        this.predicateMode = predicateMode;
    }

    public String getBadRequestNamespace() {
        return badRequestNamespace;
    }

    public void setBadRequestNamespace(String badRequestNamespace) {
        this.badRequestNamespace = badRequestNamespace;
    }

    public boolean isMethodMappingCacheLazily() {
        return methodMappingCacheLazily;
    }

    public void setMethodMappingCacheLazily(boolean methodMappingCacheLazily) {
        this.methodMappingCacheLazily = methodMappingCacheLazily;
    }

    public String getKeyGenePolicy() {
        return keyGenePolicy;
    }

    public void setKeyGenePolicy(String keyGenePolicy) {
        this.keyGenePolicy = keyGenePolicy;
    }

    public long getFlushWaitTime() {
        return flushWaitTime;
    }

    public void setFlushWaitTime(long flushWaitTime) {
        this.flushWaitTime = flushWaitTime;
    }

    public boolean isResetExpireTimeOnBadRequest() {
        return resetExpireTimeOnBadRequest;
    }

    public void setResetExpireTimeOnBadRequest(boolean resetExpireTimeOnBadRequest) {
        this.resetExpireTimeOnBadRequest = resetExpireTimeOnBadRequest;
    }

    public long getCheckIntervalOnMemoryCache() {
        return checkIntervalOnMemoryCache;
    }

    public void setCheckIntervalOnMemoryCache(long checkIntervalOnMemoryCache) {
        this.checkIntervalOnMemoryCache = checkIntervalOnMemoryCache;
    }
}
