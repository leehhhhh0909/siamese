package com.siamese.bri.cache.record;

import com.siamese.bri.property.BadRequestProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class MemoryInterceptorCache {

    private volatile static Map<String, Map<String,BadRequestInterceptedRecord>> CACHE;

    private static BadRequestProperties properties;

    private static final ReentrantLock  LOCK = new ReentrantLock();

    private MemoryInterceptorCache() {

    }

    public static void initialize(BadRequestProperties properties) {
        MemoryInterceptorCache.properties = properties;
        if(Objects.isNull(CACHE)) {
            synchronized (MemoryInterceptorCache.class) {
                if(Objects.isNull(CACHE)) {
                    CACHE = new ConcurrentHashMap<>(128);
                    handleExpired();
                }
            }
        }
    }


    public static int getCount(String methodKey,String paramKey) {
        checkInitialization();
        try{
            return CACHE.get(methodKey).get(paramKey).getTimes();
        }catch (NullPointerException e){
            return 0;
        }
    }

    public static int increase(String methodKey,String paramKey,long expireTime) {
        try{
            LOCK.lock();
            Map<String, BadRequestInterceptedRecord> innerMap = CACHE.get(methodKey);
            if(Objects.nonNull(innerMap) && !innerMap.isEmpty()) {
                BadRequestInterceptedRecord record = innerMap.get(paramKey);
                if(Objects.nonNull(record)) {
                    if(record.getExpireBy() <= System.currentTimeMillis()){
                        return record.reset(expireTime);
                    }
                    if(properties.isResetExpireTimeOnBadRequest()){
                        record.updateExpireTime(expireTime);
                    }
                    return record.increaseAndGet();
                }
                record = new BadRequestInterceptedRecord(1,System.currentTimeMillis()+expireTime);
                innerMap.put(paramKey,record);
                return 1;
            }
            Map<String,BadRequestInterceptedRecord> tempMap = new HashMap<>();
            BadRequestInterceptedRecord record = new BadRequestInterceptedRecord(1, System.currentTimeMillis() + expireTime);
            tempMap.put(paramKey,record);
            CACHE.put(methodKey,tempMap);
            return 1;
        }finally {
            LOCK.unlock();
        }
    }




    public static int clearAll(){
        try{
            LOCK.lock();
            int size = 0;
            for(Map.Entry<String,Map<String,BadRequestInterceptedRecord>> entry : CACHE.entrySet()) {
                size += entry.getValue().size();
            }
            CACHE.clear();
            return size;
        }finally {
            LOCK.unlock();
        }
    }


    private static void checkInitialization() {
        if(Objects.isNull(CACHE)) {
            throw new UnsupportedOperationException("MemoryInterceptorCache hasn't been initialized!");
        }
    }




    private static void handleExpired() {
        CompletableFuture.runAsync(()->{
            while(true){
                try {
                    TimeUnit.MILLISECONDS.sleep(properties.getCheckIntervalOnMemoryCache());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(LOCK.tryLock()){
                    try{
                        for(Map.Entry<String,Map<String,BadRequestInterceptedRecord>> outEntry : CACHE.entrySet()) {
                            for(Map.Entry<String,BadRequestInterceptedRecord> innerEntry : outEntry.getValue().entrySet()) {
                                if(innerEntry.getValue().getExpireBy() <= System.currentTimeMillis()) {
                                    CACHE.get(outEntry.getKey()).remove(innerEntry.getKey());
                                }
                            }
                        }
                    }finally {
                        LOCK.unlock();
                    }
                }
            }
        }).exceptionally(e->{
            handleExpired();
            return null;
        });
    }


    static class BadRequestInterceptedRecord {

        private int times;

        private long expireBy;

        BadRequestInterceptedRecord (int times,long expireBy) {
            this.times = times;
            this.expireBy = expireBy;
        }

        public int getTimes() {
            return this.times;
        }


        public long getExpireBy() {
            return this.expireBy;
        }

        public int increaseAndGet() {
            this.times += 1;
            return times;
        }

        public int reset(long expireTime) {
            this.times = 1;
            this.expireBy = System.currentTimeMillis() + expireTime;
            return times;
        }


        public void updateExpireTime(long expireTime) {
            this.expireBy = System.currentTimeMillis() + expireTime;
        }
    }
}
