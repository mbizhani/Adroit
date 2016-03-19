package org.devocative.adroit.obuilder;

import java.util.Collection;

class CollectionBuilder<T> {
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
