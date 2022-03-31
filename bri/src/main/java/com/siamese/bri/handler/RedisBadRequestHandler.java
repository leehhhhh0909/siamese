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
import java.util.UUID;

public class RedisBadRequestHandler extends AbstractBadRequestHandler {

    private String NAME_SPACE;

    private StringRedisTemplate redisTemplate;



    public RedisBadRequestHandler(BadRequestProperties properties,
                                  StringRedisTemplate stringRedisTemplate,
                                  BadRequestStorageKeyGenerator generator){
        super(generator);
        this.redisTemplate = stringRedisTemplate;
        this.NAME_SPACE = StringUtils.hasText(properties.getBadRequestNamespace()) ?
                properties.getBadRequestNamespace() : ("BRI_"+ UUID.randomUUID().toString().replaceAll("-",""));
    }


    @Override
    protected int getCurrentInterceptionCount(StorageKey storageKey) {
        if(!StringUtils.hasText(storageKey.getMethodKey())){
            return 0;
        }
        Object count = redisTemplate.opsForHash().get(NAME_SPACE,
                storageKey.getMethodKey()+ StringConstants.SEPARATOR+storageKey.getParamKey());
        if(Objects.nonNull(count)) {
            return (int) count;
        }
        return 0;
    }

    @Override
    protected Object increaseBy(StorageKey storageKey) {
        //todo 并发问题
        if(redisTemplate.opsForHash().hasKey(NAME_SPACE,
                storageKey.getMethodKey() + StringConstants.SEPARATOR + storageKey.getParamKey())) {
            return redisTemplate.opsForHash().increment(NAME_SPACE,
                    storageKey.getMethodKey() + StringConstants.SEPARATOR + storageKey.getParamKey(), 1);
        }
        redisTemplate.opsForHash().put(NAME_SPACE,
                storageKey.getMethodKey() + StringConstants.SEPARATOR + storageKey.getParamKey(), 1);
        return 1L;
    }

    @Override
    public Object flush() {
        //todo 并发处理
        Set<Object> keys = redisTemplate.opsForHash().keys(NAME_SPACE);
        return redisTemplate.opsForHash().delete(NAME_SPACE,keys);
    }


    public String getNamespace(){
        return this.NAME_SPACE;
    }

    @Override
    protected Object postHandle(ProceedingJoinPoint point) {
        return null;
    }
}
