package com.siamese.bri.cache.record;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryInterceptorCache {

    private volatile static Map<String, Map<String,Integer>> CACHE;

    private final static Object LOCK = new Object();


    private MemoryInterceptorCache() {

    }

    public static void initialize() {
        if(Objects.isNull(CACHE)) {
            synchronized (MemoryInterceptorCache.class) {
                if(Objects.isNull(CACHE)) {
                    CACHE = new ConcurrentHashMap<>(128);
                }
            }
        }
    }


    public static int getCount(String methodKey,String paramKey) {
        checkInitialization();
        Map<String, Integer> mapping = CACHE.get(methodKey);
        if(Objects.nonNull(mapping)) {
            Integer count = mapping.get(paramKey);
            if(Objects.nonNull(count)) {
                return count;
            }
        }
        return 0;
    }


    public static boolean hasMethodKey(String methodKey) {
        if(Objects.isNull(CACHE)) return false;
        return CACHE.containsKey(methodKey);
    }

    public static boolean hasParamKey(String methodKey,String paramKey) {
        if(Objects.isNull(CACHE)) return false;
        if(!CACHE.containsKey(methodKey)) return false;
        return CACHE.get(methodKey).containsKey(paramKey);
    }


    public static int increase(String methodKey,String paramKey) {
        checkInitialization();
        if(CACHE.containsKey(methodKey)){
            Map<String, Integer> outerMap = CACHE.get(methodKey);
            if(outerMap.containsKey(paramKey)) {
                Integer count = outerMap.get(paramKey);
                outerMap.put(paramKey,count+1);
                return count+1;
            }
            outerMap.putIfAbsent(paramKey,1);
            return 1;
        }
        Map<String,Integer> innerMap = new ConcurrentHashMap<>();
        innerMap.put(paramKey,1);
        CACHE.putIfAbsent(methodKey,innerMap);
        return 1;
    }


    public static int clearAll(){
        int size = 0;
        synchronized (LOCK) {
            for(Map.Entry<String,Map<String,Integer>> entry : CACHE.entrySet()) {
                size += entry.getValue().size();
            }
        }
        CACHE.clear();
        return size;
    }


    private static void checkInitialization() {
        if(Objects.isNull(CACHE)) {
            throw new UnsupportedOperationException("MemoryInterceptorCache hasn't been initialized!");
        }
    }


}
