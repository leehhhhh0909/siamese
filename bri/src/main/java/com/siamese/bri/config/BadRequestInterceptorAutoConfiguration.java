package com.siamese.bri.config;


import com.siamese.bri.handler.BadRequestHandler;
import com.siamese.bri.handler.DefaultBadRequestHandler;
import com.siamese.bri.handler.RedisBadRequestHandler;
import com.siamese.bri.predicate.BadRequestPredicate;
import com.siamese.bri.predicate.BadRequestPredicateFactory;
import com.siamese.bri.predicate.BadRequestPredicateFactoryCustomizer;
import com.siamese.bri.predicate.DefaultBadRequestPredicateFactory;
import com.siamese.bri.property.BadRequestProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@SuppressWarnings("rawtypes")
//@Import({BadRequestHandleAspectJ.class})
public class BadRequestInterceptorAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(BadRequestHandler.class)
    private static class BadRequestHandlerAutoConfiguration{
        @Bean
        public BadRequestHandler badRequestHandler(StringRedisTemplate redisTemplate,
                                                   BadRequestProperties properties){
            if(Objects.nonNull(redisTemplate)){
                return new RedisBadRequestHandler(properties,redisTemplate);
            }
            return new DefaultBadRequestHandler();
        }
    }


    @Configuration
    @ConditionalOnMissingBean(BadRequestPredicateFactory.class)
    private static class BadRequestPredicateFactoryAutoConfiguration{

        @Bean
        public BadRequestPredicateFactory badRequestPredicateFactory(ObjectProvider<List<BadRequestPredicate>> provider,
                                                                     ObjectProvider<List<BadRequestPredicateFactoryCustomizer>> customizers,
                                                                     BadRequestProperties properties){
            DefaultBadRequestPredicateFactory factory = new DefaultBadRequestPredicateFactory(provider.getIfAvailable(), properties);
            List<BadRequestPredicateFactoryCustomizer> customizerList = customizers.getIfAvailable(ArrayList::new);
            customizerList.forEach(customizer->customizer.customize(factory));
            factory.lock();
            return factory;
        }
    }




}
