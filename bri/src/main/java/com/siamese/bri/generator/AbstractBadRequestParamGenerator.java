package com.siamese.bri.generator;

import com.siamese.bri.annotation.BadRequestProperty;
import com.siamese.bri.common.model.BadRequestParamWrapper;
import com.siamese.bri.common.util.ReflectionUtils;
import com.siamese.bri.common.constants.StringConstants;
import com.siamese.bri.identifier.BadRequestIdentifier;

import java.lang.reflect.Field;
import java.util.Objects;

public abstract class AbstractBadRequestParamGenerator implements BadRequestParamGenerator {

    @Override
    public String doGenerate(BadRequestParamWrapper... params) throws IllegalAccessException {
        if(params == null || params.length == 0){
            return StringConstants.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        for(BadRequestParamWrapper wrapper:params){
            Object param = wrapper.getParam();
            if(wrapper.isAllOf()){
                builder.append(StringConstants.SEPARATOR).append(getParamGeneration(param));
                continue;
            }
            if(param instanceof BadRequestIdentifier){
                String identifier = ((BadRequestIdentifier) param).getIdentifier();
                builder.append(StringConstants.SEPARATOR).append(identifier);
                continue;
            }
            Class<?> paramClass = param.getClass();
            if(inSpecialClass(paramClass)) {
                builder.append(StringConstants.SEPARATOR).append(param);
                continue;
            }
            Field[] declaredFields = paramClass.getDeclaredFields();
            for(Field f : declaredFields){
                BadRequestProperty badRequestProperty = ReflectionUtils.getAnnotationFromField(f, BadRequestProperty.class);
                if(Objects.nonNull(badRequestProperty)){
                    f.setAccessible(true);
                    Object o = f.get(param);
                    Object value = inSpecialClass(o.getClass()) ? String.valueOf(o) : getParamGeneration(o);
                    builder.append(StringConstants.SEPARATOR).append(value);
                }
            }
        }
        return builder.toString();
    }





    protected boolean inSpecialClass(Class<?> clazz) {
        return Integer.class.equals(clazz) ||
                Long.class.equals(clazz) ||
                Double.class.equals(clazz) ||
                Float.class.equals(clazz) ||
                String.class.equals(clazz) ||
                Character.class.equals(clazz) ||
                Byte.class.equals(clazz) ||
                Short.class.equals(clazz) ||
                Boolean.class.equals(clazz) ||
                int.class.equals(clazz) ||
                byte.class.equals(clazz) ||
                short.class.equals(clazz) ||
                long.class.equals(clazz) ||
                double.class.equals(clazz) ||
                float.class.equals(clazz) ||
                char.class.equals(clazz) ||
                boolean.class.equals(clazz);
    }
}
