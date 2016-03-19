package org.devocative.adroit.obuilder;

import java.util.*;

/**
 * A simple wrapper class around classes using method chaining to add/modify elements easily
 */
public class ObjectBuilder {

	/**
	 * Creates a MapBuilder around HashMap
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a MapBuilder object for method chaining
	 */
	public static <K, V> MapBuilder<K, V> createDefaultMap() {
		return createMap(new HashMap<K, V>());
	}

	/**
	 * Creates a MapBuilder around the passed instance
	 *
	 * @param instance
	 * @param <K>      the key type
	 * @param <V>      the value type
	 * @return a MapBuilder object for method chaining
	 */
	public static <K, V> MapBuilder<K, V> createMap(Map<K, V> instance) {
		return new MapBuilder<>(instance);
	}

	/**
	 * Creates a CollectionBuilder around ArrayList
	 *
	 * @param <T> type of list
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> createDefaultList() {
		return createCollection(new ArrayList<T>());
	}

	/**
	 * Creates a CollectionBuilder around HashSet
	 *
	 * @param <T> type of set
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> createDefaultSet() {
		return createCollection(new HashSet<T>());
	}

	/**
	 * Creates a CollectionBuilder around passed instance
	 * @param instance of a collection
	 * @param <T> type of collection
	 * @return a CollectionBuilder object for method chaining
	 */
	public static <T> CollectionBuilder<T> createCollection(Collection<T> instance) {
		return new CollectionBuilder<>(instance);
	}
}
