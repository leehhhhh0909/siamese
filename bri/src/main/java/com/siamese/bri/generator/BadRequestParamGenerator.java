package com.siamese.bri.generator;

import com.siamese.bri.common.BadRequestParamWrapper;

public interface BadRequestParamGenerator {

    String doGenerate(BadRequestParamWrapper...params) throws IllegalAccessException;

    Object getParamGeneration(Object param);
}
