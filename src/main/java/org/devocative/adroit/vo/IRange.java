package org.devocative.adroit.vo;

import java.io.Serializable;

public interface IRange<T extends Serializable> extends Serializable {
	T getLower();

	IRange<T> setLower(T lower);

	// ---------------

	T getUpper();

	IRange<T> setUpper(T upper);
}
