package sportbet.client.common.store;

import java.io.Serializable;

import com.google.common.base.Function;

public interface SerializableFunction<K,V> extends Function<K,V>, Serializable{} 

