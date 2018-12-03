package sportbet.client.common.store;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;
import com.google.common.cache.Cache;

import sportbet.common.program.EventValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={EntityStoreManagerCachedAsyncTestConfiguration.class})
@PropertySource("classpath:till.test.properties")
public class EntityStoreManagerCachedAsyncTest {
    
    static final EventValue EVENT_1 = new EventValue();
    static final EventValue EVENT_2 = new EventValue();
    static final Long EVENT_PK1 = 101L;
    static final Long EVENT_PK2 = 102L;

    static final Long LONG_TIME = 1000L;
    static final Date BEGIN=Calendar.getInstance().getTime();
    
    @Value("${till.store.cache.expiration.seconds}")
    int EXPIRATION;

    @Value("${till.store.cache.size}")
    int MAX_SIZE;
    
    @Autowired
    ApplicationContext ctx;
    
    @Autowired
    EntityStore oldStore;

    @Autowired
    EntityStoreCachedAsync newStore;
    
    @Before
    public void setup(){
        assertNotNull(ctx);
        assertNotNull(oldStore);
        assertNotNull(newStore);
        
        assertThat(oldStore.eventPkMap, instanceOf(ConcurrentHashMap.class));
        assertThat(newStore.eventPkMap, instanceOf(GuavaCacheBackedMap.class));
    }
    
    @Test
    public void testAsynchronicityEntityStore() {
        
        //call async store and check op hasn't finished
        Future<Void> response=null;
        try{
            response=newStore.cleanUp();
            assertThat(response.isDone(), is(false));
        }finally{
            response.cancel(true);
        }
    }

    
    Function<Date, EventValue> cacheValueGenerator = new Function<Date, EventValue>() {
        
        @Override
        public EventValue apply(Date input) {
            EventValue ev = new EventValue();
            ev.setBegin(input);
            return ev;
        }
    };
    
    void loadCache(Cache<Long,EventValue> cache, int count, Date date){
        for (int i=0;i<count;){
            cache.put((long) ++i, cacheValueGenerator.apply(date));
        }
    } 
    
    @Test
    public void testBoundedExpiringCache() throws InterruptedException{
        Map<Long,EventValue> map=newStore.eventPkMap;
        testCache(map, ((GuavaCacheBackedMap<Long,EventValue>)map).cache);
    }

    private void testCache(Map<Long, EventValue> map, Cache<Long,EventValue> cache) throws InterruptedException {
        //overload cache
        loadCache(cache, MAX_SIZE, BEGIN);
        loadCache(cache, MAX_SIZE, BEGIN);
        
        cache.cleanUp();
        assertTrue(map.size()<=MAX_SIZE);

        //assert cache content
        for (EventValue v: map.values()){
            assertThat(v.getBegin(), is(BEGIN));
        }

        //allow cache to expire
        Thread.currentThread().sleep((long) ( EXPIRATION * 1000 * 1.1));
        cache.cleanUp();
        assertThat(map.size(), is(0));
        
        //load cache lightly
        final int LOAD =MAX_SIZE%3;
        loadCache(cache, LOAD, BEGIN);
        cache.cleanUp();
        assertThat(map.size(), is(LOAD));
    }

    @Test
    public void testSerialization() throws FileNotFoundException, IOException, ClassNotFoundException{
        final String FILE_NAME = "esac.bin";

        Map<Long,EventValue> map=newStore.eventPkMap;
        Cache<Long,EventValue> cache = ((GuavaCacheBackedMap<Long,EventValue>)map).cache;
        loadCache(cache, MAX_SIZE, BEGIN);

        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(FILE_NAME));
        o.writeObject(newStore);
        o.close();
        
        ObjectInputStream i = new ObjectInputStream(new FileInputStream(FILE_NAME));
        EntityStore readStore = (EntityStore) i.readObject();
        i.close();
        assertNotNull(readStore);
        assertTrue(readStore.eventPkMap instanceof GuavaCacheBackedMap<?, ?>);
    }
}
