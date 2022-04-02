package com.siamese.bri.common.util;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException("can not create an instance of utility class");
    }

    public static <T extends Annotation> T getAnnotationFromMethod(Method method, Class<T> clazz){
        ObjectUtils.nonNull("method and the class of An annotation must be not null!", method, clazz);
        return method.getAnnotation(clazz);
    }

    public static <T extends Annotation> T getAnnotationFromField(Field field, Class<T> clazz) {
        ObjectUtils.nonNull("field and the class of An annotation must be not null!", field, clazz);
        return field.getAnnotation(clazz);
    }

    public static <T extends Annotation> T getAnnotationFromParameter(Parameter parameter ,Class<T> clazz) {
        ObjectUtils.nonNull("parameter and the class of An annotation must be not null!", parameter, clazz);
        return parameter.getAnnotation(clazz);
    }

    public static Class<?>[] getParameters(Method method){
        ObjectUtils.nonNull("method and the class of An annotation must be not null!", method);
        return method.getParameterTypes();
    }

    public static <T> T newInstance(Class<T> clazz){
        ObjectUtils.nonNull("target class must be not null!", clazz);
        try{
            return clazz.newInstance();
        }catch (InstantiationException | IllegalAccessException e){
            return null;
        }
    }

    public static List<Method> methodsWithAnnotation(List<String> packagePathList, Class<? extends Annotation> annotationClass){
        List<Method> methodList = new CopyOnWriteArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        final String RESOURCE_PATTERN = "/**/*.class";
        for(String packageName : packagePathList){
            try {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(packageName) + RESOURCE_PATTERN;
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String classname = reader.getClassMetadata().getClassName();
                    Class<?> clazz;
                    try{
                        clazz = Class.forName(classname);
                    }catch (Throwable cause){
                        continue;
                    }
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    for(Method method:declaredMethods){
                        if (method.isAnnotationPresent(annotationClass)) {
                            methodList.add(method);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return methodList;
    }

    public static Method getSourceMethodByJoinPoint(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        return methodSignature.getMethod();
    }

}
