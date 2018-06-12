package org.devocative.adroit.cache;

@FunctionalInterface
public interface IMissedHitHandler<K, V> {
	V loadForCache(K key);
}
