package com.siamese.bri.handler;

import com.siamese.bri.property.BadRequestProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisBadRequestHandler implements BadRequestHandler {

    private String NAME_SPACE;

    private StringRedisTemplate redisTemplate;



    public RedisBadRequestHandler(BadRequestProperties properties,
                                  StringRedisTemplate stringRedisTemplate){
        this.redisTemplate = stringRedisTemplate;
        this.NAME_SPACE = properties.getBadRequestNamespace();
    }


    @Override
    public boolean needIntercept(ProceedingJoinPoint point) {
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


    public String getNamespace(){
        return this.NAME_SPACE;
    }
}
