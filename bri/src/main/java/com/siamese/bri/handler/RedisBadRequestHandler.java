package com.siamese.bri.handler;

import com.siamese.bri.common.constants.StringConstants;
import com.siamese.bri.common.model.StorageKey;
import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import com.siamese.bri.property.BadRequestProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisBadRequestHandler extends AbstractBadRequestHandler {

    private String NAME_SPACE;

    private StringRedisTemplate redisTemplate;

    private BadRequestProperties properties;



    public RedisBadRequestHandler(BadRequestProperties properties,
                                  StringRedisTemplate stringRedisTemplate,
                                  BadRequestStorageKeyGenerator generator){
        super(generator);
        this.properties = properties;
        this.redisTemplate = stringRedisTemplate;
        this.NAME_SPACE = properties.getBadRequestNamespace();
    }


    @Override
    protected int getCurrentInterceptionCount(StorageKey storageKey) {
        if(!StringUtils.hasText(storageKey.getMethodKey())){
            return 0;
        }
        Object count = redisTemplate.opsForValue().get(NAME_SPACE+
                storageKey.getMethodKey()+ StringConstants.SEPARATOR+storageKey.getParamKey());
        if(Objects.nonNull(count)) {
            return (int) count;
        }
        return 0;
    }

    @Override
    protected Object increaseBy(StorageKey storageKey,long expireTime) {
        String redisKey = NAME_SPACE + storageKey.getMethodKey() + StringConstants.SEPARATOR + storageKey.getParamKey();
        redisTemplate.opsForValue().setIfAbsent(redisKey, "0", expireTime, TimeUnit.MILLISECONDS);
        Long increment = redisTemplate.opsForValue().increment(redisKey, 1);
        if(Objects.nonNull(increment) && increment >1 && properties.isResetExpireTimeOnBadRequest()){
            redisTemplate.expire(redisKey,expireTime,TimeUnit.MILLISECONDS);
        }
        return increment;
    }

    @Override
    public Object flush() {
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent("BAD_REQUEST_INTERCEPTOR_FLUSH_LOCK", StringConstants.EMPTY, properties.getFlushWaitTime(),
                TimeUnit.MILLISECONDS);
        if(Objects.nonNull(setIfAbsent) && setIfAbsent){
            Set<String> keys = redisTemplate.keys(NAME_SPACE);
            if(Objects.nonNull(keys) && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
        }
        return 0;
    }


    public String getNamespace(){
        return this.NAME_SPACE;
    }

    @Override
    protected Object postHandle(ProceedingJoinPoint point) {
        return null;
    }
}
