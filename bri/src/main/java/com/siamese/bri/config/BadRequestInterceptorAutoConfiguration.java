package com.siamese.bri.config;


import com.siamese.bri.aspectj.BadRequestAspectJ;
import com.siamese.bri.cache.BadRequestCacheMapping;
import com.siamese.bri.cache.FallbackMethodDefaultCacheMapping;
import com.siamese.bri.cache.FallbackMethodLazyCacheMapping;
import com.siamese.bri.cache.collector.DefaultAnnotatedMethodCollector;
import com.siamese.bri.cache.collector.TargetMethodCollector;
import com.siamese.bri.common.enumeration.StorageKeyGeneratePolicyEnum;
import com.siamese.bri.generator.BadRequestParamGenerator;
import com.siamese.bri.generator.BadRequestStorageKeyGenerator;
import com.siamese.bri.generator.DefaultBadRequestStorageKeyGenerator;
import com.siamese.bri.generator.HashBadRequestParamGenerator;
import com.siamese.bri.generator.StringifyBadRequestParamGenerator;
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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@SuppressWarnings("rawtypes")
@Import({BadRequestAspectJ.class})
public class BadRequestInterceptorAutoConfiguration {

    public static final Logger logger = LoggerFactory.getLogger(BadRequestInterceptorAutoConfiguration.class);

    @Configuration
    public static class BadRequestPropertiesAutoConfiguration {

        @Bean
        public BadRequestProperties badRequestProperties(){
            return new BadRequestProperties();
        }
    }

    @Configuration
    @ConditionalOnMissingBean(BadRequestParamGenerator.class)
    @AutoConfigureAfter(BadRequestProperties.class)
    public static class OnBadRequestParamGeneratorMissing {

        @Bean
        public BadRequestParamGenerator badRequestParamGenerator(BadRequestProperties properties){
            String keyGenePolicy = properties.getKeyGenePolicy();
            if(StringUtils.hasText(keyGenePolicy)){
                if(StorageKeyGeneratePolicyEnum.HASH.getCode().equals(keyGenePolicy.toLowerCase())){
                    return new HashBadRequestParamGenerator();
                }
            }
            return new StringifyBadRequestParamGenerator();
        }
    }

    @Configuration
    @AutoConfigureAfter(OnBadRequestParamGeneratorMissing.class)
    public static class OnBadRequestStorageKeyGeneratorMissing {

        @Bean
        public BadRequestStorageKeyGenerator badRequestStorageKeyGenerator(BadRequestParamGenerator generator){
            return new DefaultBadRequestStorageKeyGenerator(generator);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(BadRequestHandler.class)
    @AutoConfigureAfter(OnBadRequestStorageKeyGeneratorMissing.class)
    @ConditionalOnProperty(prefix = "spring",name = "redis")
    public static class RedisBadRequestHandlerAutoConfiguration {

        @Bean
        public BadRequestHandler badRequestHandler(StringRedisTemplate redisTemplate,
                                                   BadRequestProperties properties,
                                                   BadRequestStorageKeyGenerator generator){
            return new RedisBadRequestHandler(properties,redisTemplate,generator);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(BadRequestHandler.class)
    @AutoConfigureAfter(OnBadRequestStorageKeyGeneratorMissing.class)
    public static class OnBadRequestHandlerMissing {

        @Bean
        public BadRequestHandler badRequestHandler(BadRequestProperties properties,
                                                   BadRequestStorageKeyGenerator generator){
            return new DefaultBadRequestHandler(generator,properties);
        }
    }

    @Configuration
    @ConditionalOnBean(BadRequestPredicateFactory.class)
    public static class OnBadRequestPredicateFactoryExisting implements InitializingBean {

        @Autowired
        BadRequestPredicateFactory badRequestPredicateFactory;

        @Autowired(required = false)
        List<BadRequestPredicateFactoryCustomizer> factoryCustomizers;

        @Override
        public void afterPropertiesSet() {
            if(DefaultBadRequestPredicateFactory.class.equals(badRequestPredicateFactory.getClass())) return;
            if(Objects.nonNull(factoryCustomizers) && !factoryCustomizers.isEmpty()) {
                factoryCustomizers.forEach(customizer -> customizer.customize(badRequestPredicateFactory));
            }
            logger.info("The initialization of BadRequestPredicateFactory is done,and the size of BadRequestPredicateFactory is:{}",badRequestPredicateFactory.lock());
        }
    }

    @Configuration
    @ConditionalOnMissingBean(BadRequestPredicateFactory.class)
    public static class OnBadRequestPredicateFactoryMissing {

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

    @Configuration
    @ConditionalOnMissingBean(TargetMethodCollector.class)
    public static class OnTargetMethodCollectorMissing {

        @Bean
        public TargetMethodCollector targetMethodCollector() {
            return new DefaultAnnotatedMethodCollector();
        }

    }

    @Configuration
    @ConditionalOnMissingBean(BadRequestCacheMapping.class)
    @AutoConfigureAfter({BadRequestProperties.class})
    public static class OnBadRequestCacheMappingMissing {

        @Bean
        public BadRequestCacheMapping badRequestCacheMapping(TargetMethodCollector collector,
                                                             BadRequestProperties properties) {
            return properties.isMethodMappingCacheLazily() ? new FallbackMethodLazyCacheMapping(collector) :
                    new FallbackMethodDefaultCacheMapping(collector);
        }

    }
}
