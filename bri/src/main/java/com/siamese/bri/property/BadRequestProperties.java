package com.siamese.bri.property;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = "bri")
public class BadRequestProperties extends Properties {
    private String predicateMode;


    public String getPredicateMode() {
        return predicateMode;
    }

    public void setPredicateMode(String predicateMode) {
        this.predicateMode = predicateMode;
    }
}
