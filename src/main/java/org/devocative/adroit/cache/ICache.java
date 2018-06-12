package org.devocative.adroit.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ICache<K, V> {
	int getCapacity();

	void setCapacity(int capacity);

	long getMissHitCount();

	int getSize();

	Set<K> getKeys();

	Collection<V> getValues();

	Set<Map.Entry<K, V>> getEntries();

	void put(K key, V value);

	V remove(K key);

	void clear();

	V get(K key);

	boolean containsKey(K key);

	boolean containsKeyOrFetch(K key);

	V findByProperty(String property, Object value);
}
