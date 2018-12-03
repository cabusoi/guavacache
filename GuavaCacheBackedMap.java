package sportbet.client.common.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

import lombok.SneakyThrows;

public class GuavaCacheBackedMap<K,V> implements Map<K,V>, Serializable{
    
    private static final long serialVersionUID = 1L;
    Cache<K,V> cache;
    SerializableFunction<K, V> valueLoader;
            
    GuavaCacheBackedMap(Cache<K,V> cache, SerializableFunction<K, V> valueLoader){
        this.cache=cache;
        this.valueLoader=valueLoader;
    }
    
    @Override
    public int size() {
        return (int) cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.size()==0;
    }

    @Override
    public boolean containsKey(Object key) {
        return cache.getIfPresent(key)!=null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new RuntimeException("not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public V get(final Object key) {
        return cache.get((K)key, new Callable<V>() {

            @Override
            public V call() throws Exception {
                return valueLoader.apply((K)key);
            }
        });
    }

    @Override
    public V put(K key, V value) {
        cache. put(key, value);
        return value;
    }

    @Override
    public V remove(Object key) {
        V result = cache.getIfPresent(key);
        cache.invalidate(key);
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public Set<K> keySet() {
        return cache.asMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return cache.asMap().values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return cache.asMap().entrySet();
    }
    
    public CacheStats getCacheStats(){
        return cache.stats();
    }
    
}
