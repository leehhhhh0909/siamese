package com.siamese.bri.recorder;

import java.lang.reflect.Method;

public interface BadRequestRecorder {

    int doRecord(Method method,Object...params);
}
