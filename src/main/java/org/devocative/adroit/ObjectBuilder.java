package org.devocative.adroit;

import java.util.Collection;
import java.util.Map;

public class ObjectBuilder {

	public static <K, V> MapBuilder<K, V> createMap(Map<K, V> instance) {
		return new MapBuilder<>(instance);
	}

	public static <T> CollectionBuilder<T> createCollection(Collection<T> instance) {
		return new CollectionBuilder<>(instance);
	}

	// --------------- MapBuilder Class

	public static class MapBuilder<K, V> {
		private Map<K, V> map;

		public MapBuilder(Map<K, V> map) {
			this.map = map;
		}

		public MapBuilder<K, V> put(K key, V value) {
			map.put(key, value);
			return this;
		}

		public Map<K, V> get() {
			return map;
		}
	}

	// --------------- CollectionBuilder Class

	public static class CollectionBuilder<T> {
		private Collection<T> collection;

		public CollectionBuilder(Collection<T> collection) {
			this.collection = collection;
		}

		public CollectionBuilder<T> add(T item) {
			collection.add(item);
			return this;
		}

		public Collection<T> get() {
			return collection;
		}
	}

}
