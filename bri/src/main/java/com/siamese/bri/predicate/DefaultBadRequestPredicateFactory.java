package com.siamese.bri.predicate;

import com.siamese.bri.exception.BadRequestException;
import com.siamese.bri.predicate.context.BadRequestPredicateContext;
import com.siamese.bri.property.BadRequestProperties;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("rawtypes")
public class DefaultBadRequestPredicateFactory implements BadRequestPredicateFactory{

    private List<BadRequestPredicate> predicates;

    private Map<Class<?>,List<BadRequestPredicate>> predicatesMapping;

    private PredicateMode predicateMode;

    private BuildPredicatesMappingPredicate mappingPredicate;

    private AtomicBoolean locked = new AtomicBoolean(false);

    public DefaultBadRequestPredicateFactory(List<BadRequestPredicate> provider,
                                      BadRequestProperties properties){
        this.predicates = provider;
        this.predicateMode = generatePredicateMode(properties.getPredicateMode());
        this.mappingPredicate = (PredicateMode.STRICT.equals(predicateMode))?(Object::equals):
                (keyClass, targetClass) -> keyClass.equals(targetClass) ||
                        targetClass.isAssignableFrom(keyClass);
        this.predicatesMapping = buildPredicatesMapping(this.mappingPredicate);
    }

    private PredicateMode generatePredicateMode(String predicateMode) {
        PredicateMode mode = PredicateMode.getMode(predicateMode);
        return Objects.nonNull(mode)? mode : PredicateMode.STRICT;
    }

    private Map<Class<?>,List<BadRequestPredicate>> buildPredicatesMapping(BuildPredicatesMappingPredicate mappingPredicate){
        Map<Class<?>, List<BadRequestPredicate>> mapping = new ConcurrentHashMap<>();
        if(predicates != null && !predicates.isEmpty()) {
            for (BadRequestPredicate predicate : predicates) {
                Class targetClass = predicate.getTargetClass();
                if(Objects.isNull(targetClass)) {
                    throw new BadRequestException("targetClass of a BadRequestPredicate can not be null");
                }
                for (Map.Entry<Class<?>, List<BadRequestPredicate>> entry : mapping.entrySet()) {
                    Class<?> keyClass = entry.getKey();
                    if (mappingPredicate.test(keyClass, targetClass)) {
                        entry.getValue().add(predicate);
                    }
                }
                if(!mapping.containsKey(targetClass)){
                    List<BadRequestPredicate> list = new CopyOnWriteArrayList<>();
                    list.add(predicate);
                    mapping.put(targetClass,list);
                }
            }
        }
        return mapping;
    }


    @Override
    public void registerPredicate(BadRequestPredicate predicate){
        if(predicate == null)
            throw new UnsupportedOperationException("BadRequestPredicate can not be null");
        this.predicates.add(predicate);
        Class targetClass = predicate.getTargetClass();
        if(Objects.isNull(targetClass)) {
            throw new BadRequestException("targetClass of a BadRequestPredicate can not be null");
        }
        for(Map.Entry<Class<?>, List<BadRequestPredicate>> entry : this.predicatesMapping.entrySet()){
            Class<?> keyClass = entry.getKey();
            if(this.mappingPredicate.test(keyClass,targetClass)){
                entry.getValue().add(predicate);
            }
        }
        if(!this.predicatesMapping.containsKey(targetClass)){
            List<BadRequestPredicate> list = new CopyOnWriteArrayList<>();
            list.add(predicate);
            this.predicatesMapping.put(targetClass,list);
        }
    }

    @Override
    public BadRequestPredicateContext getPredicatesContext(Class<?> clazz){
        return new BadRequestPredicateContext(this.predicatesMapping.get(clazz));
    }

    @Override
    public int lock(){
        if(!locked.get()){
            this.predicatesMapping = Collections.unmodifiableMap(this.predicatesMapping);
            this.predicates = Collections.unmodifiableList(this.predicates);
            locked.set(true);
            return predicates.size();
        }
        throw new UnsupportedOperationException("BadRequestPredicateFactory has been locked!");
    }


    public List<BadRequestPredicate> getPredicates() {
        return predicates;
    }


    public Map<Class<?>, List<BadRequestPredicate>> getPredicatesMapping() {
        return predicatesMapping;
    }


    public PredicateMode getPredicateMode() {
        return predicateMode;
    }

    @FunctionalInterface
    interface BuildPredicatesMappingPredicate{
        boolean test(Class<?> keyClass,Class<?> targetClass);
    }
}
