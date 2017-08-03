package org.devocative.adroit.sql.filter;

public class FilterValue {
	private Object value;
	private Object upper;
	private FilterType type;

	// ------------------------------

	public FilterValue(Object value, FilterType type) {
		this(value, null, type);
	}

	public FilterValue(Object lower, Object upper, FilterType type) {
		this.value = lower;
		this.upper = upper;
		this.type = type;
	}

	// ------------------------------

	public Object getValue() {
		return value;
	}

	public Object getUpper() {
		return upper;
	}

	public FilterType getType() {
		return type;
	}
}
