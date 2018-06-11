package org.devocative.adroit.sql.filter;

public class FilterValue {
	private final Object value;
	private final Object lower;
	private final Object upper;
	private final FilterType type;

	private String sqlFunc;

	// ------------------------------

	public static FilterValue equal(Object value) {
		return new FilterValue(value, FilterType.Equal);
	}

	public static FilterValue contain(String value) {
		return new FilterValue(value, FilterType.Contain);
	}


	public static FilterValue between(Object lower, Object upper) {
		return new FilterValue(lower, upper, FilterType.Between);
	}

	public static FilterValue range(Object lower, Object upper) {
		return new FilterValue(lower, upper, FilterType.Range);
	}

	// ------------------------------

	private FilterValue(Object value, FilterType type) {
		this.value = value;
		this.type = type;

		this.upper = null;
		this.lower = null;
	}

	private FilterValue(Object lower, Object upper, FilterType type) {
		this.type = type;
		this.lower = lower;
		this.upper = upper;
		this.value = null;
	}

	// ------------------------------

	public Object getValue() {
		return value;
	}

	public Object getLower() {
		return lower;
	}

	public Object getUpper() {
		return upper;
	}

	public FilterType getType() {
		return type;
	}

	// ---------------

	public String getSqlFunc() {
		return sqlFunc;
	}

	public FilterValue setSqlFunc(String sqlFunc) {
		this.sqlFunc = sqlFunc;
		return this;
	}
}
