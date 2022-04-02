# siamese bri

## 1.基本介绍

siamese bri是一个基于SpringAOP实现的细粒度的服务降级组件。可以针对某个请求接口进行服务降级，也可以针对同一接口的不同参数进行不同处理。对部分参数进行放行，或对部分参数进行降级处理。针对不同接口可以设置不同恢复服务的时间。

应用场景:

假如存在一个接口:

```java
public String purchaseGood(Integer goodId)
```

请求参数传入商品编号，接口内部经过一系列查询处理最终返回商品卖光，或者在商品还存在的情况下为用户进行购买商品的处理。

但是假如3号商品已经卖光了，且短时间内不会补货，那么短时间内针对3号商品的请求进行的查询都将是无意义的，那么我们可以记录已经卖光的商品编号，对于接下来针对该商品的购买请求，返回预设好的信息即可。**同时，这种处理不能影响其他商品的购买**。这种处理就是siamese bri的主要功能



## 2.六大组件

### 2.1 注解

#### 2.1.1 @BadRequestInterceptor

这个注解用于标注在目标方法上

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BadRequestInterceptor {
	//配置用于服务降级的默认方法，类全路径+"."+方法名
    String fallback() default "";
	//当无效请求达到多少次后对目标方法进行降级
    int tolerance();
	//如果目标方法返回值为String,则可以设置一个默认的返回信息
    String defaultMessage() default "";
	//当目标方法抛出那些异常时记录该次请求为一次无效请求
    Class<? extends Exception>[] targetException() default {BadRequestException.class};
	//记录的窗口时间，计数器会在这个时间过后清除
    long expireTime() default 1000*60*60*24L;

}
```



需要注意的点:

1.fallback:配置的fallback方法所在的类必须由Spring容器管理或者存在一个无参构造器。且fallback方法和目标方法的参数列表必须一致。

2.defaultMessage:如果目标方法返回值不为String,则该配置无效









#### 2.1.2 @BadRequestParam

这个注解用于标注在参数列表上

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BadRequestParam {
  	//allOf 为true 是说明以该参数对象整体生成请求签名
    boolean allOf() default false;
}
```

需要注意的点:

**如果一个接口上标注了 @BadRequestInterceptor 但是参数列表上没有标注 @BadRequestParam，则当该接口发生降级处理时，对任意参数的请求都进行降级处理。**













#### 2.1.3 @BadRequestProperty

该注解用于标注在类的属性上，且该类必须作为参数被 @BadRequestParam 标注。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BadRequestProperty {
}
```

需要注意的点:

1.如果一个参数的类的部分属性上被标注了 @BadRequestProperty 注解，则只通过这部分属性为请求生成签名。

2.如果一个类的属性上标注了 @BadRequestProperty,但是该类没有作为参数被标注 @BadRequestParam,那么这个注解标注不会生效。









### 2.2 Generator

BadRequestStorageKeyGenerator 类会对每一个请求生成一个签名，相同签名的请求会一起被放行或被一起被降级处理。

```java
public interface BadRequestStorageKeyGenerator {
    StorageKey getStorageKey(Method var1, Object... var2) throws IllegalAccessException;
}
```



请求签名分为以下四种：

1.如果只标注了 @BadRequestInterceptor ,则会通过 **目标方法和目标方法的参数列表类型** 生成请求签名。

2.如果标注了 @BadRequestInterceptor 和 @BadRequestParam ,则会通过 **目标方法和目标方法的参数列表** **和标注注解的参数** 生成请求签名。

3.如果标注了 @BadRequestInterceptor 、@BadRequestParam 和 @BadRequestProperty,则会通过 **目标方法和目标方法的参数列表和标注注解的参数的指定属性** 生成请求签名。

4.如果标注了 @BadRequestInterceptor 、@BadRequestParam并使用了 BadRequestIdentifier 接口，则会通过 **目标方法和目标方法的参数列表** 生成方法部分的请求签名，**通过自定义规则生成参数部分的请求签名**。



#### 2.2.1 DefaultBadRequestStorageKeyGenerator

DefaultBadRequestStorageKeyGenerator 是 BadRequestStorageKeyGenerator 的默认实现类，它实现了 getStorageKey 方法。其中方法部分的签名生成由 DefaultBadRequestStorageKeyGenerator 本身来实现，参数部分的签名生成由 内部成员 BadRequestParamGenerator 实现。





#### 2.2.2 BadRequestParamGenerator

BadRequestParamGenerator 是 DefaultBadRequestStorageKeyGenerator 内部的一个成员属性，负责通过参数生成请求签名。

```java
public interface BadRequestParamGenerator {
  	//该方法通过参数属性列表生成方法签名
    String doGenerate(BadRequestParamWrapper... var1) throws IllegalAccessException;
	//该方法是通过一个参数生成签名的逻辑
    Object getParamGeneration(Object var1);
}
```

```java
public class DefaultBadRequestStorageKeyGenerator implements BadRequestStorageKeyGenerator {
    private BadRequestParamGenerator paramGenerator;
```



AbstractBadRequestParamGenerator 是 BadRequestParamGenerator 的抽象子类，它对 doGenerate 和 getParamGeneration 方法有默认的实现，但是将非包装类引用类型对象转换为签名的逻辑是抽象方法 doGenerateByParam ，由子类实现。

```java
public abstract class AbstractBadRequestParamGenerator implements BadRequestParamGenerator {
    public AbstractBadRequestParamGenerator() {
    }
  	public String doGenerate(BadRequestParamWrapper... params) throws IllegalAccessException {
```



AbstractBadRequestParamGenerator  类还有两个子类，HashBadRequestParamGenerator 和 StringifyBadRequestParamGenerator。

```java
public class HashBadRequestParamGenerator extends AbstractBadRequestParamGenerator {
    public HashBadRequestParamGenerator() {
    }

    public Object doGenerateByParam(Object param) {
        return Objects.hash(new Object[]{param});
    }
}
```

```java
public class StringifyBadRequestParamGenerator extends AbstractBadRequestParamGenerator {
    public StringifyBadRequestParamGenerator() {
    }

    public Object doGenerateByParam(Object param) {
        return Objects.toString(param);
    }
}
```









### 2.3 Predicate

BadRequestPredicate 表示一个断言，它决定了一个什么样的请求被认为是无效请求(BadRequest)，它是 BadRequestDecidable 的子接口。

```java
public interface BadRequestDecidable<T> {
  	//入参是请求返回值 返回本次请求是否为无效请求
    boolean isBadRequest(T var1);
}
```

```java
public interface BadRequestPredicate<T> extends BadRequestDecidable<T> {
  	//表示接口返回值类型
    Class<T> getTargetClass();
}
```

如果想新增一个断言,实现 BadRequestPredicate 接口，并**将实现类注入容器**即可：

```java
//该断言只能用于判断 返回值类型为 CommonResult 的请求
@Component
public class MyBadRequestPredicate implements BadRequestPredicate<CommonResult> {
    @Override
    public Class<CommonResult> getTargetClass() {
        return CommonResult.class;
    }
  
  	//当返回码小于0 就被认为是BadRequest
    @Override
    public boolean isBadRequest(CommonResult commonResult) {
        return commonResult.getCode() <0 ;
    }
}
```



**可以同时存在多个 targetClass 相同的断言；不同断言的 targetClass 之间也允许存在继承与被继承的关系，对于这样的断言，可以选择不同的策略处理它们。**



### 2.4 PredicateFactory

BadRequestPredicateFactory 是 一个接口，同时它是断言的工厂类：

```java
public interface BadRequestPredicateFactory {
  	//通过该方法可以动态的注册一个断言
    void registerPredicate(BadRequestPredicate var1);
	
  	//该方法用于锁定工厂类
    int lock();

  	//该方法可以通过工厂类获取处理指定类型的一个或一组断言
    BadRequestDecidable getBadRequestDecider(Class<?> var1);
}
```



#### 2.4.1 DefaultBadRequestPredicateFactory

DefaultBadRequestPredicateFactory 是 BadRequestPredicateFactory 的默认实现，它对接口方法进行了默认实现。

DefaultBadRequestPredicateFactory  内部维护了一个Map，key为返回值类型，value为处理这种类型的断言集合。

```java
public class DefaultBadRequestPredicateFactory implements BadRequestPredicateFactory {
    private List<BadRequestPredicate> predicates;
    private Map<Class<?>, List<BadRequestPredicate>> predicatesMapping;
    private BadRequestProperties properties;
    private boolean inheritLyGet;
    private AtomicBoolean locked = new AtomicBoolean(false);
```



从 BadRequestPredicateFactory 中获取断言的方法:

```java
public BadRequestDecidable getBadRequestDecider(Class<?> clazz) {
    List<BadRequestPredicate> badRequestPredicates = this.getPredicatesByClass(clazz);
    if (badRequestPredicates != null && !badRequestPredicates.isEmpty()) {
        return (BadRequestDecidable)(badRequestPredicates.size() == 1 ? (BadRequestDecidable)badRequestPredicates.get(0) : new BadRequestPredicateGroup(badRequestPredicates));
    } else {
        return NonBadRequestDecider.getDecider();
    }
}
```

其中 BadRequestPredicate、BadRequestPredicateGroup、NonBadRequestDecider 都是 BadRequestDecidable 的实现类。





#### 2.4.2 BadRequestPredicateFactoryCustomizer

BadRequestPredicateFactoryCustomizer 是一个定制化接口，通过实现该接口，可以在 BadRequestPredicateFactory 放入Spring容器前对 断言工厂 进行一些操作。比如向断言工厂中注册断言等。

```java
public interface BadRequestPredicateFactoryCustomizer {
    void customize(BadRequestPredicateFactory var1);
}
```

通过 BadRequestPredicateFactoryCustomizer 接口注册断言的方法：

```java
@Component
public class MyFactoryCustomizer implements BadRequestPredicateFactoryCustomizer {
    @Override
    public void customize(BadRequestPredicateFactory badRequestPredicateFactory) {
        badRequestPredicateFactory.registerPredicate(new BadRequestPredicate<CommonResult>() {
            @Override
            public Class<CommonResult> getTargetClass() {
                return CommonResult.class;
            }

            @Override
            public boolean isBadRequest(CommonResult o) {
                return o.getCode()<0;
            }
        });
    }
}
```



需要注意的点：

在 BadRequestPredicateFactory 注入容器之前会被默认调用 lock 方法，之后就不允许通过 BadRequestPredicateFactory  类动态向 断言工厂中注册断言了，如果需要实现项目运行过程中动态注册断言的功能，可以继承 DefaultBadRequestPredicateFactory 类后重写 lock 方法，然后将自定义的工厂类注入即可。



动态注册断言报错：

```java
@Autowired
private BadRequestPredicateFactory predicateFactory;

@RequestMapping("/test")
public void test(){
    predicateFactory.registerPredicate(new BadRequestPredicate<String>() {
        @Override
        public Class<String> getTargetClass() {
            return String.class;
        }

        @Override
        public boolean isBadRequest(String o) {
            return "Hello World".equals(o);
        }
    });
}
```

```java
java.lang.UnsupportedOperationException: null
```






### 2.5 CacheMapping

CacheMapping意为缓存映射，在siamese bri中，BadRequestCacheMapping 是一个父接口： bri

```java
public interface BadRequestCacheMapping<T> extends InitializingBean {
  	//从缓存中获取数据
    T get(BadRequestInterceptor var1, Class<?>[] var2) throws NoSuchMethodException, ClassNotFoundException;
	//锁定缓存池
    void lock(Map<String, T> var1);
	//创建缓存池
    void doBuild() throws NoSuchMethodException, ClassNotFoundException;
	//在缓存池加入Spring容器初始化完成后会自动构建缓存池
    default void afterPropertiesSet() throws Exception {
        this.doBuild();
    }
}
```





#### 2.5.1 FallbackMethodCacheMapping

FallbackMethodCacheMapping 是 BadRequestCacheMapping 的抽象子类，它内部持有所有 fallback 的方法引用，避免在项目启动后反复通过反射解析 fallback 获取目标方法。

```java
public abstract class FallbackMethodCacheMapping implements BadRequestCacheMapping<InterceptorMetadata> {
    private Map<String, InterceptorMetadata> fallbackMethodMapping;
    private AtomicBoolean init = new AtomicBoolean(false);
    private AtomicBoolean locked = new AtomicBoolean(false);
    protected final Object LOCK = new Object();
    private TargetMethodCollector collector;
```

FallbackMethodCacheMapping  内部维护了一个Map,key为fallback方法的签名，由fallback和目标方法的参数列表共同生成；value是一个 拦截器元数据对象 InterceptorMetadata 。

InterceptorMetadata ：

```java
public class InterceptorMetadata {
    private Method fallBackMethod;
    private int tolerance;
    private String defaultMessage;
    private Class<? extends Exception>[] interceptFor;
    private Class<?>[] parameterTypes;
    private long expireTime;
```



FallbackMethodCacheMapping 实现了接口的 dobuild 方法，但是将缓存池的初始化逻辑设置为抽象方法由子类实现。

```java
public void doBuild() throws NoSuchMethodException, ClassNotFoundException {
    if (!this.init.get()) {
        synchronized(this.LOCK) {
            if (!this.init.get()) {
                List<Method> targetMethod = this.collector.getTargetMethod();
                this.fallbackMethodMapping = this.initMapping(targetMethod);
                this.lock(this.fallbackMethodMapping);
                this.init.set(true);
                return;
            }
        }
    }

    throw new UnsupportedOperationException("cache mapping has been initialized!");
}
```

```java
abstract Map<String, InterceptorMetadata> initMapping(List<Method> var1);
```







#### 2.5.2 FallbackMethodDefaultCacheMapping

FallbackMethodDefaultCacheMapping 是 FallbackMethodCacheMapping  的一个子类。

该类会在项目启动时通过 initMapping 方法直接对缓存池进行初始化构建，并对缓存池进行 lock 加锁处理。因此在运行期，对缓存池只存在查询的操作,不存在并发修改问题 。 因此，在 FallbackMethodDefaultCacheMapping  中对缓存池是通过 HashMap 实现的。

```java
public Map<String, InterceptorMetadata> initMapping(List<Method> targetMethods) throws NoSuchMethodException, ClassNotFoundException {
    if (targetMethods != null && !targetMethods.isEmpty()) {
        Map<String, InterceptorMetadata> tempMapping = new HashMap();
			
			...
			...

            return tempMapping;
        }
    } else {
        return new HashMap(0);
    }
}
```





#### 2.5.3 FallbackMethodLazyCacheMapping

FallbackMethodLazyCacheMapping 是 FallbackMethodCacheMapping  的另一个子类

该类对缓存池采用的是懒加载处理，即在运行期，如果一个 fallback 是第一次访问，则会通过反射解析这个 方法并获取 方法引用，然后将这个方法引用放入缓存池。后续对这个 fallback 的访问则会直接通过缓存池获取。因此该类在缓存池初始化时不会对缓存池内部数据进行填充。同时因为在运行期存在并发修改的操作，所以对于缓存池采用的是 ConcurrentHashMap 的实现。该类重写了 lock 方法，在缓存池初始化完成之后不会锁定缓存池。

```java
public Map<String, InterceptorMetadata> initMapping(List<Method> targetMethods) {
    return new ConcurrentHashMap(256);
}
```









### 2.6 Handler

BadRequestHandler 在siamese bri中 是一个接口，规定了一个无效请求处理器应该实现哪些方法。 

```java
public interface BadRequestHandler {
  	//判断目标方法是否需要拦截
    boolean needIntercept(ProceedingJoinPoint var1, int var2) throws IllegalAccessException;
	//后置处理
    Object handleAfter(ProceedingJoinPoint var1);
	//记录一次无效请求
    Object record(ProceedingJoinPoint var1, long var2) throws IllegalAccessException;
	//缓存刷新
    Object flush();
	//通过目标方法获取请求签名
    StorageKey getStorageKey(ProceedingJoinPoint var1) throws IllegalAccessException;
}
```





#### 2.6.1 AbstractBadRequestHandler

AbstractBadRequestHandler 是 BadRequestHandler 的一个抽象子类。

AbstractBadRequestHandler 对 getStorageKey 有默认实现，内部维护了一个 ThreadLocal 用于保存 请求签名。避免请求签名的多次生成。

同时 AbstractBadRequestHandler 对接口大部分方法都进行了封装，实现了部分逻辑，并将核心逻辑通过抽象方法的方式交给子类实现。

在 

```java
Object handleAfter(ProceedingJoinPoint var1);
```

后置处理中，AbstractBadRequestHandler 会清空本地 ThreadLocal 的内部数据，并且将其他后置处理封装成

```java
public Object handleAfter(ProceedingJoinPoint point) {
    this.localStorageKey.remove();
    return this.postHandle(point);
}
```

```java
protected abstract Object postHandle(ProceedingJoinPoint var1);
```



可以通过继承 AbstractBadRequestHandler 类实现内部方法，实现自定义的处理器。比如可以基于数据库实现一个处理器等。在 siamese bri内部默认实现了基于redis实现的 RedisBadRequestHandler 和 基于本地内存实现的 DefaultBadRequestHandler。





#### 2.6.2 RedisBadRequestHandler

RedisBadRequestHandler 是 AbstractBadRequestHandler 的子类。其核心逻辑都是通过 redis 来实现的。因此使用该类的处理器可以支持对分布式方法的访问拦截。

通过 postHandle 方法默认是空实现的，如果需要其他请求拦截后续处理，可以通过继承该类重写 postHandle 方法的方式进行扩展。





#### 2.6.3 DefaultBadRequestHandler

DefaultBadRequestHandler 是 AbstractBadRequestHandler  另一个实现子类。其核心逻辑都是通过本地内存实现的，因此如果存在多个节点，它们的错误次数将无法共享。DefaultBadRequestHandler 的核心逻辑都封装在 MemoryInterceptorCache 类中，该类内部维护了一个Map,会记录每一个请求签名对应的访问错误次数。通过会启动一个线程异步的对这个Map进行检查，清除其中的过期数据。







## 3.如何使用

对于使用 siamese bri，需要解决三个问题：

1.什么样的请求算无效请求

2.对于两次不同的访问，如果判断他们为同一类无效请求

3.如何处理无效请求





### 3.1 无效请求

在 siamese bri中 BadRequest 意味无效请求，就像400 BadRequest 一样，可能表示本次请求的参数异常。

对于上面的第一个问题：什么样的请求算无效请求。

在 siamese bri中是通过一次请求的返回值 来判断一次请求是否有效的。这种判断完全是自定义的。比如返回的错误码为某一个值时认为该次请求无效，或者没有返回某个字段时任务请求无效等。

对于请求是否有效的判断通过 BadRequestPredicate 来实现，可以通过 实现 BadRequestPredicate 接口 或者 通过断言工厂注册断言的方法 添加自定义断言。

断言的注册方式可以参考 六大组件 - Predicate







### 3.2 请求签名

请求签名其实就是能够区别是否为同一类请求的标识，只有请求签名相同，才会被认为是同一类请求。比如一开始举的例子：

1.

```java
public String purchaseGood(Integer goodId)
```

对于一个通过商品编号购买商品的接口来说，商品编号就是这个接口请求的请求标识，即使是两个不同的用户，如果他们要购买同一个商品，即商品编号相同时，就可以认为这是同一类请求。



2.实际情况可能会更加复杂，比如存在多个商品仓库：

```java
class PurchaseParam {
  	//仓库编号
    Integer houseId;
	//商品编号
    Integer goodId;
}
```

用户发起购买申请时，会将仓库编号和商品编号一起传给接口：

```java
@RequestMapping("/buy")
public String buy(PurchaseParam purchaseParam){
    return doSomething();
}
```

那么对于这种情况，我们认为只有仓库编号和商品编号都重复时，才认为这是一次无效的请求。



3.或者用户购买时会将购买单号和商品编号传给接口:

```java
class PurchaseParam {
  	//请求流水号
    Long requestNo;
	//商品编号
    Integer goodId;
}
```

对于这种请求来说，请求流水号大部分情况都不相同，因此不能用来判断是否为同一类请求的依据。这种情况下我们只认为 PurchaseParam.goodId 相同的请求为同一类请求。





对于上面的第一种情况，通过参数列表中的部分参数生成请求签名的情况：

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyOne(@BadRequestParam Integer goodId, String userName){
    return doSomething();
}
```

只需要在 goodId 参数上标注 @BadRequestParam 注解即可。由于 userName 不是判断是否同一类请求的条件，因此这个参数上不需要标注注解。







对于上面的第二种问题，请求参数是一个非包装类引用类型：

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyTwo(@BadRequestParam PurchaseParam purchaseParam){
    return doSomething();
}
```

首先需要在 PurchaseParam 上标注 @BadRequestParam 注解，说明这个参数是生成请求签名的依据。由于 PurchaseParam 的两个属性都是生成请求签名的条件，因此需要在两个属性上都标注 @BadRequestProperty 注解：

```java
class PurchaseParam {
    @BadRequestProperty
    Integer houseId;
    
    @BadRequestProperty
    Integer goodId;
}

```



PurchaseParam 只有两个属性，但是如果实体类存在多个属性，一个个属性标注注解繁琐且容易漏标，如果一个实体类的所有属性都是生成请求参数的条件的话，可以使用 BadRequestParam 的 allOf 属性：

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyTwo(@BadRequestParam(allOf = true) PurchaseParam purchaseParam){
    return doSomething();
}
```

这样就表明这个参数的所有内部属性都参与请求签名的生成。





对于第三种情况，请求参数内部只有一部分属性用于请求签名的生成，只需要在这部分属性上标注 @BadRequestProperty 注解即可：

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyThree(@BadRequestParam PurchaseParam purchaseParam){
    return doSomething();
}
```

```java
class PurchaseParam {
    Integer requestNo;

    @BadRequestProperty
    Integer goodId;
}
```







#### 3.2.1 复杂请求签名的处理

有时一类请求是否相同的判断逻辑可能会非常复杂，比如：

请求单号为偶数且购买商品编号相同的请求为同一类请求 ...



对于这类请求签名的生成，可以使用 BadRequestIdentifier 接口来处理：

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyFour(@BadRequestParam PurchaseParam purchaseParam){
    return doSomething();
}
```

```java
class PurchaseParam implements BadRequestIdentifier {
    Integer houseId;

    Integer goodId;

    @Override
    public String getIdentifier() {
        return (houseId % 2) + goodId.toString();
    }
}
```

通过实现 BadRequestIdentifier 接口重写 getIdentifier 方法，可以自定义请求签名中参数签名的生成规则。







还有一类请求签名，可能它的请求参数中有20个属性，但其中19个属性都参与了请求签名的生成，但有一个参数不参与生成请求签名，对于这类请求签名生成方式，有三种解决办法：

1.在19个属性上标注 @BadRequestProperty。  【不推荐】

2.使用 @BadRequestParam(allOf = true)，同时通过这个参数对象的19个属性重写 toString()或是 hashCode()方法。

【不推荐，有时toString()和hashCode()方法具有其他业务意义】

3.实现 BadRequestIdentifier 接口重写 getIdentifier 方法，通过19个参数自定义请求签名的生成规则。【推荐】



需要注意的点:

**通过参数生成请求参数签名的优先级:**

**BadRequestIdentifier 接口的 getIdentifier 方法  > @BadRequestParam(allOf = true) > @BadRequestProperty**







### 3.3 如何处理无效请求

siamese bri中采用两种方式处理无效请求，defaultMessage 和 fallback ：



#### 3.3.1 defaultMessage

defaultMessage 只有在原方法返回值为String时生效，且 defaultMessage 只支持配置字符串字面值。

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,defaultMessage = "Sold Out!")
public String buyOne(@BadRequestParam Integer goodId, String userName){
    return doSomething();
} 
```

如这样的配置就会在请求被拦截时 返回  "Sold Out!"。





#### 3.3.2 fallback

```java
@RequestMapping("/buy")
@BadRequestInterceptor(tolerance = 5,fallback = "com.example.demo.controller.TestController2.fallback")
public String buyOne(@BadRequestParam Integer goodId, String userName){
    return doSomething();
}


public String fallback(Integer goodId, String userName) {
    return String.format("尊敬的用户: %s,非常抱歉,您所购买的商品,商品编号:%d 已经售罄！",userName,goodId);
}
```

使用 fallback 的注意事项:

1.fallback 方法所在的类必须在Spring容器中，或者存在无参构造器。

2.fallback 方法的参数列表必须和原参数列表一致。

3.fallback 方法的返回值必须和原方法保持一致。

4.fallback 方法的访问权限必须是 public。









## 4.配置文件

siamese bri的所有配置都存在默认值，因此如果没有特殊需要，可以无视 siamese bri的配置直接使用它。

siamese bri支持的配置有如下这些：

```yaml
bri:
  predicate-mode: inherit
  bad-request-namespace: DEFAULT_1_
  method-mapping-cache-lazily: true
  key-gene-policy: hash
  flush-wait-time: 20000
  reset-expire-time-on-bad-request: true
  check-interval-on-memory-cache: 2000
```







### 4.1 释义

| 配置名                              | 配置含义                                     | 可选值            | 默认值         |
| -------------------------------- | ---------------------------------------- | -------------- | ----------- |
| predicate-mode                   | 断言匹配模式，inherit模式下，一个断言可以判断它泛型类型的所有子类返回值请求。strict模式下严格匹配类型。 | inherit/strict | strict      |
| bad-request-namespace            | redis的命名空间。                              |                | BRI_前缀+UUID |
| method-mapping-cache-lazily      | fallback 缓存池是否懒加载                        | true/false     | false       |
| key-gene-policy                  | 通过参数生成请求签名的策略，string表示调用目标类的toString()方法生成，hash表示调用目标类的hashCode()方法生成 | string/hash    | string      |
| flush-wait-time                  | 调用Handler的flush方法时最大阻塞时间，单位毫秒            |                | 10000L      |
| reset-expire-time-on-bad-request | 已经被拦截的方法再次被拦截时，是否重置窗口时间(窗口时间即为恢复服务降级的等待时间) | true/false     | false       |
| check-interval-on-memory-cache   | 基于内存处理无效请求时，每隔多久对内存中记录的无效请求记录次数进行检查并删除已经过期的数据，单位毫秒（仅当使用DefaultBadRequestHandler时该配置生效） |                | 10000L      |





### 4.2 配置选择

predicate-mode： 推荐 strict 模式，strict模式获取断言的效率更高。同时不建议断言的targetClass之间存在继承关系。

method-mapping-cache-lazily：非懒加载状态下，缓存池采用HashMap实现，运行期读取效率更高，但是在加载阶段会阻塞读取。懒加载状态下缓存池采用ConcurrentHashMap实现，运行期可能存在读取阻塞的情况。如果fallback方法较少建议使用非懒加载，如果fallback方法数量很多的情况建议使用懒加载方式。

key-gene-policy：默认 string 模式，如果实体类本身不需要使用hash值进行其他业务处理(比如去重)，则推荐使用hash模式，可以减少请求签名的空间占用。但是使用@BadRequestParam(allOf = true) 时一定要重写toString/hashCode方法。

reset-expire-time-on-bad-request：该配置的选择和 @BadRequestInterceptor 的expireTime的设置方式有关，如果expireTime设置的周期较长，建议该配置设置为false。

