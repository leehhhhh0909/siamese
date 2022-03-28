package com.siamese.bri.handler;

import org.aspectj.lang.ProceedingJoinPoint;

public class DefaultBadRequestHandler implements BadRequestHandler {
    @Override
    public boolean needIntercept(ProceedingJoinPoint point){
        return false;
    }

    @Override
    public Object handleAfter(ProceedingJoinPoint point) {
        return null;
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
