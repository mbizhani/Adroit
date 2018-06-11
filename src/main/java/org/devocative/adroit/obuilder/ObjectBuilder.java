package org.devocative.adroit.obuilder;

import java.util.*;

/**
 * A simple wrapper class around Collection using method chaining to add/modify elements easily
 */
public class ObjectBuilder {

	/**
	 * Creates a MapBuilder around Default
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a MapBuilder object for method chaining
	 */
	public static <K, V> MapBuilder<K, V> map() {
		return map(new LinkedHashMap<>());
	}

	/**
	 * Creates a MapBuilder around the passed instance
	 *
	 * @param instance
	 * @param <K>      the key type
	 * @param <V>      the value type
	 * @return a MapBuilder object for method chaining
	 */
	public static <K, V> MapBuilder<K, V> map(Map<K, V> instance) {
		return new MapBuilder<>(instance);
	}

	/**
	 * Creates a CollectionBuilder around ArrayList
	 *
	 * @param <T> type of list
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> list() {
		return list(new ArrayList<>());
	}

	public static <T> CollectionBuilder<T> list(List<T> list) {
		return collection(list);
	}

	/**
	 * Creates a CollectionBuilder around HashSet
	 *
	 * @param <T> type of set
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> set() {
		return set(new HashSet<>());
	}

	public static <T> CollectionBuilder<T> set(Set<T> set) {
		return collection(set);
	}

	/**
	 * Creates a CollectionBuilder around passed instance
	 *
	 * @param instance of a collection
	 * @param <T>      type of collection
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> collection(Collection<T> instance) {
		return new CollectionBuilder<>(instance);
	}
}
