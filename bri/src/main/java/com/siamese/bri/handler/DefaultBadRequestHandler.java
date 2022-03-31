package com.siamese.bri.handler;

import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;

public class DefaultBadRequestHandler extends AbstractBadRequestHandler {

    public DefaultBadRequestHandler(BadRequestStorageKeyGenerator generator) {
        super(generator);
    }

    @Override
    protected Object postHandle(ProceedingJoinPoint point) {
        return null;
    }

    @Override
    public boolean needIntercept(ProceedingJoinPoint point){
        return false;
    }



    @Override
    public Object record(ProceedingJoinPoint point) throws IllegalAccessException {
        return null;
    }

    @Override
    public Object flush() {
        return null;
    }
}
