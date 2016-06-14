package org.devocative.adroit.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
	private int cacheSize;
	private Map<K, V> map;
	private IMissedHitHandler<K, V> missedHitHandler;

	// ------------------------------

	public LRUCache(int cacheSize) {
		this.cacheSize = cacheSize;

		map = Collections.synchronizedMap(new CacheMap());
	}

	// ------------------------------

	public LRUCache setMissedHitHandler(IMissedHitHandler<K, V> missedHitHandler) {
		this.missedHitHandler = missedHitHandler;
		return this;
	}

	// ------------------------------

	public synchronized void put(K key, V value) {
		map.put(key, value);
	}

	public synchronized void update(K key, V value) {
		if (map.containsKey(key)) {
			map.put(key, value);
		}
	}

	public synchronized V remove(K key) {
		return map.remove(key);
	}

	// ---------------

	public V get(K key) {
		if (missedHitHandler != null && !map.containsKey(key)) {
			map.put(key, missedHitHandler.loadForCache(key));
		}
		return map.get(key);
	}

	public int size() {
		return map.size();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
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
