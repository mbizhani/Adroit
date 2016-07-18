package org.devocative.adroit.cache;

import org.devocative.adroit.ObjectUtil;

import java.util.*;

public class LRUCache<K, V> implements ICache<K, V> {
	private int capacity;
	private long missHitCount;
	private final Map<K, V> map;

	private IMissedHitHandler<K, V> missedHitHandler;

	// ------------------------------

	public LRUCache(int capacity) {
		this.capacity = capacity;

		map = Collections.synchronizedMap(new CacheMap());
	}

	// ------------------------------

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public long getMissHitCount() {
		return missHitCount;
	}

	@Override
	public int getSize() {
		return map.size();
	}

	@Override
	public Set<K> getKeys() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<V> getValues() {
		return Collections.unmodifiableCollection(map.values());
	}

	@Override
	public Set<Map.Entry<K, V>> getEntries() {
		return Collections.unmodifiableSet(map.entrySet());
	}

	@Override
	public void setMissedHitHandler(IMissedHitHandler<K, V> missedHitHandler) {
		this.missedHitHandler = missedHitHandler;
	}

	// ------------------------------

	@Override
	public void put(K key, V value) {
		if (!map.containsKey(key) && map.size() >= capacity) {
			missHitCount++;
		}
		map.put(key, value);
	}

	@Override
	public synchronized void update(K key, V value) {
		if (map.containsKey(key)) {
			map.put(key, value);
		}
	}

	@Override
	public V remove(K key) {
		return map.remove(key);
	}

	@Override
	public void clear() {
		map.clear();
		missHitCount = 0;
	}

	// ---------------

	@Override
	public synchronized V get(K key) {
		callMissedHitHandler(key);
		return map.get(key);
	}

	@Override
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsKeyOrFetch(K key) {
		callMissedHitHandler(key);
		return map.containsKey(key);
	}

	@Override
	public V findByProperty(String property, Object value) {
		for (V v : map.values()) {
			Object propertyValue = ObjectUtil.getPropertyValue(v, property, true);
			if (propertyValue != null && propertyValue.equals(value)) {
				return v;
			}
		}
		missHitCount++;
		return null;
	}

	// ------------------------------

	private void callMissedHitHandler(K key) {
		if (missedHitHandler != null && !map.containsKey(key)) {
			V v = missedHitHandler.loadForCache(key);
			if (v != null) {
				put(key, v);
			}
		}
	}

	// ------------------------------

	private class CacheMap extends LinkedHashMap<K, V> {
		public CacheMap() {
			super(capacity, 0.75f, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > capacity;
		}
	}
}
