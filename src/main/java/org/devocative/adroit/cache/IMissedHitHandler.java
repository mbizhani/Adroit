package org.devocative.adroit.cache;

public interface IMissedHitHandler<K, V> {
	V loadForCache(K key);
}
