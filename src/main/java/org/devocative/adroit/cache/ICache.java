package org.devocative.adroit.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ICache<K, V> {
	LRUCache setMissedHitHandler(IMissedHitHandler<K, V> missedHitHandler);

	void put(K key, V value);

	void update(K key, V value);

	V remove(K key);

	V get(K key);

	int size();

	boolean containsKey(K key);

	Set<K> keys();

	Collection<V> values();

	Set<Map.Entry<K, V>> entries();

	V findByProperty(String property, Object value);

	void clear();
}
