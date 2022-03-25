package com.siamese.bri.util;

import java.util.Objects;

public final class ObjectUtils {
    private ObjectUtils() {
        throw new UnsupportedOperationException("can not create an instance of utility class");
    }


    public static void nonNull(String message,Object...objects){
        if(objects == null || objects.length == 0) return;
        for(Object o:objects){
            if(Objects.isNull(o)){
                throw new IllegalArgumentException(message);
            }
        }
    }
}
