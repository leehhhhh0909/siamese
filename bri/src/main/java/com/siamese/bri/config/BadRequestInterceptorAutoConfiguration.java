package com.siamese.bri.config;


import com.siamese.bri.aspectj.BadRequestAspectJ;
import com.siamese.bri.handler.BadRequestHandler;
import com.siamese.bri.handler.DefaultBadRequestHandler;
import com.siamese.bri.handler.RedisBadRequestHandler;
import com.siamese.bri.predicate.BadRequestPredicate;
import com.siamese.bri.predicate.BadRequestPredicateFactory;
import com.siamese.bri.predicate.BadRequestPredicateFactoryCustomizer;
import com.siamese.bri.predicate.DefaultBadRequestPredicateFactory;
import com.siamese.bri.property.BadRequestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@SuppressWarnings("rawtypes")
@Import({BadRequestAspectJ.class})
public class BadRequestInterceptorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BadRequestInterceptorAutoConfiguration.class);

    @Configuration
    @ConditionalOnMissingBean(BadRequestHandler.class)
    private static class OnBadRequestHandlerMissing {
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
    @AutoConfigureBefore(OnBadRequestPredicateFactoryMissing.class)
    @ConditionalOnMissingBean(BadRequestPredicateFactory.class)
    private static class OnBadRequestPredicateFactoryExisting implements InitializingBean {

        @Autowired
        BadRequestPredicateFactory badRequestPredicateFactory;

        @Autowired
        List<BadRequestPredicateFactoryCustomizer> factoryCustomizers;

        @Override
        public void afterPropertiesSet() throws Exception {
            factoryCustomizers.forEach(customizer -> customizer.customize(badRequestPredicateFactory));
            badRequestPredicateFactory.lock();
        }
    }




    @Configuration
    @ConditionalOnMissingBean(BadRequestPredicateFactory.class)
    private static class OnBadRequestPredicateFactoryMissing {

        @Bean
        public BadRequestPredicateFactory badRequestPredicateFactory(ObjectProvider<List<BadRequestPredicate>> provider,
                                                                     ObjectProvider<List<BadRequestPredicateFactoryCustomizer>> customizers,
                                                                     BadRequestProperties properties){
            DefaultBadRequestPredicateFactory factory = new DefaultBadRequestPredicateFactory(provider.getIfAvailable(), properties);
            List<BadRequestPredicateFactoryCustomizer> customizerList = customizers.getIfAvailable(ArrayList::new);
            customizerList.forEach(customizer->customizer.customize(factory));
            logger.info("The initialization of BadRequestPredicateFactory is done,and the size of BadRequestPredicateFactory is:{}",factory.lock());
            return factory;
        }
    }








}
