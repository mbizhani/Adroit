package org.devocative.adroit.vo;

import java.io.Serializable;

public class RangeVO<T extends Serializable> implements IRange<T> {
	private static final long serialVersionUID = -1354461856855559223L;

	private T lower;
	private T upper;

	// ------------------------------

	public RangeVO() {
	}

	public RangeVO(T lower, T upper) {
		this.lower = lower;
		this.upper = upper;
	}

	// ------------------------------

	@Override
	public T getLower() {
		return lower;
	}

	@Override
	public RangeVO<T> setLower(T lower) {
		this.lower = lower;
		return this;
	}

	@Override
	public T getUpper() {
		return upper;
	}

	@Override
	public RangeVO<T> setUpper(T upper) {
		this.upper = upper;
		return this;
	}

	// ------------------------------

	@Override
	public String toString() {
		return String.format("[ {%s} , {%s} )", getLower(), getUpper());
	}
}
