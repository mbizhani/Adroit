package org.devocative.adroit.cache;

public interface IMissedHitHandler<K, V> {
	V load(K key);
}
