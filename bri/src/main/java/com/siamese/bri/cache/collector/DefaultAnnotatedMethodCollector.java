package com.siamese.bri.cache.collector;

import com.siamese.bri.annotation.BadRequestInterceptor;
import com.siamese.bri.common.app.ApplicationContextHolder;
import com.siamese.bri.common.util.ReflectionUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Configuration
@ConditionalOnMissingBean(TargetMethodCollector.class)
public class DefaultAnnotatedMethodCollector  extends ApplicationContextHolder implements TargetMethodCollector {

    @Override
    @SuppressWarnings("unchecked")
    public List<Method> getTargetMethod() {
        Class<?> bootClass = getBootClass();
        if(bootClass == null) return Collections.EMPTY_LIST;
        String bootClassName = bootClass.getPackage().getName();
        ComponentScan componentScan = bootClass.getAnnotation(ComponentScan.class);
        if(componentScan == null || componentScan.basePackages().length == 0) {
            return ReflectionUtils.methodsWithAnnotation(Collections.singletonList(bootClassName), BadRequestInterceptor.class);
        }
        String[] basePackages = componentScan.basePackages();
        List<String> packages = Arrays.asList(basePackages);
        packages.add(bootClassName);
        return ReflectionUtils.methodsWithAnnotation(packages, BadRequestInterceptor.class);
    }




    private Class<?> getBootClass(){
        Map<String, Object> bootClass
                = getApplicationContext().getBeansWithAnnotation(SpringBootApplication.class);
        if(!bootClass.isEmpty()){
            Object o = new ArrayList<>(bootClass.values()).get(0);
            return o.getClass();
        }
        return null;
    }
}
