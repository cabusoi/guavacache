package sportbet.client.common.store;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.base.Function;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GuavaCacheBacked {

    Class keyClass=Long.class;
    Class valueClass=Object.class;
    int expirationSeconds=3600;
    int capacity=1000;
    SerializableFunction<Object, Object> loader=null;
}
