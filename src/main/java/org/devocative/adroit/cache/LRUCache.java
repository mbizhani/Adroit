package org.devocative.adroit.cache;

import org.devocative.adroit.ObjectUtil;

import java.util.*;

public class LRUCache<K, V> implements ICache<K, V> {
	private final int cacheSize;
	private final Map<K, V> map;

	private IMissedHitHandler<K, V> missedHitHandler;

	// ------------------------------

	public LRUCache(int cacheSize) {
		this.cacheSize = cacheSize;

		map = Collections.synchronizedMap(new CacheMap());
	}

	// ------------------------------

	@Override
	public LRUCache setMissedHitHandler(IMissedHitHandler<K, V> missedHitHandler) {
		this.missedHitHandler = missedHitHandler;
		return this;
	}

	// ------------------------------

	@Override
	public void put(K key, V value) {
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

	// ---------------

	@Override
	public synchronized V get(K key) {
		if (missedHitHandler != null && !map.containsKey(key)) {
			map.put(key, missedHitHandler.loadForCache(key));
		}
		return map.get(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	@Override
	public Set<K> keys() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(map.values());
	}

	@Override
	public Set<Map.Entry<K, V>> entries() {
		return Collections.unmodifiableSet(map.entrySet());
	}

	@Override
	public V findByProperty(String property, Object value) {
		for (V v : map.values()) {
			Object propertyValue = ObjectUtil.getPropertyValue(v, property, true);
			if (propertyValue != null && propertyValue.equals(value)) {
				return v;
			}
		}
		return null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	// ------------------------------

	private class CacheMap extends LinkedHashMap<K, V> {
		public CacheMap() {
			super(cacheSize, 0.75f, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > cacheSize;
		}
	}
}
