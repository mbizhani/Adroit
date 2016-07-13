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

	void setMissedHitHandler(IMissedHitHandler<K, V> missedHitHandler);

	void put(K key, V value);

	void update(K key, V value);

	V remove(K key);

	void clear();

	V get(K key);

	boolean containsKey(K key);

	V findByProperty(String property, Object value);
}
