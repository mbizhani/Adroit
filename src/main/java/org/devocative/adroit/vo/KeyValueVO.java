package org.devocative.adroit.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueVO<K extends Serializable, V extends Serializable> implements Serializable {
	private static final long serialVersionUID = 718387772269393769L;

	private K key;
	private V value;

	public KeyValueVO() {
	}

	public KeyValueVO(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public KeyValueVO setKey(K key) {
		this.key = key;
		return this;
	}

	public V getValue() {
		return value;
	}

	public KeyValueVO setValue(V value) {
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		return getValue() != null ? getValue().toString() : "?";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof KeyValueVO)) return false;

		KeyValueVO<?, ?> keyValue = (KeyValueVO<?, ?>) o;

		return !(getKey() != null ? !getKey().equals(keyValue.getKey()) : keyValue.getKey() != null);

	}

	@Override
	public int hashCode() {
		return getKey() != null ? getKey().hashCode() : 0;
	}

	public static <K extends Serializable, V extends Serializable> List<KeyValueVO<K, V>> fromMap(Map<K, V> map) {
		List<KeyValueVO<K, V>> list = new ArrayList<>(map.size());
		for (Map.Entry<K, V> entry : map.entrySet()) {
			list.add(new KeyValueVO<K, V>(entry.getKey(), entry.getValue()));
		}
		return list;
	}

	public static <K extends Serializable, V extends Serializable> Map<K, V> toMap(List<KeyValueVO<K, V>> list) {
		Map<K, V> map = new HashMap<>(list.size());
		for (KeyValueVO<K, V> keyValue : list) {
			map.put(keyValue.getKey(), keyValue.getValue());
		}
		return map;
	}
}
