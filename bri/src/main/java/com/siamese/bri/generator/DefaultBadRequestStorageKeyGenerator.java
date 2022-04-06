package com.siamese.bri.generator;

import com.siamese.bri.annotation.BadRequestParam;
import com.siamese.bri.common.model.BadRequestParamWrapper;
import com.siamese.bri.common.model.StorageKey;
import com.siamese.bri.common.util.ObjectUtils;
import com.siamese.bri.common.util.ReflectionUtils;
import com.siamese.bri.common.constants.StringConstants;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultBadRequestStorageKeyGenerator implements BadRequestStorageKeyGenerator {

    private BadRequestParamGenerator paramGenerator;

    @Autowired
    public DefaultBadRequestStorageKeyGenerator(BadRequestParamGenerator paramGenerator) {
        this.paramGenerator = paramGenerator;
    }

    @Override
    public StorageKey getStorageKey(Method method, Object... params) throws IllegalAccessException {
        ObjectUtils.nonNull("method must not be null!",method);
        String methodPath = method.getDeclaringClass().getName() + StringConstants.SEPARATOR + method.getName();
        Parameter[] parameters = method.getParameters();
        if(Objects.isNull(parameters) || parameters.length == 0) {
            return new StorageKey(methodPath,StringConstants.EMPTY);
        }
        StringBuilder builder = new StringBuilder(methodPath);
        int length = parameters.length;
        List<BadRequestParamWrapper> wrappers = new ArrayList<>();
        for(int i = 0;i<length;i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            builder.append(StringConstants.SEPARATOR).append(parameterType.getName());
            BadRequestParam annotation = ReflectionUtils.getAnnotationFromParameter(parameter, BadRequestParam.class);
            if(Objects.nonNull(annotation)){
                wrappers.add(new BadRequestParamWrapper(params[i],annotation.allOf()));
            }
        }
        return new StorageKey(builder.toString(),paramGenerator.doGenerate(wrappers.toArray(new BadRequestParamWrapper[0])));
    }
}

