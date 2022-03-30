package com.siamese.bri.generator;

import java.util.Objects;

public class StringifyBadRequestParamGenerator extends AbstractBadRequestParamGenerator{

    @Override
    public Object getParamGeneration(Object param) {
        return Objects.toString(param);
    }
}
