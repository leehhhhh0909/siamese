package com.siamese.bri.generator;


import java.util.Objects;

public class StringifyBadRequestParamGenerator extends AbstractBadRequestParamGenerator{

    @Override
    public Object doGenerateByParam(Object param) {
        return Objects.toString(param);
    }
}
