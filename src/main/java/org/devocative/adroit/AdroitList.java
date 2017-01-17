package org.devocative.adroit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class AdroitList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 9197755601834885021L;

	private Comparator<E> comparator;

	// ------------------------------

	public AdroitList(int initialCapacity) {
		super(initialCapacity);
	}

	public AdroitList() {
	}

	public AdroitList(Collection<E> c) {
		super(c);
	}

	public AdroitList(Comparator<E> comparator) {
		this.comparator = comparator;
	}

	// ------------------------------

	public AdroitList<E> setComparator(Comparator<E> comparator) {
		this.comparator = comparator;
		return this;
	}

	public AdroitList<E> addIt(E it) {
		add(it);
		return this;
	}

	// ------------------------------

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size(); i++) {
				if (get(i) == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < size(); i++) {
				if (checkEquality(get(i), o)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size() - 1; i >= 0; i--) {
				if (get(i) == null) {
					return i;
				}
			}
		} else {
			for (int i = size() - 1; i >= 0; i--) {
				if (checkEquality(get(i), o)) {
					return i;
				}
			}
		}
		return -1;
	}

	// ------------------------------

	private boolean checkEquality(E it, Object o) {
		return o.equals(it) || (comparator != null && comparator.compare(it, (E) o) == 0);
	}
}