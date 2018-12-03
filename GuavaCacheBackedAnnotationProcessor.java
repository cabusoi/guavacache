package sportbet.client.common.store;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import sportbet.common.program.EventValue;

@Component
public class GuavaCacheBackedAnnotationProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            postProcessBeforeInitializationGuavaCacheBackedAnnotation( bean,  beanName);
        } catch (IllegalArgumentException e) {
            throw new BeanInitializationException(beanName, e) ;
        } catch (IllegalAccessException e) {
            throw new BeanInitializationException(beanName, e) ;
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void postProcessBeforeInitializationGuavaCacheBackedAnnotation(Object bean, String beanName) throws IllegalArgumentException, IllegalAccessException {
        for (Field f: bean.getClass().getDeclaredFields()){
            postProcessBeforeInitializationGuavaCacheBackedAnnotationOnField(bean, f);
        }
    }

    protected void postProcessBeforeInitializationGuavaCacheBackedAnnotationOnField(Object bean, Field f)
            throws IllegalAccessException {
        if (!f.isAnnotationPresent(GuavaCacheBacked.class)
                ||f.get(bean) instanceof GuavaCacheBackedMap){
            return;
        }
        if (! (f.get(bean) instanceof Map)){
            throw new IllegalArgumentException(String.format("Field %s is not a type of Map in %s", f.getName(), bean));
        }
        Annotation annotation = f.getAnnotation(GuavaCacheBacked.class);
        GuavaCacheBacked cacheConfig=(GuavaCacheBacked)annotation;
        Cache<Object, Object> cache =CacheBuilder.newBuilder()
                .expireAfterWrite(cacheConfig.expirationSeconds, TimeUnit.SECONDS)
                .maximumSize(cacheConfig.capacity)
                .build();
        SerializableFunction<Object, Object> loader=cacheConfig.loader;
        Map< Object, Object> map = new GuavaCacheBackedMap< Object, Object> (cache,loader);
        f.setAccessible(true);
        f.set(bean, map);
    }

}
