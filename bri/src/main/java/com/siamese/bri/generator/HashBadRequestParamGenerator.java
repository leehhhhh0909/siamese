package com.siamese.bri.generator;

import java.util.Objects;

public class HashBadRequestParamGenerator extends AbstractBadRequestParamGenerator {

    @Override
    public Object getParamGeneration(Object param) {
        return Objects.hash(param);
    }
}
