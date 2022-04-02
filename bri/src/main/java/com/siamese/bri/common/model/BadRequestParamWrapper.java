package com.siamese.bri.common.model;

public class BadRequestParamWrapper {

    private Object param;

    private boolean allOf;

    public BadRequestParamWrapper(Object param, boolean allOf) {
        this.param = param;
        this.allOf = allOf;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public boolean isAllOf() {
        return allOf;
    }

    public void setAllOf(boolean allOf) {
        this.allOf = allOf;
    }

}
