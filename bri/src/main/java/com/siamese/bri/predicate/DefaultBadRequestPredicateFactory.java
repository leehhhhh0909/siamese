package com.siamese.bri.predicate;

import com.siamese.bri.common.enumeration.PredicateModeEnum;
import com.siamese.bri.exception.BadRequestException;
import com.siamese.bri.property.BadRequestProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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

    private BadRequestProperties properties;

    private boolean inheritLyGet;

    private AtomicBoolean locked = new AtomicBoolean(false);

    public DefaultBadRequestPredicateFactory(List<BadRequestPredicate> predicates,
                                             BadRequestProperties properties){
        this.predicates = Objects.isNull(predicates) ? new CopyOnWriteArrayList<>() : predicates;
        this.properties = properties;
        this.inheritLyGet = PredicateModeEnum.INHERIT.equals(PredicateModeEnum.getMode(this.properties.getPredicateMode()));
        this.predicatesMapping = buildPredicatesMapping();
    }

    private Map<Class<?>,List<BadRequestPredicate>> buildPredicatesMapping(){
        Map<Class<?>, List<BadRequestPredicate>> mapping = new ConcurrentHashMap<>();
        if(predicates != null && !predicates.isEmpty()) {
            for (BadRequestPredicate predicate : predicates) {
                Class targetClass = predicate.getTargetClass();
                if(Objects.isNull(targetClass)) {
                    throw new BadRequestException("targetClass of a BadRequestPredicate can not be null");
                }
                for (Map.Entry<Class<?>, List<BadRequestPredicate>> entry : mapping.entrySet()) {
                    Class<?> keyClass = entry.getKey();
                    if (targetClass.equals(keyClass)) {
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
            if(targetClass.equals(keyClass)){
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
    public BadRequestDecidable getBadRequestDecider(Class<?> clazz) {
        List<BadRequestPredicate> badRequestPredicates = getPredicatesByClass(clazz);
        if(badRequestPredicates == null || badRequestPredicates.isEmpty()){
            return NonBadRequestDecider.getDecider();
        }
        return badRequestPredicates.size() == 1?badRequestPredicates.get(0):
                new BadRequestPredicateGroup(badRequestPredicates);
    }



    private List<BadRequestPredicate> getPredicatesByClass(Class<?> clazz) {
        if(inheritLyGet){
            List<BadRequestPredicate> list = new ArrayList<>();
            for(Map.Entry<Class<?>,List<BadRequestPredicate>> entry : predicatesMapping.entrySet()) {
                if(clazz.equals(entry.getKey()) || entry.getKey().isAssignableFrom(clazz)) {
                    list.addAll(entry.getValue());
                }
            }
            return list;
        }
        return this.predicatesMapping.get(clazz);
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

    public BadRequestProperties getProperties() {
        return properties;
    }

    public boolean isLocked() {
        return locked.get();
    }

    public List<BadRequestPredicate> getPredicates() {
        return predicates;
    }

    public boolean isInheritLyGet() {
        return inheritLyGet;
    }

    public Map<Class<?>, List<BadRequestPredicate>> getPredicatesMapping() {
        return predicatesMapping;
    }
}
